package springboot.websocket;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The MessageSender class is responsible for sending messages to WebSockets.
 * It uses the MessagingController to send the messages.
 * This class is annotated with @Component, meaning it is an object managed by the Spring framework.
 */
@Component
public class MessageSender {

    // The MessagingController used to send messages
    private final MessagingController messagingController;

    /**
     * Constructs a new MessageSender with the provided MessagingController.
     *
     * @param messagingController the MessagingController used to send messages
     */
    @Autowired
    public MessageSender(MessagingController messagingController) {
        this.messagingController = messagingController;
    }

    /**
     * Sends an update to WebSockets.
     * It creates a new Message with the provided string, and uses the MessagingController to send this message to all WebSockets.
     *
     * @param msg the message to be sent
     */
    public void enviarAtualizacaoParaWebSockets(String msg) {
        Message message = new Message(msg);
        messagingController.sendMessageToAll(message);
    }
}