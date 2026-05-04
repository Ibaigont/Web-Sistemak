package servlets;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import HTTPeXist.HTTPeXist;

public class SaveUpdateResource extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public SaveUpdateResource() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HTTPeXist eXist = new HTTPeXist("http://localhost:8080");
		String collection = request.getParameter("collection");
		String svgName = request.getParameter("svgName");
		String imagenSVG = request.getParameter("imagenSVG");
		String opcion = request.getParameter("actualizar_salva");

		System.out.println("Servlet- DatosXML " + collection);
		System.out.println("Servlet- DatosXML " + svgName);
		System.out.println("Servlet- DatosXML " + imagenSVG);
		System.out.println("Opciones " + opcion);

		request.setAttribute("collection", collection);
		request.setAttribute("svgName", svgName);
		request.setAttribute("imagenSVG", imagenSVG);

		String imagenURI = "http://localhost:8080/exist/rest/db/" + collection + "/" + svgName + "/";
		request.setAttribute("imagenURI", imagenURI);

		if (opcion.equals("save")) {
			int a = eXist.subirString(collection, imagenSVG, svgName);
			System.out.println("    Salvado " + a);
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("     Redireccionando a imagenEdit.jsp");
		RequestDispatcher rd = request.getRequestDispatcher("/jsp/imagenEdit.jsp");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		rd.forward(request, response);
	}
}