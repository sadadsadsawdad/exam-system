package com.example.springboot.controller;

import com.example.springboot.entity.Question;
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
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public List<Question> list() {
        return questionService.getAllQuestions();
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody Question question) {
        questionService.addQuestion(question);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody Question question) {
        questionService.updateQuestion(question);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        questionService.deleteQuestion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 随机生成试卷
     */
    @GetMapping("/random")
    public Map<String, Object> generateRandomExam(
            @RequestParam(defaultValue = "5") int singleCount,
            @RequestParam(defaultValue = "5") int multiCount,
            @RequestParam(defaultValue = "5") int judgeCount,
            @RequestParam(defaultValue = "2") int programCount) {
        
        List<Question> questions = questionService.generateRandomExam(singleCount, multiCount, judgeCount, programCount);
        
        // 计算总分
        int totalScore = 0;
        for (Question q : questions) {
            if (q.getScore() != null) {
                totalScore += q.getScore();
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("questions", questions);
        result.put("totalScore", totalScore);
        result.put("questionCount", questions.size());
        return result;
    }

    /**
     * 获取各题型数量统计
     */
    @GetMapping("/stats")
    public Map<String, Integer> getQuestionStats() {
        int[] counts = questionService.getQuestionCountByType();
        Map<String, Integer> stats = new HashMap<>();
        stats.put("single", counts[0]);
        stats.put("multi", counts[1]);
        stats.put("judge", counts[2]);
        stats.put("program", counts[3]);
        stats.put("total", counts[0] + counts[1] + counts[2] + counts[3]);
        return stats;
    }
}
