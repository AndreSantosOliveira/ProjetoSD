package springboot;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * The WebServer class is the entry point of the Spring Boot application.
 * It is annotated with @SpringBootApplication, indicating it is a Spring Boot application.
 */
@SpringBootApplication
public class WebServer {

    /**
     * The main method runs the Spring Boot application.
     * It calls the run method of SpringApplication with the WebServer class and the command line arguments.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WebServer.class, args);
    }

    /**
     * The commandLineRunner method is a Spring Boot application runner.
     * It returns a CommandLineRunner bean that will be run when the application context is loaded.
     * Currently, it does nothing.
     *
     * @param ctx the ApplicationContext
     * @return a CommandLineRunner
     */
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

        };
    }
}