package com.zufang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理前缀，客户端订阅时需要加上这个前缀
        registry.enableSimpleBroker("/topic", "/user");
        // 配置客户端发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 配置点对点消息前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个STOMP端点，客户端通过这个端点进行连接
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
} 