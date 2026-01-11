package org.dromara.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.servlet.ServletUtil;
import jakarta.annotation.Resource;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.redis.utils.RedisUtils;
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
        StringBuilder sb = new StringBuilder();
        return openAiChatClient.prompt().system("你是一个java应用专家").user("请帮我写一个java类，类名为Test,类描述为测试类").stream().content().doOnSubscribe(__ -> System.out.println("Stream subscribed. Preparing to send request to AI."))
            .doOnNext(sb::append).doOnComplete(() -> System.out.println("sb = " + sb)).doOnError(throwable -> System.out.println("throwable = " + throwable));
    }

    @GetMapping(value = "/testRedis")
    public R<Integer> testRedis(){
        ServletUtils.getHeaders(ServletUtils.getRequest());
//        Integer test = RedisUtils.getCacheObject("test");
        return R.ok(321);
    }
}
