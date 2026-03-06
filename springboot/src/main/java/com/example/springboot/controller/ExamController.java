package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Exam;
import com.example.springboot.entity.ExamResult;
import com.example.springboot.entity.ExamUser;
import com.example.springboot.entity.Question;
import com.example.springboot.entity.User;
import com.example.springboot.service.ExamService;
import com.example.springboot.service.ExamResultService;
import com.example.springboot.service.ExamUserService;
import com.example.springboot.service.NotificationService;
import com.example.springboot.service.QuestionService;
import com.example.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequestMapping("/api/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamResultService examResultService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamUserService examUserService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Exam> list() {
        return examService.getAllExams();
    }

    @PostMapping
    public ResponseEntity<Exam> add(@RequestBody Exam exam) {
        examService.addExam(exam);
        
        // 如果新增时直接设置为PUBLISHED状态，发送通知
        if ("PUBLISHED".equals(exam.getStatus())) {
            notificationService.sendExamNotification(
                exam.getId(),
                exam.getTitle(),
                exam.getClassId(),
                exam.getStartTime(),
                exam.getDurationMinutes()
            );
        }
        
        return new ResponseEntity<>(exam, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody Exam exam) {
        // 获取更新前的考试状态
        Exam oldExam = examService.getExamById(exam.getId());
        String oldStatus = oldExam != null ? oldExam.getStatus() : null;
        
        examService.updateExam(exam);
        
        // 如果状态从非PUBLISHED变为PUBLISHED，发送通知
        if ("PUBLISHED".equals(exam.getStatus()) && !"PUBLISHED".equals(oldStatus)) {
            notificationService.sendExamNotification(
                exam.getId(), 
                exam.getTitle(), 
                exam.getClassId(),
                exam.getStartTime(),
                exam.getDurationMinutes()
            );
        }
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        // 先删除该考试对应的所有成绩记录，再删除考试本身
        examResultService.deleteByExamId(id);
        examService.deleteExam(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 获取考试详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Exam> getById(@PathVariable("id") Long id) {
        Exam exam = examService.getExamById(id);
        if (exam == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(exam, HttpStatus.OK);
    }

    /**
     * 学生开始考试时，根据考试配置随机生成题目
     * 每个学生调用此接口都会获得不同的随机题目
     */
    @GetMapping("/{id}/questions")
    public Map<String, Object> getExamQuestions(@PathVariable("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        
        Exam exam = examService.getExamById(id);
        if (exam == null) {
            result.put("error", "考试不存在");
            return result;
        }
        
        // 验证考试状态
        if (!"PUBLISHED".equals(exam.getStatus())) {
            result.put("error", "考试未发布，无法参加");
            return result;
        }
        
        // 验证考试时间
        long now = System.currentTimeMillis();
        
        // 检查开始时间
        if (exam.getStartTime() != null && !exam.getStartTime().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date startDate = sdf.parse(exam.getStartTime());
                if (now < startDate.getTime()) {
                    result.put("error", "考试尚未开始，请在开始时间后再来");
                    return result;
                }
            } catch (Exception e) {
                // 时间解析失败，记录日志并返回错误
                System.err.println("考试开始时间解析失败: " + exam.getStartTime() + ", 错误: " + e.getMessage());
                result.put("error", "考试时间配置异常，请联系管理员");
                return result;
            }
        }
        
        // 检查结束时间
        if (exam.getEndTime() != null && !exam.getEndTime().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date endDate = sdf.parse(exam.getEndTime());
                if (now > endDate.getTime()) {
                    result.put("error", "考试已结束，无法参加");
                    return result;
                }
            } catch (Exception e) {
                // 时间解析失败，记录日志并返回错误
                System.err.println("考试结束时间解析失败: " + exam.getEndTime() + ", 错误: " + e.getMessage());
                result.put("error", "考试时间配置异常，请联系管理员");
                return result;
            }
        }
        
        // 根据考试配置随机抽取题目
        int singleCount = exam.getSingleCount() != null ? exam.getSingleCount() : 0;
        int multiCount = exam.getMultiCount() != null ? exam.getMultiCount() : 0;
        int judgeCount = exam.getJudgeCount() != null ? exam.getJudgeCount() : 0;
        int programCount = exam.getProgramCount() != null ? exam.getProgramCount() : 0;
        
        List<Question> questions = null;
        try {
            questions = questionService.generateRandomExam(singleCount, multiCount, judgeCount, programCount);
        } catch (IllegalArgumentException e) {
            result.put("error", e.getMessage());
            return result;
        }
        
        // 使用考试设置的总分，如果没有设置则累加题目分数
        int totalScore = exam.getTotalScore() != null ? exam.getTotalScore() : 0;
        if (totalScore == 0) {
            for (Question q : questions) {
                if (q.getScore() != null) {
                    totalScore += q.getScore();
                }
            }
        }
        
        result.put("exam", exam);
        result.put("questions", questions);
        result.put("totalScore", totalScore);
        result.put("questionCount", questions.size());
        
        return result;
    }

    /**
     * 获取考试的指定用户列表
     */
    @GetMapping("/{id}/users")
    public List<ExamUser> getExamUsers(@PathVariable("id") Long id) {
        return examUserService.getExamUsers(id);
    }

    /**
     * 获取考试的指定用户ID列表
     */
    @GetMapping("/{id}/userIds")
    public List<Long> getExamUserIds(@PathVariable("id") Long id) {
        return examUserService.getExamUserIds(id);
    }

    /**
     * 设置考试的指定用户
     */
    @PostMapping("/{id}/users")
    public ResponseEntity<Void> setExamUsers(@PathVariable("id") Long id, @RequestBody List<Long> userIds) {
        examUserService.setExamUsers(id, userIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 处理缺考：为未参加考试的学生生成缺考记录
     * 只有考试结束后才能调用此接口
     */
    @PostMapping("/{id}/process-absent")
    public Result processAbsentStudents(@PathVariable("id") Long id) {
        try {
            Exam exam = examService.getExamById(id);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            
            // 检查考试是否已结束
            if (exam.getEndTime() != null && !exam.getEndTime().isEmpty()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date endDate = sdf.parse(exam.getEndTime());
                if (System.currentTimeMillis() < endDate.getTime()) {
                    return Result.error("考试尚未结束，无法处理缺考");
                }
            }
            
            // 获取应参加考试的学生（班级内的学生）
            Long classId = exam.getClassId();
            if (classId == null) {
                return Result.error("该考试未指定班级，无法确定应参加的学生");
            }
            
            List<User> classStudents = userService.getUsersByClassId(classId);
            if (classStudents == null || classStudents.isEmpty()) {
                return Result.error("班级(ID:" + classId + ")内没有学生");
            }
            
            // 获取已参加考试的学生ID
            List<ExamResult> existingResults = examResultService.getResultsByExamId(id);
            Set<Long> participatedUserIds = new HashSet<>();
            for (ExamResult r : existingResults) {
                if (r.getUserId() != null) {
                    participatedUserIds.add(r.getUserId());
                }
            }
            
            // 统计班级内的学生数（排除管理员）
            int studentCount = 0;
            for (User student : classStudents) {
                String role = student.getRole();
                if (!"admin".equalsIgnoreCase(role) && !"2".equals(role)) {
                    studentCount++;
                }
            }
            
            // 为未参加的学生生成缺考记录
            int absentCount = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            
            for (User student : classStudents) {
                // 只处理学生角色（排除管理员）
                String role = student.getRole();
                if ("admin".equalsIgnoreCase(role) || "2".equals(role)) {
                    continue;
                }
                
                if (!participatedUserIds.contains(student.getId())) {
                    ExamResult absentResult = new ExamResult();
                    absentResult.setUserId(student.getId());
                    absentResult.setExamId(id);
                    absentResult.setUsername(student.getUsername());
                    absentResult.setExamTitle(exam.getTitle());
                    absentResult.setScore(0);
                    absentResult.setTotalScore(exam.getTotalScore() != null ? exam.getTotalScore() : 100);
                    absentResult.setStatus("ABSENT"); // 缺考状态
                    absentResult.setSubmitTime(now);
                    absentResult.setGradingStatus("GRADED"); // 缺考直接标记为已批改
                    absentResult.setAnswerDetail("[]"); // 空答案
                    
                    examResultService.saveResult(absentResult);
                    absentCount++;
                }
            }
            
            // 返回详细信息
            String message = String.format("处理完成：班级共%d名学生，已参加%d人，标记缺考%d人", 
                studentCount, participatedUserIds.size(), absentCount);
            return Result.success(message);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("处理缺考失败: " + e.getMessage());
        }
    }

    /**
     * 修复缺考记录：将所有status=ABSENT但gradingStatus不是GRADED的记录更新为GRADED
     */
    @PostMapping("/fix-absent-status")
    public Result fixAbsentStatus() {
        try {
            int fixedCount = examResultService.fixAbsentGradingStatus();
            return Result.success("修复完成，共更新 " + fixedCount + " 条缺考记录");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修复失败: " + e.getMessage());
        }
    }
}
