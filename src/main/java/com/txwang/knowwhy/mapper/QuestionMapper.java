package com.txwang.knowwhy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txwang.knowwhy.model.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author wangtianxing
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2025-03-04 00:13:34
* @Entity generator.domain.Question
*/
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    /**
     * 查询题目列表（包括已被删除的数据）
     */
    @Select("select * from question where updateTime >= #{minUpdateTime}")
    List<Question> listQuestionWithDelete(Date minUpdateTime);

}




