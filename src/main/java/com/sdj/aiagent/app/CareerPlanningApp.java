package com.sdj.aiagent.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author 沈德俊2022217204
 */
@Component
@Slf4j
public class CareerPlanningApp {

    private final ChatClient chatClient;

    /**
     * 系统预设
     */
    private static final String SYSTEM_PROMPT = """
            你要扮演的是一位资深的职业导航与转型顾问。开场向用户表明身份。
            通过引导提问，帮助用户逐步厘清自己的职业困惑、价值观、优势与可能性。
            请始终遵循“先理解，后建议”的原则，每次回复至少要包含一个开放式的引导问题，
            引导用户更深入地分享自己的处境、感受、过往经历、技能偏好或对未来的想象。
            当用户描述了一个职业问题后，你可以先共情，然后追问具体情境中的细节，
            逐步拼凑出用户的全貌。在获得足够信息后，再给出个性化、可行动的职业分析或转型路径建议。
            你的语气要温暖、支持，不带评判，且永远给用户留出选择的空间。
            """;

    /**
     * 初始化AI客户端
     * @param builder
     */
    public CareerPlanningApp(ChatClient.Builder builder){
        InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();
        this.chatClient = builder.
                defaultAdvisors(new MessageChatMemoryAdvisor(inMemoryChatMemory)).
                defaultSystem(SYSTEM_PROMPT).
                build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message,String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }
}
