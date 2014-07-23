//package HelloBoba.Server;
//
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.*;
//
//import org.json.*;
//
//
//
//@WebServlet (value="/", name="Home-Page-Servlet")
//public class HomePageServlet extends HttpServlet{
//
//	private String causeOfFailure;
//
//	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
//		String jsonReqString = "";
//
//		try {
//			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
//			if(inFromClient != null) {
//				jsonReqString = inFromClient.readLine();
//			}
//		}catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		JSONObject jsonObj = null;
//		jsonObj = new JSONObject(jsonReqString);
//
//		//		InputStream inputStream = exchange.getRequestBody();
//		//		BufferedReader inFromClient = 
//		//				new BufferedReader(new InputStreamReader(inputStream));
//		//		
//		//		String jsonReqString = inFromClient.readLine();
//		//		JSONObject jsonObj = null;
//		//		jsonObj = new JSONObject(jsonReqString);
//
//		int userId = 0;
//		String affiliation = "";
//		userId = jsonObj.getInt("user_id");
//		affiliation = jsonObj.getString("affiliation");
//
//		JSONObject jsonResObj = new JSONObject();
//
//		if(setAccountAffiliation(userId, affiliation) && setAffilationForUserQueue(userId, affiliation)) {
//			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.ACCOUNT_AFFILIATION_SUCCESS);
//		}
//		else {
//			jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
//		}
//
//		response.setContentType("application/json");
//		String jsonResString = jsonResObj.toString();
//
//		response.setBufferSize(jsonResString.length());  //lets client know how long
//		OutputStream outputStream;
//		try {
//			outputStream = response.getOutputStream();
//			outputStream.write(jsonResString.getBytes());
//			outputStream.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//}
