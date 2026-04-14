package org.dromara.system.domain.akka;

import java.io.Serial;
import java.io.Serializable;

public class ImMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String messageId;

    private String fromUserId;

    private String toUserId;

    private String content;

    private long timestamp;

    public ImMessage() {
    }

    public ImMessage(String messageId, String fromUserId, String toUserId, String content) {
        this.messageId = messageId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ImMessage{messageId='" + messageId + "', fromUserId='" + fromUserId +
               "', toUserId='" + toUserId + "', content='" + content +
               "', timestamp=" + timestamp + "}";
    }
}
