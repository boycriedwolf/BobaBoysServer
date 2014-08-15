package HelloBoba.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.*;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


@WebServlet (value="/fbconnect", name="FB-Connect-Servlet")
public class FBConnectServlet extends HttpServlet{

	private static String apiKey = "267256790104878";
	private static String apiSecret = "d8d2d39f5ecb04a5b2bc788125edd67e";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		int userId = 0;
		String shortLivedFBToken = "";

		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if(inFromClient != null) {
				jsonReqString = inFromClient.readLine();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObj = new JSONObject(jsonReqString);
		userId = jsonObj.getInt("user_id");
		shortLivedFBToken = jsonObj.getString("fb_token");

		String longLivedFBToken = convertToLongLivedFBToken(shortLivedFBToken);	
		JSONObject jsonResObj = new JSONObject();

		if(MiscMethods.updateFBToken(userId, longLivedFBToken)) {
			MiscMethods.giveFreePearlMilkTea(userId, 1);
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.FB_TOKEN_UPDATE_SUCCESS);
			jsonResObj.put(ServerConstants.LONG_LIVED_FB_TOKEN, longLivedFBToken);
		}
		else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.GENERIC_FAILURE);

		response.setContentType("application/json");
		String jsonResString = jsonResObj.toString();

		response.setBufferSize(jsonResString.length());  //lets client know how long
		OutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
			outputStream.write(jsonResString.getBytes());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String convertToLongLivedFBToken(String shortLivedFBToken) {
		FacebookClient facebookClient = new DefaultFacebookClient(shortLivedFBToken);
		AccessToken extendedAccessToken = facebookClient.obtainExtendedAccessToken(apiKey, apiSecret, shortLivedFBToken);
		String extendedToken = extendedAccessToken.getAccessToken();
		return extendedToken;

	}

}
