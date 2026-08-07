package com.pages.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/wss","/ws")
                // Safely allows your local server, your current frontend, and any potential backup subdomains
                .setAllowedOriginPatterns(
                        "http://localhost:[*]",
                        "https://pages-production-6b7d.up.railway.app",
                        "https://pages-api-production-88b2.up.railway.app"
                );
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefixes for routing messages from client to server
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for topics clients subscribe to for listening
        registry.enableSimpleBroker("/topic");
    }

    // Add this method to ensure Spring Security context passes seamlessly to WebSocket controllers
    @Override
    public boolean configureMessageConverters(java.util.List<org.springframework.messaging.converter.MessageConverter> messageConverters) {
        return true;
    }
}
