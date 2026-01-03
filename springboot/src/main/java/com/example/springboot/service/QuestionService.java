package com.example.springboot.service;

import com.example.springboot.dao.QuestionMapper;
import com.example.springboot.entity.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    public List<Question> getAllQuestions() {
        Question[] array = questionMapper.selectAll();
        List<Question> list = new ArrayList<>();
        if (array != null) {
            for (Question q : array) {
                list.add(q);
            }
        }
        return list;
    }

    public void addQuestion(Question question) {
        questionMapper.insert(question);
    }

    public void updateQuestion(Question question) {
        questionMapper.update(question);
    }

    public void deleteQuestion(Long id) {
        questionMapper.delete(id);
    }

    /**
     * 随机生成试卷（总分100分）
     * @param singleCount 单选题数量
     * @param multiCount 多选题数量
     * @param judgeCount 判断题数量
     * @param programCount 编程题数量
     * @return 随机题目列表，每题分数已调整使总分为100
     */
    public List<Question> generateRandomExam(int singleCount, int multiCount, int judgeCount, int programCount) {
        List<Question> result = new ArrayList<>();
        
        // 1-单选题
        if (singleCount > 0) {
            Question[] singles = questionMapper.selectRandomByType(1, singleCount);
            if (singles != null) {
                result.addAll(Arrays.asList(singles));
            }
        }
        
        // 2-多选题
        if (multiCount > 0) {
            Question[] multis = questionMapper.selectRandomByType(2, multiCount);
            if (multis != null) {
                result.addAll(Arrays.asList(multis));
            }
        }
        
        // 3-判断题
        if (judgeCount > 0) {
            Question[] judges = questionMapper.selectRandomByType(3, judgeCount);
            if (judges != null) {
                result.addAll(Arrays.asList(judges));
            }
        }
        
        // 4-编程题
        if (programCount > 0) {
            Question[] programs = questionMapper.selectRandomByType(4, programCount);
            if (programs != null) {
                result.addAll(Arrays.asList(programs));
            }
        }
        
        // 调整每题分数，使总分为100分
        adjustScoresToTotal(result, 100);
        
        return result;
    }
    
    /**
     * 调整题目分数使总分为指定值（100分）
     * 简单策略：每题平均分，余数加到前几题
     */
    private void adjustScoresToTotal(List<Question> questions, int totalScore) {
        if (questions == null || questions.isEmpty()) return;
        
        int count = questions.size();
        int baseScore = totalScore / count;  // 每题基础分
        int remainder = totalScore % count;  // 余数
        
        System.out.println("[评分调整] 题目数量: " + count + ", 每题基础分: " + baseScore + ", 余数: " + remainder);
        
        for (int i = 0; i < count; i++) {
            Question q = questions.get(i);
            int score = (i < remainder) ? (baseScore + 1) : baseScore;
            q.setScore(score);
            System.out.println("[评分调整] 第" + (i+1) + "题: " + q.getTitle() + " -> " + score + "分");
        }
        
        // 验证总分
        int total = 0;
        for (Question q : questions) {
            total += q.getScore();
        }
        System.out.println("[评分调整] 总分验证: " + total);
    }

    /**
     * 获取各题型数量统计
     */
    public int[] getQuestionCountByType() {
        int[] counts = new int[4];
        counts[0] = questionMapper.countByType(1); // 单选
        counts[1] = questionMapper.countByType(2); // 多选
        counts[2] = questionMapper.countByType(3); // 判断
        counts[3] = questionMapper.countByType(4); // 编程
        return counts;
    }
}
