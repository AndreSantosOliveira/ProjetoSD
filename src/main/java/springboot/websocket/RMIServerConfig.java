package springboot.websocket;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import common.ConnectionsEnum;
import common.MetodosRMIWebServerSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Configuration
public class RMIServerConfig {

    @Autowired
    private MessageSender messageSender;

    @Bean
    public MetodosRMIWebServerSocket remoteService() throws RemoteException {
        return new RemoteServiceImpl(messageSender);
    }

    @Bean
    public RmiServiceExporter rmiServiceExporter(MetodosRMIWebServerSocket remoteService) {
        try {
            // dar unexport se ja existir o objeto
            UnicastRemoteObject.unexportObject(remoteService, true);
        } catch (java.rmi.NoSuchObjectException e) {
        }

        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("websocketrmi");
        exporter.setService(remoteService);
        exporter.setServiceInterface(MetodosRMIWebServerSocket.class);
        exporter.setRegistryPort(ConnectionsEnum.WEBSERVER_SOCKET_RMI.getPort());

        System.out.println("RMI WebSocket Server started on port " + ConnectionsEnum.WEBSERVER_SOCKET_RMI.getPort());

        return exporter;
    }
}
