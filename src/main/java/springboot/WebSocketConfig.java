package springboot;

import common.ConnectionsEnum;
import common.MetodosRMIGateway;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.rmi.Naming;
import java.rmi.RemoteException;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
@Override
public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    MetodosRMIGateway metodosGateway = null;

    try {
        // Invoke the search method on the remote Gateway service
        metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");

    } catch (Exception e) {
        // Handle any exceptions
        e.printStackTrace();
    }

    WebSocketHandlerCaseiro wsc = new WebSocketHandlerCaseiro();
registry.addHandler(wsc, "/websocket")
.setAllowedOrigins("*");

    //1 second loop to send messages to all clients
    MetodosRMIGateway finalMetodosGateway = metodosGateway;
    new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(1000);
                if (finalMetodosGateway != null) {
                    wsc.sendMessageToAll(finalMetodosGateway.getAdministrativeStatistics());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }).start();
}
}