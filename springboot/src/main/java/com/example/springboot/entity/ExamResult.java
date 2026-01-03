package com.example.springboot.entity;

public class ExamResult {
    private Long id;
    private Long userId;
    private Long examId;
    private String username;
    private String examTitle;
    private Integer score;
    private Integer totalScore;  // 试卷总分
    private String status; // FINISHED / PASSED / FAILED / PENDING / GRADED 等
    private String submitTime; // 简单用字符串保存
    private String codeAnswer; // 编程题代码（可以是JSON或纯文本）
    private String answerDetail; // 答题详情JSON（包含每道题的用户答案、正确答案、得分等）
    private String gradedBy; // 批改人
    private String gradedTime; // 批改时间
    private String gradingStatus; // 批改状态: PENDING(待批改), GRADED(已批改)
    private Long classId; // 班级ID（非数据库字段，联表查询）
    private String className; // 班级名称（非数据库字段，联表查询）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public String getCodeAnswer() {
        return codeAnswer;
    }

    public void setCodeAnswer(String codeAnswer) {
        this.codeAnswer = codeAnswer;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getAnswerDetail() {
        return answerDetail;
    }

    public void setAnswerDetail(String answerDetail) {
        this.answerDetail = answerDetail;
    }

    public String getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(String gradedBy) {
        this.gradedBy = gradedBy;
    }

    public String getGradedTime() {
        return gradedTime;
    }

    public void setGradedTime(String gradedTime) {
        this.gradedTime = gradedTime;
    }

    public String getGradingStatus() {
        return gradingStatus;
    }

    public void setGradingStatus(String gradingStatus) {
        this.gradingStatus = gradingStatus;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
