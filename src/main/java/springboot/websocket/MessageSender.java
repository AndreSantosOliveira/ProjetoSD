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

@Component
public class MessageSender {

    private final MessagingController messagingController;

    @Autowired
    public MessageSender(MessagingController messagingController) {
        this.messagingController = messagingController;
    }

    public void enviarAtualizacaoParaWebSockets(String msg) {
        Message message = new Message(msg);
        messagingController.sendMessageToAll(message);
    }
}
