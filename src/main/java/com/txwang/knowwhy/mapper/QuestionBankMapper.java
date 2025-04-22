package com.txwang.knowwhy.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txwang.knowwhy.model.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangtianxing
* @description 针对表【question_bank(题库)】的数据库操作Mapper
* @createDate 2025-03-04 00:13:34
* @Entity generator.domain.QuestionBank
*/
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

}




