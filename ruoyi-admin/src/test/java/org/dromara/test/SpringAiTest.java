package org.dromara.test;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringAiTest {
    @Resource
    private OpenAiChatModel openAiChatModel;
    @Resource
    private ChatClient openAiChatClient;

    @Test
    public void test() {
        ChatClient.CallResponseSpec youAreAHelpfulAssistant = openAiChatClient.prompt("你是一个java应用专家").user("java类加载过程").advisors(x -> x.param(ChatMemory.CONVERSATION_ID, 77)).call();
        String content = youAreAHelpfulAssistant.content();
        System.out.println(content);
//        String call = openAiChatModel.call(SystemMessage.builder().text("You are a helpful assistant.").build(), UserMessage.builder().text("What is the meaning of life?").build());
//        System.out.println(call);
    }


}
