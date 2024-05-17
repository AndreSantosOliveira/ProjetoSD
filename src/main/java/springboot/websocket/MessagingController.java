package springboot.websocket;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.io.Serializable;

/**
 * The MessagingController class is responsible for handling messages in the application.
 * It uses the SimpMessagingTemplate to send messages to a topic.
 * This class is annotated with @Controller, meaning it is a controller in the Spring framework.
 * It implements Serializable, allowing its instances to be written to an OutputStream.
 */
@Controller
public class MessagingController implements Serializable {

    // The SimpMessagingTemplate used to send messages
    private final transient SimpMessagingTemplate messagingTemplate;

    /**
     * Constructs a new MessagingController with the provided SimpMessagingTemplate.
     *
     * @param messagingTemplate the SimpMessagingTemplate used to send messages
     */
    @Autowired
    public MessagingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles incoming messages.
     * It is mapped to the "/message" endpoint and sends the message to the "/topic/messages" topic.
     * The content of the message is escaped using HtmlUtils.htmlEscape.
     *
     * @param message the incoming message
     * @return a new Message with the escaped content
     */
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(Message message) {
        return new Message(HtmlUtils.htmlEscape(message.content()));
    }

    /**
     * Sends a message to all subscribers of the "/topic/messages" topic.
     *
     * @param message the message to be sent
     */
    public void sendMessageToAll(Message message) {
        messagingTemplate.convertAndSend("/topic/messages", message);
    }
}