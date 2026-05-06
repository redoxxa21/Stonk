package io.stonk.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
    private List<String> openPaths = List.of();

    public List<String> getOpenPaths() {
        return openPaths;
    }

    public void setOpenPaths(List<String> openPaths) {
        this.openPaths = openPaths;
    }
}
