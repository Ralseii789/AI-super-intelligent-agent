package com.sdj.aiagent.app;

import com.sdj.aiagent.advisor.MyLoggerAdvisor;
import com.sdj.aiagent.advisor.ReReadingAdvisor;
import com.sdj.aiagent.chatmemory.FileBasedChatMemory;
import com.sdj.aiagent.rag.CareerRagCustomAdvisorFactory;
import com.sdj.aiagent.rag.QueryRewriter;
import com.sdj.aiagent.tools.ToolRegistration;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * @author 沈德俊2022217204
 */
@Component
@Slf4j
public class CareerPlanningApp {

    private final ChatClient chatClient;

    //RAG知识库
    @Resource(name = "careerVectorStroe")
    private VectorStore careerVectorStroe;

    @Resource
    private Advisor careerPlanningRagCloudAdvisor;

//    @Resource
//    //private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 系统预设
     */
    private static final String SYSTEM_PROMPT = """
            你要扮演的是一位资深的职业导航与转型顾问。开场向用户表明身份。
            通过引导提问，帮助用户逐步厘清自己的职业困惑、价值观、优势与可能性。
            请始终遵循“先理解，后建议”的原则，每次回复至少要包含一个开放式的引导问题，
            引导用户更深入地分享自己的处境、感受、过往经历、技能偏好或对未来的想象，
            给出个性化、可行动的职业分析或转型路径建议。
            """;

    /**
     * 初始化AI客户端
     * @param
     */
    public CareerPlanningApp(ChatModel dashscopeChatModel){
        //初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir")+"/tmp/chat-memory";
//        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        //基于内存的记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();

    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message,String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
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
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
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
        //查询重写
        //String doQueryRewrite = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                //应用RAG知识库进行问答
                .advisors(new QuestionAnswerAdvisor(careerVectorStroe))
                //检索增强服务
                //.advisors(careerPlanningRagCloudAdvisor)
                //基于PGvector的检索增强服务
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                //应用自定义RAG检索增强服务（文档查询器+上下文增强器）
                //.advisors(CareerRagCustomAdvisorFactory.createCareerAppRagCustomAdvisor(careerVectorStroe,"在职"))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    /**
     * AI 工具调用
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message,String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                //开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    /**
     * AI MCP调用
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMCP(String message,String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                //开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("Tool Callbacks: {}", Arrays.toString(toolCallbackProvider.getToolCallbacks()));
        log.info("content: {}",content);
        return content;
    }
}
