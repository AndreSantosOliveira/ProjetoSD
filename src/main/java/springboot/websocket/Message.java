package springboot.websocket;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

/**
 * The Message record represents a message with a content.
 * This is a record, a special kind of class in Java that is a transparent carrier for immutable data.
 * Records can be thought of as nominal tuples.
 *
 * @param content the content of the message
 */
public record Message(String content) {
}