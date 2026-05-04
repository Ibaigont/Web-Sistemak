<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.w3c.dom.NodeList"%>

<%HashMap<String, String> svgList = (HashMap) request.getAttribute("listaSVG");
	String collection = (String) request.getAttribute("collection");%>
<!DOCTYPE html>
<html lang="es">
	<head>
	   <title>Lista SVGs</title>
	   <link href="/GestorSvg/css/styleSheet.css" rel="stylesheet" />
	</head>
	<body>
		<header>
			<h1>Gestor Imágenes SVG en eXist</h1>
			<h3>Lista Imagenes en la colección: <%=collection %> - SW 2026</h3>
		</header>
		
		<nav class="menu">
			<a href="./jsp/index.jsp"> Inicio </a>
		</nav>
		<%
			for (Map.Entry<String, String> entry : svgList.entrySet()) {
		%>
		<section><table><tr>
		  <td><h3><%=entry.getKey()%></h3></td>
			<td><form  method='get' action='./apiEdit'>
					<%="<input type='hidden' name='collection' value='" + collection + "'/>"%>
					<%="<input type='hidden' name='svgName' value='" + entry.getKey() + "'/>"%>
					<button type='submit'>Edit</button>			
			</form> </td>
			<td><form  method='get' action='./apiDelete'>
					<%="<input type='hidden' name='collection' value='" + collection + "'/>"%>
					<%="<input type='hidden' name='svgName' value='"+ entry.getKey() + "'/>"%>
						<button type='submit'>Delete</button>	
			</form></td>
			<td><%=entry.getValue()%></td></tr>
		</table></section>
	
		<%}	%>
		<footer><h5>Sistemas Web - Escuela Ingeniería de Bilbao - EHU</h5></footer>
	</body>
</html>