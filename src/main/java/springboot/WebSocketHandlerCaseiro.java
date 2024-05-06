package springboot;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WebSocketHandlerCaseiro extends TextWebSocketHandler {
    private static Set<WebSocketSession> sessions = new HashSet<>();

    @Override
public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
// Handle incoming messages here
String receivedMessage = (String) message.getPayload();
// Process the message and send a response if needed
session.sendMessage(new TextMessage("Received: " + receivedMessage));
}
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Add the new session to the set of active sessions
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Remove the closed session from the set of active sessions
        sessions.remove(session);
    }

    public void sendMessageToAll(String message) {
        // Iterate through all sessions and send a message to each one
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}