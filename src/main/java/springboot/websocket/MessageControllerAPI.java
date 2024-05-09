package springboot.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MessageControllerAPI {

    private final MessageSender messageSender;

    @Autowired
    public MessageControllerAPI(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @PostMapping("/send-message")
    public void sendMessage(@RequestBody String content) {
        messageSender.sendMessage(content);
    }
}