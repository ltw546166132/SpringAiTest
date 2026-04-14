package org.dromara.system.service.akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.dromara.system.domain.akka.ImMessage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserSessionActor extends AbstractBehavior<UserSessionActor.Command> {

    public interface Command {}

    public record DeliverMessageCommand(ImMessage message, ActorRef<DeliveryAck> replyTo) implements Command {}

    public record RegisterSessionCommand(String userId, ActorRef<?> sessionRef) implements Command {}

    public record UnregisterSessionCommand(String userId) implements Command {}

    public record StopCommand() implements Command {}

    public record DeliveryAck(String messageId, String toUserId, boolean success) {
        @JsonCreator
        public DeliveryAck(
            @JsonProperty("messageId") String messageId,
            @JsonProperty("toUserId") String toUserId,
            @JsonProperty("success") boolean success) {
            this.messageId = messageId;
            this.toUserId = toUserId;
            this.success = success;
        }
    }

    // 为每个 userId 创建唯一的 ServiceKey
    public static ServiceKey<Command> serviceKeyFor(String userId) {
        return ServiceKey.create(Command.class, "user-session-" + userId);
    }

    private final String userId;
    private final Map<String, ActorRef<?>> localSessions = new HashMap<>();

    public static Behavior<Command> create(String userId) {
        return Behaviors.setup(context -> {
            // 注册到 Receptionist，让集群其他节点可以发现这个 Actor
            ServiceKey<Command> key = serviceKeyFor(userId);
            context.getSystem().receptionist()
                .tell(Receptionist.register(key, context.getSelf()));
            log.info("Registered UserSessionActor for userId: {} with key: {}", userId, key.id());
            return new UserSessionActor(context, userId);
        });
    }

    private UserSessionActor(ActorContext<Command> context, String userId) {
        super(context);
        this.userId = userId;
        context.getLog().info("UserSessionActor started for userId: {} on node: {}", 
            userId, context.getSystem().address());
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(DeliverMessageCommand.class, this::onDeliverMessage)
            .onMessage(RegisterSessionCommand.class, this::onRegisterSession)
            .onMessage(UnregisterSessionCommand.class, this::onUnregisterSession)
            .onMessage(StopCommand.class, this::onStop)
            .build();
    }

    private Behavior<Command> onDeliverMessage(DeliverMessageCommand cmd) {
        ImMessage msg = cmd.message();
        log.info("Delivering message to userId: {}, messageId: {}, content: {}",
            msg.getToUserId(), msg.getMessageId(), msg.getContent());

        ActorRef<?> sessionRef = localSessions.get(msg.getToUserId());
        if (sessionRef != null) {
            log.info("User {} is connected locally, delivering message", msg.getToUserId());
        } else {
            log.info("User {} is NOT connected locally", msg.getToUserId());
        }
        cmd.replyTo().tell(new DeliveryAck(msg.getMessageId(), msg.getToUserId(), sessionRef != null));
        return this;
    }

    private Behavior<Command> onRegisterSession(RegisterSessionCommand cmd) {
        log.info("Registering local session for userId: {}", cmd.userId());
        localSessions.put(cmd.userId(), cmd.sessionRef());
        return this;
    }

    private Behavior<Command> onUnregisterSession(UnregisterSessionCommand cmd) {
        log.info("Unregistering local session for userId: {}", cmd.userId());
        localSessions.remove(cmd.userId());
        return this;
    }

    private Behavior<Command> onStop(StopCommand cmd) {
        log.info("Stopping UserSessionActor for userId: {}", userId);
        // 清理本地会话
        localSessions.clear();
        // 从 Receptionist 注销（Actor 停止时会自动注销，但显式处理更清晰）
        getContext().getSystem().receptionist()
            .tell(Receptionist.deregister(serviceKeyFor(userId), getContext().getSelf()));
        // 返回 stopped 行为，Actor 将停止
        return Behaviors.stopped();
    }
}
