package com.example.springboot.controller;

import com.example.springboot.entity.ExamResult;
import com.example.springboot.service.ExamResultService;
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
