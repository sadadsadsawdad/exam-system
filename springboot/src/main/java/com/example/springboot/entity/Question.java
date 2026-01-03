package com.example.springboot.entity;

public class Question {
    private Long id;
    private String title;
    private String content;
    private Integer type;       // 1单选 2多选 3判断 4编程
    private Integer score;
    private Integer difficulty; // 1简单 2中等 3困难
    private String subject;
    private String tags;
    private Integer judgeAnswer; // 判断题答案：1正确 0错误
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // 正确选项，例如 "A" 或 "AB"
    private String inputDesc;
    private String outputDesc;
    private String sampleInput;
    private String sampleOutput;
    private String standardCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getJudgeAnswer() { return judgeAnswer; }
    public void setJudgeAnswer(Integer judgeAnswer) { this.judgeAnswer = judgeAnswer; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

    public String getInputDesc() { return inputDesc; }
    public void setInputDesc(String inputDesc) { this.inputDesc = inputDesc; }

    public String getOutputDesc() { return outputDesc; }
    public void setOutputDesc(String outputDesc) { this.outputDesc = outputDesc; }

    public String getSampleInput() { return sampleInput; }
    public void setSampleInput(String sampleInput) { this.sampleInput = sampleInput; }

    public String getSampleOutput() { return sampleOutput; }
    public void setSampleOutput(String sampleOutput) { this.sampleOutput = sampleOutput; }

    public String getStandardCode() { return standardCode; }
    public void setStandardCode(String standardCode) { this.standardCode = standardCode; }
}
