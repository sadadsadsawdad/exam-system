package com.example.springboot.service;

import com.example.springboot.dao.ExamResultMapper;
import com.example.springboot.entity.ExamResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExamResultService {

    @Autowired
    private ExamResultMapper examResultMapper;

    public List<ExamResult> getResultsByUserId(Long userId) {
        ExamResult[] arr = examResultMapper.selectByUserId(userId);
        List<ExamResult> list = new ArrayList<>();
        if (arr != null) {
            for (ExamResult r : arr) {
                list.add(r);
            }
        }
        return list;
    }

    public List<ExamResult> getAllResults() {
        ExamResult[] arr = examResultMapper.selectAll();
        List<ExamResult> list = new ArrayList<>();
        if (arr != null) {
            for (ExamResult r : arr) {
                list.add(r);
            }
        }
        return list;
    }

    public ExamResult getResultById(Long id) {
        return examResultMapper.selectById(id);
    }

    public void saveResult(ExamResult result) {
        examResultMapper.insert(result);
    }

    public void deleteByExamId(Long examId) {
        examResultMapper.deleteByExamId(examId);
    }
}
