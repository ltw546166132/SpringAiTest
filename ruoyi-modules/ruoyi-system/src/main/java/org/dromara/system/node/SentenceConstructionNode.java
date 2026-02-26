package org.dromara.system.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import java.util.Map;
import java.util.Optional;

public class SentenceConstructionNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        Optional<String> words = state.value("words", String.class);
        if (words.isPresent()) {
            ChatClient openAiChatClient = SpringUtils.getBean("openAiChatClient");
            PromptTemplate promptTemplate = PromptTemplate.builder().template("你是一个英语造句专家，能够根据给定的单词造一个句子，要求只返回造好的句子，不要返回其他内容。给定的单词是：{words}").build();
            promptTemplate.add("words", words.get());
            String content = openAiChatClient.prompt().user(promptTemplate.render()).call().content();
            return Map.of("sentence", content);
        }
        return Map.of();
    }
}
