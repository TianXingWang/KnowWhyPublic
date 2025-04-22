package com.txwang.knowwhy.model.vo;

import lombok.Data;

@Data
public class AiAnswerVO {
    // 题目ID
    private Long questionID;
    // AI答案内容
    private String content;
}
