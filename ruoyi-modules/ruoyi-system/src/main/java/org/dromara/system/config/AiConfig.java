package org.dromara.system.config;

import jakarta.annotation.Resource;
import org.dromara.common.core.utils.DateUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AiConfig {
    @Resource
    private ChatModel openAiChatModel;

    @Resource
    private EmbeddingModel openAiEmbeddingModel;

    @Resource
    private JdbcChatMemoryRepository chatMemoryRepository;



    @Bean
    public ChatMemory mySqlChatMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(20)
            .build();
    }

    @Bean
    public ChatClient openAiChatClient(ChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).defaultSystem("当前北京时间是"+ DateUtils.getTime()).defaultAdvisors(MessageChatMemoryAdvisor.builder(mySqlChatMemory()).build(), SafeGuardAdvisor.builder().sensitiveWords(List.of("敏感词")).build()).build();
    }


}
