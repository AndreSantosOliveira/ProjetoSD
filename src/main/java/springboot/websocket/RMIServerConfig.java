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
import org.springframework.web.client.RestTemplate;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The RMIServerConfig class is responsible for configuring the RMI server.
 * It is annotated with @Configuration, meaning it is a configuration class in the Spring framework.
 * It uses the MessageSender and the RemoteServiceImpl to provide the RMI service.
 */
@Configuration
public class RMIServerConfig {

    // The MessageSender used to send messages
    @Autowired
    private MessageSender messageSender;

    /**
     * Provides the RemoteServiceImpl as a bean.
     * It constructs a new RemoteServiceImpl with the MessageSender.
     *
     * @return a new RemoteServiceImpl
     * @throws RemoteException if an RMI error occurs
     */
    @Bean
    public MetodosRMIWebServerSocket remoteService() throws RemoteException {
        return new RemoteServiceImpl(messageSender);
    }

    /**
     * Provides a new RestTemplate as a bean.
     *
     * @return a new RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configures and provides the RMI service.
     * It unexports the remote service if it already exists, and then sets up a new RmiServiceExporter.
     * The service name is set to "websocketrmi", and the service interface is set to MetodosRMIWebServerSocket.
     * The registry port is set to the port of WEBSERVER_SOCKET_RMI in ConnectionsEnum.
     *
     * @param remoteService the remote service to be exported
     * @return a new RmiServiceExporter
     */
    @Bean
    public RmiServiceExporter rmiServiceExporter(MetodosRMIWebServerSocket remoteService) {
        try {
            // unexport the object if it already exists
            UnicastRemoteObject.unexportObject(remoteService, true);
        } catch (java.rmi.NoSuchObjectException ignored) {
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