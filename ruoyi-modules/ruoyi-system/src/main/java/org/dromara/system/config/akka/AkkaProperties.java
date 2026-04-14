package org.dromara.system.config.akka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "akka.cluster")
public class AkkaProperties {

    private String systemName = "ImClusterSystem";

    private int port = 25520;

    private String hostname = "127.0.0.1";

    private List<String> seedNodes;

    private String role = "im-node";

    private String nodeId;

}
