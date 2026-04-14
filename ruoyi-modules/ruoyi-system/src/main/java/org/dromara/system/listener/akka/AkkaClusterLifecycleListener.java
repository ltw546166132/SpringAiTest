package org.dromara.system.listener.akka;

import akka.actor.typed.ActorSystem;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AkkaClusterLifecycleListener {

    private final ActorSystem<Void> actorSystem;

    @PreDestroy
    public void onShutdown() {
        log.info("Shutting down Akka Actor System...");
        try {
            // 使用 Receptionist 替代 Redis 后，无需清理 Redis 中的路由信息
            // Actor 停止时会自动从 Receptionist 注销
            actorSystem.terminate();
            // 等待终止完成，最多5秒
            actorSystem.getWhenTerminated().toCompletableFuture().get(5, TimeUnit.SECONDS);
            log.info("Akka Actor System terminated successfully");
        } catch (Exception e) {
            log.error("Error terminating Akka Actor System", e);
        }
    }
}
