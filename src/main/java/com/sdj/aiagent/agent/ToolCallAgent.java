package com.sdj.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.sdj.aiagent.agent.model.AgentState;
import com.sdj.aiagent.exception.ThrowUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 沈德俊2022217204
 * 处理工具调用的基础代理类，具体实现了think和act方法，可以用作创建实例的父类
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent{

    //可用的工具
    private final ToolCallback[] availableTools;

    //保存工具调用信息的响应结果（要调用哪些工具）
    private ChatResponse toolCallChatResponse;

    //工具调用管理者
    private final ToolCallingManager toolCallingManager;

    //禁用Spring AI内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        //校验提示词，拼接用户提示词
        if(StrUtil.isNotBlank(getNextStepPrompt())){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        //调用AI大模型，获取工具调用结果
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            //解析工具调用结果，获取要调用的工具
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            String result = assistantMessage.getText();
            log.info(getName()+"的思考: "+result);
            log.info(getName()+"选择了"+toolCalls.size()+"个工具来使用");
            String toolCallInfo = toolCalls.stream().map(toolCall -> String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            //不需要调用工具，返回false
            if(toolCalls.isEmpty()){
                getMessageList().add(assistantMessage);
                return false;
            }else {
                //需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName()+"的思考过程遇到了问题：{}",e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了问题："+e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()){
            return "没有工具需要调用";
        }
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录消息上下文，conversationHistory已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        //判断是否调用了终止工具
        boolean doTerminate = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if(doTerminate){
            setState(AgentState.FINISHED);
        }
        String result = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具 " + toolResponse.name() + "返回的结果: " + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        log.info(result);
        return result;
    }
}
