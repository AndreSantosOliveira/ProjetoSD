package springboot.websocket;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * The WebSocketConfig class is responsible for configuring the WebSocket message broker.
 * It is annotated with @Configuration and @EnableWebSocketMessageBroker, meaning it is a configuration class in the Spring framework and it enables WebSocket message broker.
 * It implements WebSocketMessageBrokerConfigurer, allowing it to override methods to configure the message broker.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker.
     * It enables a simple broker on the "/topic" destination prefix, and sets the application destination prefix to "/app".
     *
     * @param config the MessageBrokerRegistry to be configured
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers STOMP endpoints.
     * It adds an endpoint on "/admin-websocket" with SockJS fallback options.
     *
     * @param registry the StompEndpointRegistry to which the endpoint will be added
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/admin-websocket").withSockJS();
    }

}