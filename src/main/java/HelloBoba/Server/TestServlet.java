package HelloBoba.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/test", name="Test-Servlet")
public class TestServlet extends HttpServlet{
	
	private String causeOfFailure;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";

		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if(inFromClient != null) {
				jsonReqString = inFromClient.readLine();
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(jsonReqString);

//		try {
//		} catch (JSONException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		int userId = 0;
		String affiliation = "";
		userId = jsonObj.getInt("user_id");
		affiliation = jsonObj.getString("affiliation");
		System.out.println("hi2");
//		try {
//			
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		JSONObject jsonResObj = new JSONObject();

		jsonResObj.put("user_id", userId);
		jsonResObj.put("affiliation", affiliation);
//		try {
//			
//		}
//		catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		response.setContentType("application/json");
		String jsonResString = jsonResObj.toString();

		response.setBufferSize(jsonResString.length());  //lets client know how long
		OutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
			outputStream.write(jsonResString.getBytes());
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
