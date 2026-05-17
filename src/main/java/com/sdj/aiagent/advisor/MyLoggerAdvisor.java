//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sdj.aiagent.advisor;

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.model.ModelOptionsUtils;
import reactor.core.publisher.Flux;

/**
 * 自定义日志Advisor
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientRequest before(ChatClientRequest request) {
        log.info("AI Request: {}",request.prompt());
        return request;
    }

    private void observeAfter(ChatClientResponse advisedResponse) {
        log.info("AI Response: {}",advisedResponse.chatResponse().getResult().getOutput().getText());
    }


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest advisedRequest, CallAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        ChatClientResponse advisedResponse = chain.nextCall(advisedRequest);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest advisedRequest, StreamAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        Flux<ChatClientResponse> advisedResponses = chain.nextStream(advisedRequest);
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(advisedResponses, this::observeAfter);
    }

}
