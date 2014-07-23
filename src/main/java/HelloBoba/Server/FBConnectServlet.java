package HelloBoba.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class FBConnectServlet extends HttpServlet{
	
	//private String causeOfFailure;

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

//		try {
			jsonObj = new JSONObject(jsonReqString);
//		} catch (JSONException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		int userId = 0;
		String fbToken = "";

//		try {
			userId = jsonObj.getInt("user_id");
			fbToken = jsonObj.getString("fb_token");
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		JSONObject jsonResObj = new JSONObject();

//		try {
			if(MiscMethods.updateFBToken(userId, fbToken)) {
				MiscMethods.setFBConnectForUserQueue(userId);
				MiscMethods.giveFreePearlMilkTea(userId, 1);
				jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.FB_TOKEN_UPDATE_SUCCESS);
			}
			else jsonResObj.put(ServerConstants.REQUEST_STATUS, false);
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
