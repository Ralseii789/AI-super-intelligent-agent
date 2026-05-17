package com.sdj.aiagent.agent.model;

/**
 * @author 沈德俊2022217204
 * 代理执行状态的枚举类
 */
public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */

    ERROR
}
