package servlets;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import HTTPeXist.HTTPeXist;

public class NewImageFile extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String collection = request.getParameter("collection");
        String svgName = request.getParameter("svgName");
        String imagenSVG = request.getParameter("imagenSVG");

        if (!svgName.endsWith(".svg")) {
            svgName += ".svg";
        }

        HTTPeXist eXist = new HTTPeXist("http://localhost:8080");
        eXist.subirString(collection, imagenSVG, svgName);

        request.setAttribute("informacion", "El archivo " + svgName + " se ha subido correctamente.");
        RequestDispatcher rd = request.getRequestDispatcher("/jsp/index.jsp");
        rd.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}