package com.txwang.knowwhy.model.dto.question;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionAiAnswerRequest implements Serializable {

    // 题目ID
    private Long questionId;

    private static final long serialVersionUID = 1L;

}
