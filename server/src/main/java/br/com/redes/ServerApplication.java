package br.com.redes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);
    public static final int PORT = 8899;


    /**
     * M�todo principal do servidor, nele � aberto um ponto de fluxo na porta especificada
     * Tamb�m ir� aceitar requisi��es na porta e tamb�m abrir� uma thread para cada uma, para que uma chamada n�o
     * bloqueie outra de ser recebida,
     *
     * @param args
     */
    public static void main(String[] args) {
        ServerSocket ss = null;
        Socket socket = null;
        try {
            ss = new ServerSocket(PORT);
            while (true) {
                log.info("SERVER UP!");
                socket = ss.accept();
                log.info("Requisi��o recebida de " + socket.getInetAddress().getHostName());
                MessageService dp = new MessageService(socket);
                new Thread(dp).start();
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            // fechando servicos...
            try {
                ss.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
}