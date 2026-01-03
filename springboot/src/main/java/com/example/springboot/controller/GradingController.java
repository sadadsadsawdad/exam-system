package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dao.ExamResultMapper;
import com.example.springboot.dao.GradingDetailMapper;
import com.example.springboot.entity.ExamResult;
import com.example.springboot.entity.GradingDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class GradingController {

    @Autowired
    private ExamResultMapper examResultMapper;

    @Autowired
    private GradingDetailMapper gradingDetailMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取批改列表
     * @param status 批改状态 (PENDING, GRADED)
     * @param examId 考试ID
     * @param keyword 搜索关键词
     * @return 答卷列表
     */
    @GetMapping("/exam-results/grading")
    public Result getGradingList(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) Long examId,
                                  @RequestParam(required = false) String keyword) {
        try {
            List<ExamResult> results = examResultMapper.selectForGrading(status, examId, keyword);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error("获取批改列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取答卷详情
     * @param id 答卷ID
     * @return 答卷详情，包含所有题目和答案
     */
    @GetMapping("/exam-results/{id}/detail")
    public Result getExamResultDetail(@PathVariable Long id) {
        try {
            ExamResult result = examResultMapper.selectById(id);
            if (result == null) {
                return Result.error("答卷不存在");
            }
            
            // 解析answerDetail JSON
            if (result.getAnswerDetail() != null && !result.getAnswerDetail().isEmpty()) {
                JsonNode answerDetail = objectMapper.readTree(result.getAnswerDetail());
                result.setAnswerDetail(answerDetail.toString());
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取答卷详情失败: " + e.getMessage());
        }
    }

    /**
     * 保存批改结果
     * @param request 批改请求，包含答卷ID、题目分数和评语
     * @return 保存结果
     */
    @PostMapping("/grading")
    public Result saveGrading(@RequestBody Map<String, Object> request) {
        try {
            Long sheetId = Long.valueOf(request.get("sheetId").toString());
            String gradedBy = request.get("gradedBy") != null ? request.get("gradedBy").toString() : "admin";
            List<Map<String, Object>> answers = (List<Map<String, Object>>) request.get("answers");
            
            // 获取答卷
            ExamResult examResult = examResultMapper.selectById(sheetId);
            if (examResult == null) {
                return Result.error("答卷不存在");
            }
            
            // 计算总分
            int totalScore = 0;
            for (Map<String, Object> answer : answers) {
                Integer score = answer.get("score") != null ? 
                    Integer.valueOf(answer.get("score").toString()) : 0;
                totalScore += score;
                
                // 保存批改详情
                GradingDetail detail = new GradingDetail();
                detail.setSheetId(sheetId);
                detail.setQuestionId(Long.valueOf(answer.get("questionId").toString()));
                detail.setScore(score);
                detail.setComment(answer.get("comment") != null ? answer.get("comment").toString() : "");
                detail.setCreateTime(LocalDateTime.now().format(formatter));
                
                // 先删除旧的批改记录（如果存在）
                gradingDetailMapper.deleteBySheetId(sheetId);
                gradingDetailMapper.insert(detail);
            }
            
            // 更新答卷
            examResult.setScore(totalScore);
            examResult.setGradingStatus("GRADED");
            examResult.setGradedBy(gradedBy);
            examResult.setGradedTime(LocalDateTime.now().format(formatter));
            examResultMapper.update(examResult);
            
            return Result.success("批改保存成功");
        } catch (Exception e) {
            return Result.error("保存批改失败: " + e.getMessage());
        }
    }

    /**
     * 获取考试统计信息
     * @return 所有考试的完成统计
     */
    @GetMapping("/exam-results/statistics")
    public Result getExamStatistics() {
        try {
            // 获取所有答卷
            ExamResult[] allResults = examResultMapper.selectAll();
            
            // 按考试ID分组统计
            java.util.Map<Long, java.util.Map<String, Object>> statsMap = new java.util.HashMap<>();
            
            for (ExamResult result : allResults) {
                Long examId = result.getExamId();
                if (!statsMap.containsKey(examId)) {
                    java.util.Map<String, Object> stat = new java.util.HashMap<>();
                    stat.put("examId", examId);
                    stat.put("examTitle", result.getExamTitle());
                    stat.put("submittedCount", 0);
                    stat.put("gradedCount", 0);
                    stat.put("totalScore", 0);
                    statsMap.put(examId, stat);
                }
                
                java.util.Map<String, Object> stat = statsMap.get(examId);
                stat.put("submittedCount", (Integer) stat.get("submittedCount") + 1);
                
                if ("GRADED".equals(result.getGradingStatus())) {
                    stat.put("gradedCount", (Integer) stat.get("gradedCount") + 1);
                    if (result.getScore() != null) {
                        stat.put("totalScore", (Integer) stat.get("totalScore") + result.getScore());
                    }
                }
            }
            
            // 计算完成率和平均分
            java.util.List<java.util.Map<String, Object>> statsList = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> stat : statsMap.values()) {
                int submittedCount = (Integer) stat.get("submittedCount");
                int gradedCount = (Integer) stat.get("gradedCount");
                int totalScore = (Integer) stat.get("totalScore");
                
                // 计算完成率（这里假设应考人数等于提交人数）
                double completionRate = submittedCount > 0 ? 100.0 : 0.0;
                stat.put("completionRate", completionRate);
                
                // 计算平均分
                double averageScore = gradedCount > 0 ? (double) totalScore / gradedCount : 0.0;
                stat.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
                
                stat.put("totalStudents", submittedCount); // 应考人数
                
                statsList.add(stat);
            }
            
            return Result.success(statsList);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取按班级统计的信息
     * @return 所有班级的完成统计
     */
    @GetMapping("/exam-results/statistics-by-class")
    public Result getStatisticsByClass() {
        try {
            // 获取所有答卷（带班级信息）
            List<ExamResult> allResults = examResultMapper.selectForGrading(null, null, null);
            
            // 按班级ID分组统计
            java.util.Map<Long, java.util.Map<String, Object>> statsMap = new java.util.HashMap<>();
            
            for (ExamResult result : allResults) {
                Long classId = result.getClassId();
                String className = result.getClassName();
                
                // 如果没有班级信息，归类到"未分配班级"
                if (classId == null) {
                    classId = 0L;
                    className = "未分配班级";
                }
                
                if (!statsMap.containsKey(classId)) {
                    java.util.Map<String, Object> stat = new java.util.HashMap<>();
                    stat.put("classId", classId);
                    stat.put("className", className);
                    stat.put("submittedCount", 0);
                    stat.put("gradedCount", 0);
                    stat.put("totalScore", 0);
                    statsMap.put(classId, stat);
                }
                
                java.util.Map<String, Object> stat = statsMap.get(classId);
                stat.put("submittedCount", (Integer) stat.get("submittedCount") + 1);
                
                if ("GRADED".equals(result.getGradingStatus())) {
                    stat.put("gradedCount", (Integer) stat.get("gradedCount") + 1);
                    if (result.getScore() != null) {
                        stat.put("totalScore", (Integer) stat.get("totalScore") + result.getScore());
                    }
                }
            }
            
            // 计算完成率和平均分
            java.util.List<java.util.Map<String, Object>> statsList = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> stat : statsMap.values()) {
                int submittedCount = (Integer) stat.get("submittedCount");
                int gradedCount = (Integer) stat.get("gradedCount");
                int totalScore = (Integer) stat.get("totalScore");
                
                // 计算完成率
                double completionRate = submittedCount > 0 ? 100.0 : 0.0;
                stat.put("completionRate", completionRate);
                
                // 计算平均分
                double averageScore = gradedCount > 0 ? (double) totalScore / gradedCount : 0.0;
                stat.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
                
                stat.put("totalStudents", submittedCount);
                
                statsList.add(stat);
            }
            
            return Result.success(statsList);
        } catch (Exception e) {
            return Result.error("获取班级统计信息失败: " + e.getMessage());
        }
    }
}
