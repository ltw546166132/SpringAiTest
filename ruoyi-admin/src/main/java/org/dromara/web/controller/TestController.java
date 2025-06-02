package org.dromara.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SaIgnore
@RestController
@RequestMapping(value = "/aiTest")
public class TestController {
    @Resource
    private ChatClient openAiChatClient;

    /**
     * 测试aiStream接口
     * @return
     */
    @GetMapping(value = "/chatAiStream", produces = "text/html;charset=UTF-8")
    public Flux<String> chatAiStream(){
        return openAiChatClient.prompt().system("你是一个java应用专家").user("请帮我写一个java类，类名为Test,类描述为测试类").stream().content();
    }
}
