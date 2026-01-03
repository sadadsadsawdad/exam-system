package com.example.springboot.service;

import com.example.springboot.dao.ExamMapper;
import com.example.springboot.entity.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExamService {

    @Autowired
    private ExamMapper examMapper;

    public List<Exam> getAllExams() {
        Exam[] arr = examMapper.selectAll();
        List<Exam> list = new ArrayList<>();
        if (arr != null) {
            for (Exam e : arr) {
                list.add(e);
            }
        }
        return list;
    }

    public Exam getExamById(Long id) {
        return examMapper.selectById(id);
    }

    public void addExam(Exam exam) {
        examMapper.insert(exam);
    }

    public void updateExam(Exam exam) {
        examMapper.update(exam);
    }

    public void deleteExam(Long id) {
        examMapper.delete(id);
    }
}
