package servlets;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import HTTPeXist.HTTPeXist;

public class NewImage extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String collection = request.getParameter("collection");
        String svgName = request.getParameter("svgName");

        if (!svgName.endsWith(".svg")) {
            svgName += ".svg";
        }

        String blankSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"400\" height=\"400\"></svg>";

        HTTPeXist eXist = new HTTPeXist("http://localhost:8080");
        eXist.subirString(collection, blankSvg, svgName);

        request.setAttribute("informacion", "Imagen " + svgName + " creada en blanco.");
        RequestDispatcher rd = request.getRequestDispatcher("/jsp/index.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}