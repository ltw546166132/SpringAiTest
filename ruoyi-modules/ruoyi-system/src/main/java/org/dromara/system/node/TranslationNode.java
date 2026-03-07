package org.dromara.system.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;
import java.util.Optional;

public class TranslationNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        Optional<String> sentence = state.value("sentence", String.class);
        if (sentence.isPresent()) {
            ChatClient openAiChatClient = SpringUtils.getBean("openAiChatClient");
            PromptTemplate promptTemplate = PromptTemplate.builder().template("你是一个翻译专家，能够将给定的句子翻译成英文，要求只返回翻译结果，不要返回其他内容。给定的句子是：{sentence}").build();
            promptTemplate.add("sentence", sentence.get());
            String content = openAiChatClient.prompt().user(promptTemplate.render()).call().content();
            return Map.of("translation", content);
        }
        return Map.of();
    }
}
