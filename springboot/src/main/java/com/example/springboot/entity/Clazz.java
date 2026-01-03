package com.example.springboot.entity;

/**
 * 班级实体类
 */
public class Clazz {
    private Long id;
    private String name;        // 班级名称
    private String description; // 班级描述
    private Integer studentCount; // 学生人数（非数据库字段，统计用）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return "Clazz{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", studentCount=" + studentCount +
                '}';
    }
}
