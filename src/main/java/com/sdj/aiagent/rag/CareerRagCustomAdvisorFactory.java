package com.sdj.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * @author 沈德俊2022217204
 * 创建自定义的RAG检索增强顾问的工厂
 */
public class CareerRagCustomAdvisorFactory {

    /**
     * 创建自定义的RAG检索增强顾问
     * @param vectorStore
     * @param status
     * @return
     */
    public static Advisor createCareerAppRagCustomAdvisor(VectorStore vectorStore,String status){
        //过滤特定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        VectorStoreDocumentRetriever vectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) //过滤条件
                .similarityThreshold(0.5)   //相似度阈值
                .topK(3)  //返回文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(vectorStoreDocumentRetriever)
                .queryAugmenter(CareerAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
