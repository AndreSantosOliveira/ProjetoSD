package springboot.websocket;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import common.MetodosRMIWebServerSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The RemoteServiceImpl class is responsible for implementing the MetodosRMIWebServerSocket interface.
 * It extends UnicastRemoteObject, allowing its instances to be sent in RMI calls.
 * This class is annotated with @Service, meaning it is a service provider in the Spring framework.
 * It uses the MessageSender to send updates to WebSockets.
 */
@Service
public class RemoteServiceImpl extends UnicastRemoteObject implements MetodosRMIWebServerSocket {

    // The MessageSender used to send updates
    private final MessageSender messageSender;

    /**
     * Constructs a new RemoteServiceImpl with the provided MessageSender.
     *
     * @param messageSender the MessageSender used to send updates
     * @throws RemoteException if an RMI error occurs
     */
    @Autowired
    protected RemoteServiceImpl(MessageSender messageSender) throws RemoteException {
        super();
        this.messageSender = messageSender;
    }

    /**
     * Sends an update to WebSockets.
     * It uses the MessageSender to send the provided string as an update to all WebSockets.
     *
     * @param msg the update to be sent
     */
    @Override
    public void enviarAtualizacaoParaWebSockets(String msg) {
        messageSender.enviarAtualizacaoParaWebSockets(msg);
    }
}