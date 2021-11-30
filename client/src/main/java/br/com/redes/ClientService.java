package br.com.redes;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

@Data
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);
    private static final String QUIT = "\\quit";

    private final String HOST = "127.0.0.1";
    private final int PORT = 8899;
    private final String SEPARATOR = "---------------------------------------------";
    private Socket sockConn;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Método responsável por iniciar a conexão com o servidor
     *
     * @throws IOException
     */
    private void open()
            throws IOException {
        try {
            sockConn = new Socket(HOST, PORT);
            inputStream = sockConn.getInputStream();
            outputStream = sockConn.getOutputStream();
            log.info("Conexao: " + HOST + ":" + PORT);
        } catch (Exception e) {
            System.err.println("Servidor informado não disponível");
        }
    }

    /**
     * Método responsável por enviar a mensagem digita pelo usuário ao servidor
     *
     * @param request
     * @throws IOException
     */
    private void sendRequest(String request)
            throws IOException {
        outputStream.write(request.getBytes());
        outputStream.flush();
        System.out.println("Requisicao: " + request);
    }

    /**
     * Método responsavel receber a mensagem do servidor e transforma-la em texto.
     *
     * @return
     * @throws IOException
     */
    private String getResponse()
            throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        var response = "";
        while ((read = inputStream.read(buffer)) != -1) {
            response = new String(buffer, 0, read);
            break;
        }
        return response;
    }

    /**
     * Método responsável por fechar a conexão que foi aberta no inicio da comunicação com o servidor.
     */
    private void close() {
        try {
            sockConn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método responsável por executar a classe, ele recebe o dado informado pelo usuário no console.
     * Valida se a mensagem que o usuário informou são mensagens que o servidor irá responder.
     * Valida se o usuário deseja encerrar a execução.
     */
    public void execute() {
        var msg = "";
        do {
            System.out.println("Escreva sua mensagem ou \\quit para interromper a execução");
            System.out.println(SEPARATOR);
            msg = new Scanner(System.in).nextLine();
            if (!validMsg(msg))
                System.err.println("Mensagem inválida!");
            if (!msg.equalsIgnoreCase(QUIT)) {
                doRequestAndPrintResponse(msg);
                msg = "";
            }
        } while (!msg.equalsIgnoreCase(QUIT));
    }

    /**
     * Método responsável chamar o método que abre conexão com o servidor.
     * Método responsável por enviar a requisição ao servidor.
     * Método responsável por receber a resposta do servidor e printá-la no console.
     *
     * @param msg
     */
    private void doRequestAndPrintResponse(String msg) {
        try {
            open();
            sendRequest(msg);
            var response = getResponse();
            System.out.println(SEPARATOR);
            System.out.println("Resposta:");
            System.out.println(response);
            System.out.println(SEPARATOR);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     * Método responsável por validar as mensagens que o servidor irá aceitar
     *
     * @param msg
     * @return
     */
    private boolean validMsg(String msg) {
        return valid(msg, "/quem") ||
                valid(msg, "/data") ||
                valid(msg, "/ip") ||
                valid(msg, "/mac") ||
                valid(msg, "/sys") ||
                valid(msg, "/dev") ||
                valid(msg, "/info") ||
                valid(msg, "/dolar") ||
                valid(msg, "/trends") ||
                valid(msg, QUIT);
    }

    /**
     * Método que faz a comparação entre duas strings, para saber se são iguais.
     *
     * @param data
     * @param dataValid
     * @return
     */
    private boolean valid(String data, String dataValid) {
        return data.equalsIgnoreCase(dataValid);
    }
}
