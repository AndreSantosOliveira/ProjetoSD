package springboot;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public WSMessage greeting(WSMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new WSMessage("Hello, " + message.getContent() + "!");
    }
}