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

@Service
public class RemoteServiceImpl extends UnicastRemoteObject implements MetodosRMIWebServerSocket {

    private final MessageSender messageSender;

    @Autowired
    protected RemoteServiceImpl(MessageSender messageSender) throws RemoteException {
        super();
        this.messageSender = messageSender;
    }

    @Override
    public void enviarAtualizacaoParaWebSockets(String msg) {
        messageSender.enviarAtualizacaoParaWebSockets(msg);
    }
}
