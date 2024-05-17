package springboot.websocket;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * The MessageSenderService class is responsible for sending messages to WebSockets.
 * It uses the MessagingController to send the messages.
 * This class is annotated with @Service, meaning it is a service provider in the Spring framework.
 * It implements Serializable, allowing its instances to be written to an OutputStream.
 */
@Service
public class MessageSenderService implements Serializable {

    // The MessagingController used to send messages
    private final MessagingController messagingController;

    /**
     * Constructs a new MessageSenderService with the provided MessagingController.
     *
     * @param messagingController the MessagingController used to send messages
     */
    @Autowired
    public MessageSenderService(MessagingController messagingController) {
        this.messagingController = messagingController;
    }

    /**
     * Sends an update to WebSockets.
     * It creates a new Message with the provided string, and uses the MessagingController to send this message to all WebSockets.
     *
     * @param msg the message to be sent
     */
    public void atualizarWebSockets(String msg) {
        Message message = new Message(msg);
        messagingController.sendMessageToAll(message);
    }
}