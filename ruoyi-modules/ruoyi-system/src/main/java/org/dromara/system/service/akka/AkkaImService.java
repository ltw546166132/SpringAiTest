package org.dromara.system.service.akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.system.config.akka.AkkaProperties;
import org.dromara.system.domain.akka.ImMessage;
import org.dromara.system.service.akka.UserSessionActor.DeliverMessageCommand;
import org.dromara.system.service.akka.UserSessionActor.DeliveryAck;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AkkaImService {

    private final ActorSystem<Void> actorSystem;

    private final AkkaProperties akkaProperties;

    private final ConcurrentMap<String, ActorRef<UserSessionActor.Command>> localActors = new ConcurrentHashMap<>();

    public CompletionStage<DeliveryAck> sendMessage(String fromUserId, String toUserId, String content) {
        ImMessage imMessage = new ImMessage(
            UUID.randomUUID().toString(), fromUserId, toUserId, content);

        // 使用 Receptionist 查询目标 Actor
        return findAndSendToActor(imMessage);
    }

    public void registerLocalSession(String userId, ActorRef<?> sessionRef) {
        ActorRef<UserSessionActor.Command> actor = localActors.computeIfAbsent(userId, id -> {
            ActorRef<UserSessionActor.Command> ref = actorSystem.systemActorOf(
                UserSessionActor.create(id), "user-session-" + id, Props.empty());
            log.info("Created local UserSessionActor for userId: {}", id);
            return ref;
        });

        actor.tell(new UserSessionActor.RegisterSessionCommand(userId, sessionRef));
        log.info("Registered local session for userId: {} on node: {}", userId, getNodeAddress());
    }

    public void unregisterLocalSession(String userId) {
        ActorRef<UserSessionActor.Command> actor = localActors.remove(userId);
        if (actor != null) {
            // 发送停止命令给 Actor，Actor 内部处理清理后自行停止
            actor.tell(new UserSessionActor.StopCommand());
            log.info("Sent stop command to UserSessionActor for userId: {}", userId);
        }
    }

    private CompletionStage<DeliveryAck> findAndSendToActor(ImMessage imMessage) {
        String toUserId = imMessage.getToUserId();
        ServiceKey<UserSessionActor.Command> key = UserSessionActor.serviceKeyFor(toUserId);

        // 先检查本地缓存
        ActorRef<UserSessionActor.Command> localActor = localActors.get(toUserId);
        if (localActor != null) {
            log.info("Found local actor for userId: {}", toUserId);
            return sendToActor(localActor, imMessage);
        }

        // 通过 Receptionist 查询集群中的 Actor
        log.info("Querying Receptionist for userId: {}", toUserId);
        
        CompletionStage<Receptionist.Listing> listingFuture = AskPattern.ask(
            actorSystem.receptionist(),
            (ActorRef<Receptionist.Listing> replyTo) -> Receptionist.find(key, replyTo),
            Duration.ofSeconds(3),
            actorSystem.scheduler()
        );

        return listingFuture.thenCompose(listing -> {
            ActorRef<UserSessionActor.Command> targetActor = null;
            for (ActorRef<UserSessionActor.Command> ref : listing.getServiceInstances(key)) {
                targetActor = ref;
                break; // 取第一个
            }

            if (targetActor == null) {
                log.warn("User {} is not online (no actor found in cluster)", toUserId);
                return CompletableFuture.completedFuture(
                    new DeliveryAck(imMessage.getMessageId(), toUserId, false));
            }

            log.info("Found remote actor for userId: {} at {}", toUserId, targetActor.path());
            return sendToActor(targetActor, imMessage);
        }).exceptionally(ex -> {
            log.error("Failed to find actor for userId: {}", toUserId, ex);
            return new DeliveryAck(imMessage.getMessageId(), toUserId, false);
        });
    }

    private CompletionStage<DeliveryAck> sendToActor(ActorRef<UserSessionActor.Command> actor, ImMessage imMessage) {
        return AskPattern.ask(
            actor,
            replyTo -> new DeliverMessageCommand(imMessage, replyTo),
            Duration.ofSeconds(5),
            actorSystem.scheduler()
        );
    }

    private String getNodeAddress() {
        return akkaProperties.getHostname() + ":" + akkaProperties.getPort();
    }
}
