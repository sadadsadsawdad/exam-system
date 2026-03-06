package com.example.springboot.controller;

import com.example.springboot.entity.Exam;
import com.example.springboot.entity.ExamResult;
import com.example.springboot.service.ExamResultService;
import com.example.springboot.service.ExamService;
import com.example.springboot.service.GradingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequestMapping("/api/exam-results")
public class ExamResultController {

    @Autowired
    private ExamResultService examResultService;

    @Autowired
    private ExamService examService;

    @Autowired
    private GradingService gradingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public List<ExamResult> listByUser(@RequestParam("userId") Long userId) {
        return examResultService.getResultsByUserId(userId);
    }

    @GetMapping("/all")
    public List<ExamResult> listAll() {
        return examResultService.getAllResults();
    }

    @GetMapping("/{id}")
    public ExamResult getById(@PathVariable Long id) {
        return examResultService.getResultById(id);
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody ExamResult result) {
        examResultService.saveResult(result);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 提交考试并在后端评分（从数据库查询正确答案）
     * 请求体格式：
     * {
     *   "userId": 1,
     *   "examId": 1,
     *   "username": "张三",
     *   "examTitle": "C语言期末考试",
     *   "totalScore": 100,
     *   "answers": [
     *     {"questionId": 1, "userAnswer": "A"},
     *     {"questionId": 2, "userAnswer": "BC"},
     *     {"questionId": 3, "userAnswer": "1"},
     *     {"questionId": 4, "userCode": "#include..."}
     *   ]
     * }
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitAndGrade(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            Long examId = body.get("examId") != null ? Long.valueOf(body.get("examId").toString()) : null;
            String username = (String) body.getOrDefault("username", "匿名用户");
            String examTitle = (String) body.getOrDefault("examTitle", "");
            int totalScore = body.get("totalScore") != null ? Integer.parseInt(body.get("totalScore").toString()) : 100;
            
            // 幂等性检查：检查用户是否已经提交过该考试
            if (userId != null && examId != null) {
                List<ExamResult> existingResults = examResultService.getResultsByUserId(userId);
                for (ExamResult existing : existingResults) {
                    if (examId.equals(existing.getExamId())) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", "您已经提交过该考试，不能重复提交",
                            "alreadySubmitted", true
                        ));
                    }
                }
            }
            
            // 验证考试状态和时间（允许超时一定时间内提交，考虑网络延迟）
            if (examId != null) {
                Exam exam = examService.getExamById(examId);
                if (exam != null) {
                    // 检查考试状态
                    if (!"PUBLISHED".equals(exam.getStatus()) && !"FINISHED".equals(exam.getStatus())) {
                        return ResponseEntity.badRequest().body(Map.of("error", "考试未发布，无法提交"));
                    }
                    
                    // 检查结束时间（允许超时5分钟内提交，考虑网络延迟和自动交卷）
                    if (exam.getEndTime() != null && !exam.getEndTime().isEmpty()) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            java.util.Date endDate = sdf.parse(exam.getEndTime());
                            long now = System.currentTimeMillis();
                            long gracePeriod = 5 * 60 * 1000; // 5分钟宽限期
                            if (now > endDate.getTime() + gracePeriod) {
                                return ResponseEntity.badRequest().body(Map.of("error", "考试已结束超过5分钟，无法提交"));
                            }
                        } catch (Exception e) {
                            // 时间解析失败，忽略
                        }
                    }
                }
            }
            
            // 获取用户答案
            String answersJson = objectMapper.writeValueAsString(body.get("answers"));
            
            // 调用评分服务
            String gradingResultJson = gradingService.gradeExam(answersJson, totalScore);
            JsonNode gradingResult = objectMapper.readTree(gradingResultJson);
            
            if (gradingResult.has("error")) {
                return ResponseEntity.badRequest().body(Map.of("error", gradingResult.get("error").asText()));
            }
            
            int score = gradingResult.get("score").asInt();
            String answerDetail = gradingResult.get("answerDetails").toString();
            boolean needsGrading = gradingResult.has("needsGrading") && gradingResult.get("needsGrading").asBoolean();
            
            // 检查是否交白卷（所有题目都未作答）
            boolean isBlankPaper = true;
            JsonNode answerDetails = gradingResult.get("answerDetails");
            if (answerDetails != null && answerDetails.isArray()) {
                for (JsonNode detail : answerDetails) {
                    String userAnswer = detail.has("userAnswer") ? detail.get("userAnswer").asText() : "";
                    if (!userAnswer.isEmpty() && !userAnswer.equals("未作答")) {
                        isBlankPaper = false;
                        break;
                    }
                }
            }
            
            // 保存考试结果
            ExamResult result = new ExamResult();
            result.setUserId(userId);
            result.setExamId(examId);
            result.setUsername(username);
            result.setExamTitle(examTitle);
            result.setTotalScore(totalScore);
            result.setStatus("FINISHED");
            result.setSubmitTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            result.setAnswerDetail(answerDetail);
            
            // 交白卷直接0分，状态为已批改
            if (isBlankPaper) {
                result.setScore(0);
                result.setGradingStatus("GRADED");
                needsGrading = false;
            } else {
                result.setScore(score);
                // 如果有编程题需要批改，设置批改状态为PENDING
                if (needsGrading) {
                    result.setGradingStatus("PENDING");
                } else {
                    result.setGradingStatus("GRADED");
                }
            }
            
            examResultService.saveResult(result);
            
            // 返回评分结果
            return ResponseEntity.ok(Map.of(
                "score", score,
                "totalScore", totalScore,
                "needsGrading", needsGrading,
                "answerDetails", objectMapper.readTree(answerDetail)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "提交失败: " + e.getMessage()));
        }
    }
}
