package com.minicloud.client.network;

/**
 * Server Configuration for MiniDrive Client.
 * 
 * Supports dynamic server endpoint configuration:
 * 1. Java System Property: -Dminicloud.server.host=192.168.1.150 -Dminicloud.server.port=8080
 * 2. Environment Variable: MINICLOUD_SERVER_HOST=192.168.1.150 MINICLOUD_SERVER_PORT=8080
 * 3. Default fallback: localhost:8080 (Single-machine local development)
 */
public class ServerConfig {

    public static String getHost() {
        String prop = System.getProperty("minicloud.server.host");
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }
        String env = System.getenv("MINICLOUD_SERVER_HOST");
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        return "localhost";
    }

    public static String getPort() {
        String prop = System.getProperty("minicloud.server.port");
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }
        String env = System.getenv("MINICLOUD_SERVER_PORT");
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        return "8080";
    }

    public static String getBaseUrl() {
        return "http://" + getHost() + ":" + getPort();
    }
}
