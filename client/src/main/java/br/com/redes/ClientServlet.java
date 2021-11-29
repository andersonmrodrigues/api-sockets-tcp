package br.com.redes;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ClientServlet extends HttpServlet {

    /* Metodo responsavel por receber via GET a chamada junto com a mensagem (message)
     * É validado se o parametro message possui "/" caso não possuir um Bad Request é retornado */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ClientApplication clientApplication = new ClientApplication();
        var param = req.getParameter("message");
        if (!param.contains("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("Invalid param");
        }
        clientApplication.sendRequest(param);
        var response = clientApplication.getResponse();
        clientApplication.close();
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(response);

    }
}
