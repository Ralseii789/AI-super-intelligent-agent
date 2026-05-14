package com.sdj.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author 沈德俊2022217204
 * 职业规划向量数据库配置（初始化基于内存的向量数据库Bean）
 */
@Configuration
public class CareerPlaningVectorStoreConfig {

    @Resource
    private CareerPlaningDocumentLoader careerPlaningDocumentLoader;

//    @Bean
//    VectorStore careerVectorStroe(EmbeddingModel dashscopeEmbeddingModel){
//        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
//        List<Document> documents = careerPlaningDocumentLoader.loadMarkdowns();
//        simpleVectorStore.add(documents);
//        return simpleVectorStore;
//    }
}
