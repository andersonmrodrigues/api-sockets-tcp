package br.com.redes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);
    public static final int PORT = 8899;


    /**
     * Método principal do servidor, nele é aberto um ponto de fluxo na porta especificada
     * Também irá aceitar requisições na porta e também abrirá uma thread para cada uma, para que uma chamada não
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
                log.info("Requisição recebida de " + socket.getInetAddress().getHostName());
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