package com.sdj.aiagent.app;

import com.sdj.aiagent.advisor.MyLoggerAdvisor;
import com.sdj.aiagent.advisor.ReReadingAdvisor;
import com.sdj.aiagent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author 沈德俊2022217204
 */
@Component
@Slf4j
public class CareerPlanningApp {

    private final ChatClient chatClient;

    //RAG知识库
    @Resource
    private VectorStore careerVectorStroe;

    /**
     * 系统预设
     */
    private static final String SYSTEM_PROMPT = """
            你要扮演的是一位资深的职业导航与转型顾问。开场向用户表明身份。
            通过引导提问，帮助用户逐步厘清自己的职业困惑、价值观、优势与可能性。
            请始终遵循“先理解，后建议”的原则，每次回复至少要包含一个开放式的引导问题，
            引导用户更深入地分享自己的处境、感受、过往经历、技能偏好或对未来的想象，
            给出个性化、可行动的职业分析或转型路径建议。
            如果有对应的课程要向用户推荐。
            """;

    /**
     * 初始化AI客户端
     * @param builder
     */
    public CareerPlanningApp(ChatClient.Builder builder){
        //初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir")+"/tmp/chat-memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        //InMemoryChatMemory chatMemory = new InMemoryChatMemory();
        this.chatClient = builder.
                defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志Advisor 可按需开启
                        new MyLoggerAdvisor()
                        //自定义推理增强Advisor 可按需开启
                        //new ReReadingAdvisor()
                ).
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

    record CareerReoprt(String title, List<String> suggestions){}
    /**
     * AI 职业规划报告（实战结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public CareerReoprt doChatWithReport(String message,String chatId){
        CareerReoprt careerReoprt = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成职业规划结果，标题为{用户名}的职业规划报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(CareerReoprt.class);
        log.info("careerReoprt: {}",careerReoprt);
        return careerReoprt;
    }

    /**
     * 和 RAG 知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message,String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //应用RAG知识库进行问答
                .advisors(new QuestionAnswerAdvisor(careerVectorStroe))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }
}
