package common;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.io.IOException;
import java.rmi.Remote;

/**
 * The MetodosRMIWebServerSocket interface extends the Remote interface.
 * This interface defines the methods that a WebServerSocket object must implement.
 * These methods allow the WebServerSocket to send updates to WebSockets.
 */
public interface MetodosRMIWebServerSocket extends Remote {

    /**
     * Sends an update to WebSockets.
     *
     * @param msg the message to be sent
     * @throws IOException if an error occurs during the operation.
     */
    void enviarAtualizacaoParaWebSockets(String msg) throws IOException;

}