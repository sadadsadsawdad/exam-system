package com.example.springboot.dao;

import com.example.springboot.entity.GradingDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GradingDetailMapper {
    
    @Insert("INSERT INTO grading_details(sheet_id, question_id, score, comment, create_time) " +
            "VALUES(#{sheetId}, #{questionId}, #{score}, #{comment}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GradingDetail gradingDetail);
    
    @Select("SELECT * FROM grading_details WHERE sheet_id = #{sheetId}")
    List<GradingDetail> findBySheetId(Long sheetId);
    
    @Select("SELECT * FROM grading_details WHERE id = #{id}")
    GradingDetail findById(Long id);
    
    @Update("UPDATE grading_details SET score = #{score}, comment = #{comment} WHERE id = #{id}")
    int update(GradingDetail gradingDetail);
    
    @Delete("DELETE FROM grading_details WHERE sheet_id = #{sheetId}")
    int deleteBySheetId(Long sheetId);
}
