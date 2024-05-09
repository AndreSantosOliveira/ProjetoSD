package springboot.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class MessagingController {
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(Message message) {
        return new Message(HtmlUtils.htmlEscape(message.content()));
    }
}