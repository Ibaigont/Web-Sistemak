package servlets;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import HTTPeXist.HTTPeXist;

public class CreateCollection extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String collection = request.getParameter("collection");

        HTTPeXist eXist = new HTTPeXist("http://localhost:8080");

        eXist.subirString(collection, "<svg></svg>", "temporal.svg");

        eXist.delete(collection, "temporal.svg");

        request.setAttribute("informacion", "Colección " + collection + " creada correctamente.");
        RequestDispatcher rd = request.getRequestDispatcher("/jsp/index.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}