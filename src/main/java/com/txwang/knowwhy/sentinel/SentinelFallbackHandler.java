package com.txwang.knowwhy.sentinel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.txwang.knowwhy.common.BaseResponse;
import com.txwang.knowwhy.common.ResultUtils;
import com.txwang.knowwhy.model.dto.question.QuestionQueryRequest;
import com.txwang.knowwhy.model.dto.questionbank.QuestionBankQueryRequest;
import com.txwang.knowwhy.model.vo.QuestionBankVO;
import com.txwang.knowwhy.model.vo.QuestionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class SentinelFallbackHandler {
    /**
     * listQuestionBankVOByPage 降级操作：直接返回本地数据
     */
    public static BaseResponse<Page<QuestionBankVO>> handleFallback(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                             HttpServletRequest request, Throwable ex) {
        // 可以返回本地数据或空数据
        log.warn("触发降级");

        return ResultUtils.success(null);
    }

    public static BaseResponse<Page<QuestionVO>> handleFallback(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                HttpServletRequest request, Throwable ex) {
        // 可以返回本地数据或空数据
        log.warn("触发降级");
        return ResultUtils.success(null);
    }

    public static BaseResponse<QuestionVO> handleFallback(long id, HttpServletRequest request, Throwable ex) {
        // 可以返回本地数据或空数据
        log.warn("触发降级");
        return ResultUtils.success(null);
    }

}
