package com.txwang.knowwhy.manager;

import cn.hutool.core.collection.CollUtil;
import com.txwang.knowwhy.common.ErrorCode;
import com.txwang.knowwhy.exception.BusinessException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiManager {

    @Resource
    private ArkService aiService;

    private final String DEFAULT_SYSTEM_PROMPT = "你是一位专业的程序员面试官，我会给你一道面试题，请帮我生成详细的题解。要求如下：\n" +
            "\n" +
            "1. 题解的语句要自然流畅\n" +
            "2. 题解可以先给出总结性的回答，再详细解释\n" +
            "3. 要使用 Markdown 语法输出\n" +
            "\n" +
            "除此之外，请不要输出任何多余的内容，不要输出开头、也不要输出结尾，只输出题解。\n" +
            "\n" +
            "接下来我会给你要生成答案的面试题\n";

    public String startChat(String userPrompt) {
        return startChat(DEFAULT_SYSTEM_PROMPT, userPrompt);
    }

    public String startChat(List<ChatMessage> chatMessages) {
        // 创建聊天完成请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("deepseek-r1-250120")// 需要替换为Model ID
                .messages(chatMessages) // 设置消息列表
                .build();

        List<ChatCompletionChoice> choices = aiService.createChatCompletion(chatCompletionRequest)
                .getChoices();
        if (CollUtil.isEmpty(choices)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 调用失败");
        }
        return choices.get(0).getMessage().stringContent();
    }

    public String startChat(String systemPrompt, String userPrompt) {
        List<ChatMessage> chatMessages = new ArrayList<>();

        // 创建系统消息
        ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM) // 设置消息角色为系统
                .content(systemPrompt) // 设置消息内容
                .build();

        // 创建用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER) // 设置消息角色为用户
                .content(userPrompt) // 设置消息内容
                .build();

        // 将消息添加到消息列表
        chatMessages.add(systemMessage);
        chatMessages.add(userMessage);

        return startChat(chatMessages);
    }
}
