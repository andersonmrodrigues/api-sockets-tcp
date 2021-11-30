package br.com.redes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClientApplication {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);


    /**
     * Método responsável por inicializar o cliente
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new ClientService().execute();
    }

}