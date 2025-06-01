package org.dromara.test;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringAiTest {
    @Resource
    private OpenAiChatModel openAiChatModel;

    @Test
    public void test() {
        String call = openAiChatModel.call(SystemMessage.builder().text("You are a helpful assistant.").build(), UserMessage.builder().text("What is the meaning of life?").build());
        System.out.println(call);
    }
}
