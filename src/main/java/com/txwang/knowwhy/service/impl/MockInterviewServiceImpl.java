package com.txwang.knowwhy.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.txwang.knowwhy.common.ErrorCode;
import com.txwang.knowwhy.constant.CommonConstant;
import com.txwang.knowwhy.exception.BusinessException;
import com.txwang.knowwhy.exception.ThrowUtils;
import com.txwang.knowwhy.manager.AiManager;
import com.txwang.knowwhy.model.dto.mockinterview.MockInterviewAddRequest;
import com.txwang.knowwhy.model.dto.mockinterview.MockInterviewChatMessage;
import com.txwang.knowwhy.model.dto.mockinterview.MockInterviewEventRequest;
import com.txwang.knowwhy.model.dto.mockinterview.MockInterviewQueryRequest;
import com.txwang.knowwhy.model.entity.MockInterview;
import com.txwang.knowwhy.model.entity.User;
import com.txwang.knowwhy.model.enums.MockInterviewEventEnum;
import com.txwang.knowwhy.model.enums.MockInterviewStatusEnum;
import com.txwang.knowwhy.service.MockInterviewService;
import com.txwang.knowwhy.mapper.MockInterviewMapper;
import com.txwang.knowwhy.utils.SqlUtils;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
* @author wangtianxing
* @description 针对表【mock_interview(模拟面试)】的数据库操作Service实现
* @createDate 2025-03-18 18:02:23
*/
@Service
public class MockInterviewServiceImpl extends ServiceImpl<MockInterviewMapper, MockInterview>
    implements MockInterviewService{

    @Resource
    private AiManager aiManager;

    /**
     * 创建模拟面试
     * @param mockInterviewAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public Long createMockInterview(MockInterviewAddRequest mockInterviewAddRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(mockInterviewAddRequest == null || loginUser == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.hasBlank(mockInterviewAddRequest.getJobPosition(),
                mockInterviewAddRequest.getDifficulty(), mockInterviewAddRequest.getWorkExperience()),
                ErrorCode.PARAMS_ERROR);
        // 创建实体类
        MockInterview mockInterview = new MockInterview();
        BeanUtils.copyProperties(mockInterviewAddRequest, mockInterview);
        mockInterview.setUserId(loginUser.getId());
        // 插入数据
        boolean result = this.save(mockInterview);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回
        return mockInterview.getId();
    }

    /**
     * 获取查询条件
     *
     * @param mockInterviewQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<MockInterview> getQueryWrapper(MockInterviewQueryRequest mockInterviewQueryRequest) {
        QueryWrapper<MockInterview> queryWrapper = new QueryWrapper<>();
        if (mockInterviewQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = mockInterviewQueryRequest.getId();
        String workExperience = mockInterviewQueryRequest.getWorkExperience();
        String jobPosition = mockInterviewQueryRequest.getJobPosition();
        String difficulty = mockInterviewQueryRequest.getDifficulty();
        Integer status = mockInterviewQueryRequest.getStatus();
        Long userId = mockInterviewQueryRequest.getUserId();
        String sortField = mockInterviewQueryRequest.getSortField();
        String sortOrder = mockInterviewQueryRequest.getSortOrder();
        // 补充需要的查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotBlank(workExperience), "workExperience", workExperience);
        queryWrapper.like(StringUtils.isNotBlank(jobPosition), "jobPosition", jobPosition);
        queryWrapper.like(StringUtils.isNotBlank(difficulty), "difficulty", difficulty);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public String handleMockInterviewEvent(MockInterviewEventRequest mockInterviewEventRequest, User loginUser) {
        //  校验并区分事件
        ThrowUtils.throwIf(mockInterviewEventRequest == null || loginUser == null,
                ErrorCode.PARAMS_ERROR);
        Long id = mockInterviewEventRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);

        MockInterview mockInterview = this.getById(id);
        ThrowUtils.throwIf(mockInterview == null, ErrorCode.NOT_FOUND_ERROR);


        MockInterviewEventEnum mockInterviewEventEnum = MockInterviewEventEnum.getEnumByValue(mockInterviewEventRequest.getEvent());
        switch (mockInterviewEventEnum) {
            case START:
                return handleChatStartEvent(mockInterview);
            case CHAT:
                return handleChatMessageEvent(mockInterviewEventRequest, mockInterview);
            case END:
                return handleChatEndEvent(mockInterview);
            default:
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
    }

    /**
     * 处理 AI 对话开始事件
     *
     * @param mockInterview
     * @return
     */
    private String handleChatStartEvent(MockInterview mockInterview) {
        // 构造消息列表
        // 定义 AI 的 Prompt
        String systemPrompt = String.format("你是一位严厉的程序员面试官，我是候选人，来应聘 %s 的 %s 岗位，面试难度为 %s。请你向我依次提出问题（最多 20 个问题），我也会依次回复。在这期间请完全保持真人面试官的口吻，比如适当引导学员、或者表达出你对学员回答的态度。\n" +
                "必须满足如下要求：\n" +
                "1. 当学员回复 “开始” 时，你要正式开始面试\n" +
                "2. 当学员表示希望 “结束面试” 时，你要结束面试\n" +
                "3. 此外，当你觉得这场面试可以结束时（比如候选人回答结果较差、不满足工作年限的招聘需求、或者候选人态度不礼貌），必须主动提出面试结束，不用继续询问更多问题了。并且要在回复中包含字符串【面试结束】\n" +
                "4. 面试结束后，应该给出候选人整场面试的表现和总结。", mockInterview.getWorkExperience(), mockInterview.getJobPosition(), mockInterview.getDifficulty());
        String userPrompt = "开始";

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);

        // 调用 AI 获取结果
        String answer = aiManager.startChat(messages);
        ChatMessage assistantMessage = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(answer).build();
        messages.add(assistantMessage);

        // 保存消息记录，并且更新状态
        List<MockInterviewChatMessage> chatMessageList = transformFromChatMessage(messages);
        String jsonStr = JSONUtil.toJsonStr(chatMessageList);

        // 操作数据库进行更新
        MockInterview updateMockInterview = new MockInterview();
        updateMockInterview.setStatus(MockInterviewStatusEnum.IN_PROGRESS.getValue());
        updateMockInterview.setId(mockInterview.getId());
        updateMockInterview.setMessages(jsonStr);
        boolean result = this.updateById(updateMockInterview);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return answer;
    }

    /**
     * 处理 AI 对话消息事件
     *
     * @param mockInterviewEventRequest
     * @param mockInterview
     * @return
     */
    private String handleChatMessageEvent(MockInterviewEventRequest mockInterviewEventRequest, MockInterview mockInterview) {
        String message = mockInterviewEventRequest.getMessage();

        // 构造消息列表，注意需要先获取之前的消息记录
        String historyMessage = mockInterview.getMessages();
        List<MockInterviewChatMessage> historyMessageList = JSONUtil.parseArray(historyMessage).toList(MockInterviewChatMessage.class);
        final List<ChatMessage> chatMessages = transformToChatMessage(historyMessageList);
        final ChatMessage chatUserMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
        chatMessages.add(chatUserMessage);

        // 调用 AI 获取结果
        String chatAnswer = aiManager.startChat(chatMessages);
        ChatMessage chatAssistantMessage = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(chatAnswer).build();
        chatMessages.add(chatAssistantMessage);

        // 保存消息记录，并且更新状态
        List<MockInterviewChatMessage> mockInterviewChatMessages = transformFromChatMessage(chatMessages);
        String newJsonStr = JSONUtil.toJsonStr(mockInterviewChatMessages);
        MockInterview newUpdateMockInterview = new MockInterview();
        newUpdateMockInterview.setId(mockInterview.getId());
        newUpdateMockInterview.setMessages(newJsonStr);

        // 如果 AI 主动结束了面试，更改状态
        if (chatAnswer.contains("【面试结束】")) {
            newUpdateMockInterview.setStatus(MockInterviewStatusEnum.ENDED.getValue());
        }
        boolean newResult = this.updateById(newUpdateMockInterview);
        ThrowUtils.throwIf(!newResult, ErrorCode.SYSTEM_ERROR, "更新失败");
        return chatAnswer;
    }


    /**
     * 处理 AI 对话结束事件
     *
     * @param mockInterview
     * @return
     */
    private String handleChatEndEvent(MockInterview mockInterview) {
        // 构造消息列表，注意需要先获取之前的消息记录
        String historyMessage = mockInterview.getMessages();
        List<MockInterviewChatMessage> historyMessageList = JSONUtil.parseArray(historyMessage).toList(MockInterviewChatMessage.class);
        final List<ChatMessage> chatMessages = transformToChatMessage(historyMessageList);

        // 构造用户结束消息
        String endUserPrompt = "结束";
        final ChatMessage endUserMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(endUserPrompt).build();
        chatMessages.add(endUserMessage);

        // 调用 AI 获取结果
        String endAnswer = aiManager.startChat(chatMessages);
        ChatMessage endAssistantMessage = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(endAnswer).build();
        chatMessages.add(endAssistantMessage);

        // 保存消息记录，并且更新状态
        List<MockInterviewChatMessage> mockInterviewChatMessages = transformFromChatMessage(chatMessages);
        String newJsonStr = JSONUtil.toJsonStr(mockInterviewChatMessages);
        MockInterview newUpdateMockInterview = new MockInterview();
        newUpdateMockInterview.setStatus(MockInterviewStatusEnum.ENDED.getValue());
        newUpdateMockInterview.setId(mockInterview.getId());
        newUpdateMockInterview.setMessages(newJsonStr);
        boolean newResult = this.updateById(newUpdateMockInterview);
        ThrowUtils.throwIf(!newResult, ErrorCode.SYSTEM_ERROR, "更新失败");
        return endAnswer;
    }

    /**
     * 消息记录对象转换
     *
     * @param chatMessageList
     * @return
     */
    List<MockInterviewChatMessage> transformFromChatMessage(List<ChatMessage> chatMessageList) {
        return chatMessageList.stream().map(chatMessage -> {
            MockInterviewChatMessage mockInterviewChatMessage = new MockInterviewChatMessage();
            mockInterviewChatMessage.setRole(chatMessage.getRole().value());
            mockInterviewChatMessage.setMessage(chatMessage.getContent().toString());
            return mockInterviewChatMessage;
        }).collect(Collectors.toList());
    }

    /**
     * 消息记录对象转换
     *
     * @param chatMessageList
     * @return
     */
    List<ChatMessage> transformToChatMessage(List<MockInterviewChatMessage> chatMessageList) {
        return chatMessageList.stream().map(chatMessage -> {
            ChatMessage tempChatMessage = ChatMessage.builder().role(ChatMessageRole.valueOf(StringUtils.upperCase(chatMessage.getRole())))
                    .content(chatMessage.getMessage()).build();
            return tempChatMessage;
        }).collect(Collectors.toList());
    }
}




