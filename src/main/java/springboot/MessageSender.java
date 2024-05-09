package springboot;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class MessageSender {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessageSender(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessage(String content) {
        Message message = new Message(content);
        messagingTemplate.convertAndSend("/topic/messages", message);
    }
}
