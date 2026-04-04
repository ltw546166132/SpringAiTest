package org.dromara.system.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.dromara.system.node.SentenceConstructionNode;
import org.dromara.system.node.TranslationNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphConfig {

    @Bean("simpleGraph")
    public CompiledGraph simpleGraph() throws GraphStateException {
        KeyStrategyFactoryBuilder keyStrategyFactoryBuilder = new KeyStrategyFactoryBuilder();
        keyStrategyFactoryBuilder.addStrategy("words", new ReplaceStrategy());
        KeyStrategyFactory keyStrategyFactory = keyStrategyFactoryBuilder.build();
        StateGraph stateGraph = new StateGraph(keyStrategyFactory);
        stateGraph.addNode("sentenceConstructionNode", AsyncNodeAction.node_async(new SentenceConstructionNode()));
        stateGraph.addNode("translation", AsyncNodeAction.node_async(new TranslationNode()));
        stateGraph.addEdge(StateGraph.START, "sentenceConstructionNode");
        stateGraph.addEdge("sentenceConstructionNode", "translation");
        stateGraph.addEdge("translation", StateGraph.END);
        return stateGraph.compile();
    }
}
