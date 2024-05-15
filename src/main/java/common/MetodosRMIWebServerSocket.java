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
 * common.MetodosRMIGateway interface extends Remote.
 * This interface defines the methods that a Gateway object must implement.
 * These methods allow the Gateway to index a URL, search for URLs based on input, and list indexed pages.
 */
public interface MetodosRMIWebServerSocket extends Remote {

    void enviarAtualizacaoParaWebSockets(String msg) throws IOException;

}