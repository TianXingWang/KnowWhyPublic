package com.txwang.knowwhy.sentinel;


import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.txwang.knowwhy.common.BaseResponse;
import com.txwang.knowwhy.common.ErrorCode;
import com.txwang.knowwhy.common.ResultUtils;
import com.txwang.knowwhy.constant.RedisConstant;
import com.txwang.knowwhy.model.dto.questionbank.QuestionBankQueryRequest;
import com.txwang.knowwhy.model.entity.Question;
import com.txwang.knowwhy.model.vo.QuestionBankVO;
import com.txwang.knowwhy.model.vo.QuestionVO;
import com.txwang.knowwhy.service.QuestionService;
import com.txwang.knowwhy.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.txwang.knowwhy.sentinel.SentinelFallbackHandler.handleFallback;

@Slf4j
@Component
public class SentinelBlockHandler {

    @Resource
    private RedisUtils<String> redisUtils;

    @Resource
    private QuestionService questionService;

    private static RedisUtils redisUtilsStatic;

    private static QuestionService questionServiceStatic;

    @PostConstruct
    private void init() {
        redisUtilsStatic = redisUtils;
        questionServiceStatic = questionService;
    }

    /**
     * listQuestionBankVOByPage 流控操作
     * 限流：提示“系统压力过大，请耐心等待”
     * 熔断：执行降级操作
     */
    public static BaseResponse<Page<QuestionBankVO>> handleBlockException(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                                   HttpServletRequest request, BlockException ex) {
        // 降级操作
        if (ex instanceof DegradeException) {
            return handleFallback(questionBankQueryRequest, request, ex);
        }
        // 限流操作
        log.warn("触发限流");

        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统压力过大，请耐心等待");
    }

    public static BaseResponse<QuestionVO> handleBlockException(long id,
                                                                HttpServletRequest request, BlockException ex) {
        // 降级操作
        if (ex instanceof DegradeException) {
            return handleFallback(id, request, ex);
        }
        // 限流操作
        log.warn("题目触发限流");

        if (redisUtilsStatic.get(RedisConstant.getQuestionRedisKey(id)) == null) {
            Question question = questionServiceStatic.getById(id);
            String questionJsonString = JSON.toJSONString(question);
            redisUtilsStatic.set(RedisConstant.getQuestionRedisKey(id), questionJsonString);
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统压力过大，请耐心等待");
    }


}
