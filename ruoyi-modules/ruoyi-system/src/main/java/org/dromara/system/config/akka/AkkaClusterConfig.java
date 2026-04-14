package org.dromara.system.config.akka;

import akka.actor.typed.ActorSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AkkaClusterConfig {

    @Bean(destroyMethod = "terminate")
    public ActorSystem<Void> actorSystem(AkkaProperties akkaProperties) {
        String configString = buildConfig(akkaProperties);
        log.info("Starting Akka Actor System: {}, config:\n{}", akkaProperties.getSystemName(), configString);

        ActorSystem<Void> system = ActorSystem.create(
            akka.actor.typed.javadsl.Behaviors.empty(),
            akkaProperties.getSystemName(),
            com.typesafe.config.ConfigFactory.parseString(configString)
        );

        log.info("Akka Actor System started at: {}", system.address());
        return system;
    }

    private String buildConfig(AkkaProperties properties) {
        StringBuilder sb = new StringBuilder();
        sb.append("akka {\n");
        sb.append("  actor {\n");
        sb.append("    provider = cluster\n");
        sb.append("    serialization-bindings {\n");
        sb.append("      \"org.dromara.system.domain.akka.ImMessage\" = jackson-json\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  remote {\n");
        sb.append("    artery {\n");
        sb.append("      canonical {\n");
        sb.append("        hostname = \"").append(properties.getHostname()).append("\"\n");
        sb.append("        port = ").append(properties.getPort()).append("\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  cluster {\n");
        sb.append("    seed-nodes = [");
        if (properties.getSeedNodes() != null && !properties.getSeedNodes().isEmpty()) {
            for (int i = 0; i < properties.getSeedNodes().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("\"").append(properties.getSeedNodes().get(i)).append("\"");
            }
        } else {
            sb.append("\"akka://").append(properties.getSystemName()).append("@")
              .append(properties.getHostname()).append(":").append(properties.getPort()).append("\"");
        }
        sb.append("]\n");
        sb.append("    roles = [\"").append(properties.getRole()).append("\"]\n");
        sb.append("    downing-provider-class = \"akka.cluster.sbr.SplitBrainResolverProvider\"\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }
}
