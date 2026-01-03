package com.example.springboot.entity;

public class Message {
    private Long id;
    private String scope;      // 发布范围: school(学校), class(班级), student(学生)
    private String title;      // 标题
    private String content;    // 内容
    private String sender;     // 发送人
    private Long senderId;     // 发送人ID
    private String createTime; // 创建时间
    private String status;     // 发布状态: published(已发布), draft(未发布)
    private Long classId;      // 班级ID（如果是班级范围）
    private String type;       // 消息类型: manual(手动发布), exam(考试通知)
    private Long relatedId;    // 关联ID（如考试ID）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }
}
