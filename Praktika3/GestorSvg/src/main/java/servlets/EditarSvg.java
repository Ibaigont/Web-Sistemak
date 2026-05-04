package servlets;


import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import HTTPeXist.HTTPeXist;


public class EditarSvg extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private HTTPeXist eXist;

	public void init(ServletConfig config) {
		System.out.println("---> Entrando en init()de listResource");
		eXist = new HTTPeXist("http://localHost:8080");
		System.out.println("---> Saliendo de init()de LoginServlet");
	}
	
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		String collection= request.getParameter("collection");
		String svgName = request.getParameter("svgName");
		
		//HTTPeXist eXist = new HTTPeXist("http://localHost:8080");
		String imagenSVG= eXist.read(collection, svgName);
		
		request.setAttribute("collection",collection);
		request.setAttribute("svgName",svgName);
		request.setAttribute("imagenSVG",imagenSVG);
		String imagenURI = "http://localhost:8080/exist/rest/db/" + collection + "/" + svgName + "/";
		request.setAttribute("imagenURI",imagenURI);
		
		System.out.println("     Redireccionando a imagenEdit.jsp");
		RequestDispatcher rd = request.getRequestDispatcher("/jsp/imagenEdit.jsp");
		rd.forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
}

