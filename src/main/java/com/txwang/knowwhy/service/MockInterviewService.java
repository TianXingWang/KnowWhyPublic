package com.txwang.knowwhy.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.txwang.knowwhy.model.dto.mockinterview.MockInterviewAddRequest;
import com.txwang.knowwhy.model.dto.mockinterview.MockInterviewEventRequest;
import com.txwang.knowwhy.model.dto.mockinterview.MockInterviewQueryRequest;
import com.txwang.knowwhy.model.entity.MockInterview;
import com.txwang.knowwhy.model.entity.User;

/**
* @author wangtianxing
* @description 针对表【mock_interview(模拟面试)】的数据库操作Service
* @createDate 2025-03-18 18:02:23
*/
public interface MockInterviewService extends IService<MockInterview> {
    Long createMockInterview(MockInterviewAddRequest mockInterviewAddRequest, User loginUser);

    QueryWrapper<MockInterview> getQueryWrapper(MockInterviewQueryRequest mockInterviewQueryRequest);


    String handleMockInterviewEvent(MockInterviewEventRequest mockInterviewEventRequest, User loginUser);
}
