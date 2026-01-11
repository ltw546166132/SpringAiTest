package org.dromara.test;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.URLUtil;
import io.milvus.client.MilvusServiceClient;
import jakarta.annotation.Resource;
import org.dromara.common.core.utils.file.MimeTypeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.MimeType;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
public class SpringAiTest {
    @Resource
    private OpenAiChatModel openAiChatModel;
    @Resource
    private ChatClient openAiChatClient;
    @Resource
    private EmbeddingModel openAiEmbeddingModel;
    @Resource
    private MilvusVectorStore milvusVectorStore;

    @Test
    public void test() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        ChatClient.CallResponseSpec youAreAHelpfulAssistant = openAiChatClient.prompt().user(x -> x.media(MimeType.valueOf(MimeTypeUtils.IMAGE_JPEG), URLUtil.url("https://www.baidu.com"))).user("脑洞乌托邦最新一期节目名称是什么")
//            .advisors(QuestionAnswerAdvisor.builder(milvusVectorStore).searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(6).build()).build())
            .advisors(QuestionAnswerAdvisor.builder(milvusVectorStore).searchRequest(SearchRequest.builder().filterExpression(b.eq("document_id", 123).build()).build()).build())
            .advisors(x -> x.param(ChatMemory.CONVERSATION_ID, 77)).call();
        System.out.println(youAreAHelpfulAssistant.content());

//        String call = openAiChatModel.call(SystemMessage.builder().text("You are a helpful assistant.").build(), UserMessage.builder().text("What is the meaning of life?").build());
//        System.out.println(call);
    }

    @Test
    public void testVectorDatabase(){
//        milvusVectorStore.write(new TokenTextSplitter().transform(new TikaDocumentReader("rag/sample2.pdf").read()));
        Document build = Document.builder().text("脑洞乌托邦最新一期节目名称是 《倒计时30天！地质学家隐瞒了百年的真相: 为什么所有预言家都说7月会发生巨灾？地球已经历4次文明大清洗，第五次可能就在7月》").metadata("document_id", 123).build();
        List <Document> documents = List.of(
            new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("document_id", IdUtil.getSnowflakeNextId())),
            new Document("The World is Big and Salvation Lurks Around the Corner", Map.of("document_id", IdUtil.getSnowflakeNextId())),
            new Document("You walk forward facing the past and you turn back toward the future.", Map.of("document_id", IdUtil.getSnowflakeNextId())));
        milvusVectorStore.add(new TokenTextSplitter().transform(ListUtil.of(build)));
        milvusVectorStore.add(documents);
    }

    @Test
    public void searchVectorDatabase(){
        FilterExpressionBuilder search = new FilterExpressionBuilder();
        List<Document> results = milvusVectorStore.similaritySearch(SearchRequest.builder().filterExpression(search.eq("document_id", 123).build()).build());
        System.out.println(results);
    }

    @Test
    public void testdelete(){
        FilterExpressionBuilder search = new FilterExpressionBuilder();
        milvusVectorStore.delete("");
        Optional<MilvusServiceClient> nativeClient = milvusVectorStore.getNativeClient();

    }


}
