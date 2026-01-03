package com.example.springboot.service;

import com.example.springboot.dao.ExamUserMapper;
import com.example.springboot.entity.ExamUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExamUserService {

    @Autowired
    private ExamUserMapper examUserMapper;

    /**
     * 设置考试的指定用户
     */
    public void setExamUsers(Long examId, List<Long> userIds) {
        // 先删除旧的关联
        examUserMapper.deleteByExamId(examId);
        // 如果有新的用户列表，则插入
        if (userIds != null && !userIds.isEmpty()) {
            examUserMapper.batchInsert(examId, userIds);
        }
    }

    /**
     * 获取考试的指定用户列表
     */
    public List<ExamUser> getExamUsers(Long examId) {
        ExamUser[] arr = examUserMapper.selectByExamId(examId);
        if (arr == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(arr));
    }

    /**
     * 获取考试的指定用户ID列表
     */
    public List<Long> getExamUserIds(Long examId) {
        List<ExamUser> users = getExamUsers(examId);
        List<Long> ids = new ArrayList<>();
        for (ExamUser u : users) {
            ids.add(u.getUserId());
        }
        return ids;
    }

    /**
     * 检查用户是否可以参加考试
     * 如果考试没有指定用户，则所有人都可以参加
     * 如果考试指定了用户，则只有被指定的用户可以参加
     */
    public boolean canUserTakeExam(Long examId, Long userId) {
        int count = examUserMapper.countByExamId(examId);
        if (count == 0) {
            // 没有指定用户，所有人都可以参加
            return true;
        }
        // 检查用户是否在指定列表中
        return examUserMapper.checkUserInExam(examId, userId) > 0;
    }

    /**
     * 删除考试的所有用户关联
     */
    public void deleteByExamId(Long examId) {
        examUserMapper.deleteByExamId(examId);
    }
}
