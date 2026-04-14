package org.dromara.system.controller.akka;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.system.service.akka.AkkaImService;
import org.dromara.system.service.akka.UserSessionActor.DeliveryAck;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/akka/im")
public class AkkaImController {

    private final AkkaImService akkaImService;

    @Log(title = "Akka IM消息发送", businessType = BusinessType.OTHER)
    @PostMapping("/send")
    public R<String> sendMessage(@Validated @RequestBody ImMessageRequest request) {
        CompletableFuture<DeliveryAck> future = akkaImService
            .sendMessage(request.fromUserId(), request.toUserId(), request.content())
            .toCompletableFuture();

        try {
            DeliveryAck ack = future.get();
            if (ack.success()) {
                return R.ok("消息已投递，messageId: " + ack.messageId());
            } else {
                return R.fail("消息投递失败，用户 " + ack.toUserId() + " 不在线");
            }
        } catch (Exception e) {
            return R.fail("消息发送失败: " + e.getMessage());
        }
    }

    public record ImMessageRequest(
        @NotBlank(message = "发送者用户ID不能为空")
        String fromUserId,
        @NotBlank(message = "接收者用户ID不能为空")
        String toUserId,
        @NotBlank(message = "消息内容不能为空")
        String content
    ) {}
}
