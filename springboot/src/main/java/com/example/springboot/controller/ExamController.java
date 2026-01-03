package com.example.springboot.controller;

import com.example.springboot.entity.Exam;
import com.example.springboot.entity.ExamUser;
import com.example.springboot.entity.Question;
import com.example.springboot.service.ExamService;
import com.example.springboot.service.ExamResultService;
import com.example.springboot.service.ExamUserService;
import com.example.springboot.service.NotificationService;
import com.example.springboot.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequestMapping("/api/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamResultService examResultService;

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
        
        // 根据考试配置随机抽取题目
        int singleCount = exam.getSingleCount() != null ? exam.getSingleCount() : 0;
        int multiCount = exam.getMultiCount() != null ? exam.getMultiCount() : 0;
        int judgeCount = exam.getJudgeCount() != null ? exam.getJudgeCount() : 0;
        int programCount = exam.getProgramCount() != null ? exam.getProgramCount() : 0;
        
        List<Question> questions = questionService.generateRandomExam(singleCount, multiCount, judgeCount, programCount);
        
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
}
