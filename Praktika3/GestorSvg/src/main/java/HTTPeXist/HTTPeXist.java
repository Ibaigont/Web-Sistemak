package HTTPeXist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.exist.xmldb.XmldbURI;
import org.xml.sax.SAXException;

public class HTTPeXist {

	private String server;

	public HTTPeXist(String server) {
		super();
		this.server = server;
	}

	/* -->READ lee un recurso de una coleccion */
	public String read(String collection, String resourceName) throws IOException {
		String resource = new String();
		URL url = new URL(
				this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection + "/" + resourceName);
		System.out.println("-->READ-url:" + url.toString());
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("GET");

		/* Crear codigo de autorizacion y meter en la cabecera Authorization */
		String codigoBase64 = getAuthorizationCode("admin", "admin");
		connect.setRequestProperty("Authorization", "Basic " + codigoBase64);
		connect.connect();
		System.out.println("<--READ-status: " + connect.getResponseCode());

		/* Lee el contenido del mensaje de respuesta- RECUSRSO */
		InputStream connectInputStream = connect.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(connectInputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			resource = resource + line + "\n";
			System.out.println("<--READ: " + line);
		}
		return resource;
	}

	/* -->LIST lista los recursos en una coleccion */
	public String list(String collection) {
		String lista = new String();
		try {
			URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("GET");

			String codigoBase64 = getAuthorizationCode("admin", "admin");
			connect.setRequestProperty("Authorization", "Basic " + codigoBase64);
			connect.connect();

			if (connect.getResponseCode() == 200) {
				InputStream connectInputStream = connect.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connectInputStream));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					lista += line + "\n";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lista;
	}

	/* -->SUBIR recurso en un fichero */
	public int subir(String collection, String resourceFileName) throws IOException {

		System.out.println("-->SUBIR: " + resourceFileName + " a " + collection);
		File file = new File(resourceFileName);
		if (!file.canRead()) {
			System.err.println("-->SUBIR: Cannot read file " + file);
			return -1;
		}
		String document = file.getName();
		URL url = new URL(
				this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection + "/" + document);
		System.out.println("-->SUBIR-url: " + url);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("PUT");
		connect.setDoOutput(true);

		String codigoBase64 = getAuthorizationCode("admin", "admin");
		connect.setRequestProperty("Authorization", "Basic " + codigoBase64);
		connect.setRequestProperty("ContentType", "aplication/xml");

		StringBuilder postData = new StringBuilder();
		String cadena = "";
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferReader = new BufferedReader(fileReader);
		while ((cadena = bufferReader.readLine()) != null) {
			postData.append(cadena + "\n");
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		System.out.println("-->SUBIR: postData : " + postData);
		connect.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		connect.setDoOutput(true);
		connect.getOutputStream().write(postDataBytes);
		fileReader.close();
		bufferReader.close();

		int status = connect.getResponseCode();
		System.out.println("<--SUBIR: " + status);
		System.out.println("<--SUBIR: " + connect.getResponseMessage());
		return connect.getResponseCode();

	}

	/* -->DELETE borrar un reurso */
	public int delete(String collection, String resourceName) {
		int status = 0;
		try {
			URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection + "/" + resourceName);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("DELETE");

			String codigoBase64 = getAuthorizationCode("admin", "admin");
			connect.setRequestProperty("Authorization", "Basic " + codigoBase64);
			connect.connect();

			status = connect.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;

	}

	/*-->SUBIR recurso en un String */
	public int subirString(String collection, String resource, String resourceName) throws IOException {
		int status = 0;
		URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection + "/" + resourceName);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("PUT");
		connect.setDoOutput(true);

		String codigoBase64 = getAuthorizationCode("admin", "admin");
		connect.setRequestProperty("Authorization", "Basic " + codigoBase64);
		connect.setRequestProperty("Content-Type", "application/xml");

		byte[] postDataBytes = resource.getBytes("UTF-8");
		connect.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

		connect.getOutputStream().write(postDataBytes);

		status = connect.getResponseCode();
		return status;
	}

	/* -->DELETE borrar coleccion */
	public int delete(String collection) {
		int status = 0;
		try {
			URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("DELETE");

			String codigoBase64 = getAuthorizationCode("admin", "admin");
			connect.setRequestProperty("Authorization", "Basic " + codigoBase64);
			connect.connect();

			status = connect.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}

	/*-->CREATE (String collection)  */
	public int create(String collection) throws IOException {
		int status = 0;
		URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("PUT");

		String codigoBase64 = getAuthorizationCode("admin", "admin");
		connect.setRequestProperty("Authorization", "Basic " + codigoBase64);
		connect.connect();

		status = connect.getResponseCode();
		return status;
	}

	/* FUNCIONES AUXILIARES */

	/* Codigo de autorizacion basic */

	public static String getAuthorizationCode(String user, String pwd) {
		String codigo = user + ":" + pwd;
		String codigoBase64 = cifrarBase64(codigo);
		System.out.println("-->CODIGO AUTORIZACION: " + codigoBase64);
		return codigoBase64;
	}

	public static String cifrarBase64(String a) {
		Base64.Encoder encoder = Base64.getEncoder();
		String b = encoder.encodeToString(a.getBytes(StandardCharsets.UTF_8));
		return b;
	}

	public static String descifrarBase64(String a) {
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] decodedByteArray = decoder.decode(a);

		String b = new String(decodedByteArray);
		return b;
	}

	public String codificaQuery(String query) {
		query = query.replaceAll(" ", "%20").replaceAll("\\<", "%3C").replaceAll("\\>", "%3E").replaceAll("\\!", "%21")
				.replaceAll("\\#", "%23").replaceAll("\\$", "%24").replaceAll("\\'", "%27").replaceAll("\\(", "%28")
				.replaceAll("\\)", "%29").replaceAll("\\*", "%2A").replaceAll("\\+", "%2B").replaceAll("\\,", "%2C")
				.replaceAll("\\:", "%3A").replaceAll("\\;", "%3B").replaceAll("\\=", "%3D").replaceAll("\\?", "%3F")
				.replaceAll("\\@", "%40").replaceAll("\\[", "%5B").replaceAll("\\]", "%5D");

		return query;
	}

	public static void main(String[] args)
			throws IOException, ParserConfigurationException, SAXException, TransformerException {
		HTTPeXist prueba = new HTTPeXist("http://localhost:8080");
		String resourceName = "camion.svg";
		String collection = "SVG_imagenes";
		String imagen = prueba.read(collection, resourceName);

	}
}