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
     * @throws IllegalArgumentException 如果题库中题目数量不足
     */
    public List<Question> generateRandomExam(int singleCount, int multiCount, int judgeCount, int programCount) {
        List<Question> result = new ArrayList<>();
        
        // 先检查题库数量是否足够
        int[] availableCounts = getQuestionCountByType();
        StringBuilder errorMsg = new StringBuilder();
        
        if (singleCount > 0 && availableCounts[0] < singleCount) {
            errorMsg.append("单选题不足(需要").append(singleCount).append("题，题库仅有").append(availableCounts[0]).append("题); ");
        }
        if (multiCount > 0 && availableCounts[1] < multiCount) {
            errorMsg.append("多选题不足(需要").append(multiCount).append("题，题库仅有").append(availableCounts[1]).append("题); ");
        }
        if (judgeCount > 0 && availableCounts[2] < judgeCount) {
            errorMsg.append("判断题不足(需要").append(judgeCount).append("题，题库仅有").append(availableCounts[2]).append("题); ");
        }
        if (programCount > 0 && availableCounts[3] < programCount) {
            errorMsg.append("编程题不足(需要").append(programCount).append("题，题库仅有").append(availableCounts[3]).append("题); ");
        }
        
        if (errorMsg.length() > 0) {
            throw new IllegalArgumentException("题库题目数量不足: " + errorMsg.toString());
        }
        
        System.out.println("[随机出题] 开始生成试卷，时间戳: " + System.currentTimeMillis());
        System.out.println("[随机出题] 题库统计 - 单选:" + availableCounts[0] + " 多选:" + availableCounts[1] + 
                          " 判断:" + availableCounts[2] + " 编程:" + availableCounts[3]);
        
        // 1-单选题
        if (singleCount > 0) {
            Question[] singles = questionMapper.selectRandomByType(1, singleCount);
            if (singles != null) {
                System.out.println("[随机出题] 抽取单选题 " + singles.length + " 道: " + getQuestionIds(singles));
                result.addAll(Arrays.asList(singles));
            }
        }
        
        // 2-多选题
        if (multiCount > 0) {
            Question[] multis = questionMapper.selectRandomByType(2, multiCount);
            if (multis != null) {
                System.out.println("[随机出题] 抽取多选题 " + multis.length + " 道: " + getQuestionIds(multis));
                result.addAll(Arrays.asList(multis));
            }
        }
        
        // 3-判断题
        if (judgeCount > 0) {
            Question[] judges = questionMapper.selectRandomByType(3, judgeCount);
            if (judges != null) {
                System.out.println("[随机出题] 抽取判断题 " + judges.length + " 道: " + getQuestionIds(judges));
                result.addAll(Arrays.asList(judges));
            }
        }
        
        // 4-编程题
        if (programCount > 0) {
            Question[] programs = questionMapper.selectRandomByType(4, programCount);
            if (programs != null) {
                System.out.println("[随机出题] 抽取编程题 " + programs.length + " 道: " + getQuestionIds(programs));
                result.addAll(Arrays.asList(programs));
            }
        }
        
        // 打乱题目顺序，增加随机性
        java.util.Collections.shuffle(result);
        System.out.println("[随机出题] 打乱后题目顺序: " + getQuestionIdsFromList(result));
        
        // 调整每题分数，使总分为100分
        adjustScoresToTotal(result, 100);
        
        return result;
    }
    
    private String getQuestionIds(Question[] questions) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < questions.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(questions[i].getId());
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String getQuestionIdsFromList(List<Question> questions) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(questions.get(i).getId());
        }
        sb.append("]");
        return sb.toString();
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
