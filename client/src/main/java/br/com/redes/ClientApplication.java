package br.com.redes;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientApplication {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8899;
    private Socket sockConn;
    private InputStream inputStream;
    private OutputStream outputStream;

    public ClientApplication()
            throws IOException {
//        server = InetAddress.getByName(host);
        sockConn = new Socket(HOST, PORT);
        inputStream = sockConn.getInputStream();
        outputStream = sockConn.getOutputStream();

        log.info("Conexao: " +
                HOST +
                ":" + PORT);
    }

    public void sendRequest(String request)
            throws IOException {
        outputStream.write(request.getBytes());
        outputStream.flush();
        System.out.println("Requisicao: " + request);
    }

    public String getResponse()
            throws IOException {
        System.out.println("Resposta: ");
        StringBuilder sb = new StringBuilder();
        do {
            sb.append((char) inputStream.read());
//            System.out.print((char) inputStream.read());
        } while (inputStream.available() > 0);
        return sb.toString();
    }

    public void close()
            throws IOException {
        sockConn.close();
    }

    public static void main(String[] args) {
        try {
            var server = new Server(8080);
            var context = new ServletContextHandler();
            context.setContextPath("/");
            context.addServlet(new ServletHolder(new ClientServlet()), "/client");
            server.setHandler(context);
            server.start();
            server.join();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}