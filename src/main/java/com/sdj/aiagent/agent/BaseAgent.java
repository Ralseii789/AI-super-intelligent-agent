package com.sdj.aiagent.agent;

import ch.qos.logback.core.util.StringUtil;
import cn.hutool.core.util.StrUtil;
import com.sdj.aiagent.agent.model.AgentState;
import com.sdj.aiagent.exception.ErrorCode;
import com.sdj.aiagent.exception.ThrowUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 沈德俊2022217204
 * 抽象基础代理类，用于管理代理状态和执行流程
 *
 * 提供状态转换，内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现step方法
 */
@Data
@Slf4j
public abstract class BaseAgent {

    //核心属性
    private String name;

    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //代理状态
    private AgentState state = AgentState.IDLE;

    //执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 7;

    //大模型
    private ChatClient chatClient;

    //Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 执行代理（智能体）
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt){
        //基础校验
        ThrowUtils.throwIf(this.state != AgentState.IDLE,
                ErrorCode.FORBIDDEN_ERROR,
                "Cannot run agent from state: "+this.state);
        ThrowUtils.throwIf(StrUtil.isBlank(userPrompt),
                ErrorCode.FORBIDDEN_ERROR,
                "Cannot run with empty user prompt");
        //执行，更改状态
        this.state = AgentState.RUNNING;
        //记录上下文
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> resultsList = new ArrayList<>();
        try {
            //执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i+1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}",stepNumber,maxSteps);
                //单步执行
                String gettedResult = step();
                String result = "Step "+stepNumber+": "+gettedResult;
                resultsList.add(result);
            }
            //检查是否超出步骤限制
            if(currentStep>=maxSteps){
                state = AgentState.FINISHED;
                resultsList.add("Terminated: Reached max steps ("+maxSteps+")");
            }
            return String.join("\n",resultsList);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent",e);
            return "执行错误" + e.getMessage();
        } finally {
            //清理资源
            this.cleanup();
        }
    }

    /**
     * 定义单个步骤，交给子类实现
     * @return
     */
    public abstract  String step();

    /**
     * 清理资源
     */
    protected void cleanup(){

    }
}
