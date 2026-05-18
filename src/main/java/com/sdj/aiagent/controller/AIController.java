package com.sdj.aiagent.controller;

import com.sdj.aiagent.agent.MyManus;
import com.sdj.aiagent.app.CareerPlanningApp;
import com.sdj.aiagent.common.BaseResponse;
import com.sdj.aiagent.common.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * @author 沈德俊2022217204
 * localhost:8512/api/doc.html#/home
 */
@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private CareerPlanningApp careerPlanningApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

//    /**
//     * 同步调用AI职业规划
//     * @param message
//     * @param chatId
//     * @return
//     */
//    @GetMapping("/career_app/chat/sync")
//    public BaseResponse<String> doChatWithCareerAppSync(String message, String chatId) {
//        return ResultUtils.success(careerPlanningApp.doChat(message, chatId));
//    }

    /**
     * 流式调用AI职业规划
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/career_app/chat/sse")
    public SseEmitter doChatWithCareerAppSSE(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        careerPlanningApp.doChatBySSE(message,chatId)
                .subscribe(
                        mes -> {
                            try{
                                sseEmitter.send(mes);
                            }catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        } ,sseEmitter::completeWithError,sseEmitter::complete
                );
        return sseEmitter;
    }

    /**
     * 流式调用AI智能体
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message){
        MyManus myManus = new MyManus(allTools,dashscopeChatModel);
        return myManus.run(message);
    }
}