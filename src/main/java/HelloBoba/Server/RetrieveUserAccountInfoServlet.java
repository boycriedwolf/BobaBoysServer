package HelloBoba.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCardCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@WebServlet (value="/retrieveuseraccountinfo", name="Retrieve-User-Account-Info-Servlet")
public class RetrieveUserAccountInfoServlet extends HttpServlet{

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		int userId = 0;

		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if(inFromClient != null) {
				jsonReqString = inFromClient.readLine();
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject jsonObj = new JSONObject(jsonReqString);

		userId = jsonObj.getInt("user_id");

		JSONObject jsonResObj = new JSONObject();
		Map<String, String> map = new HashMap<String, String>();

		map = retrieveUserInfo(userId);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			jsonResObj.put(entry.getKey(), entry.getValue());
		}

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

	private Map<String, String> retrieveUserInfo(int userId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		String name = null;
		Map<String, String> userInfoMap = new HashMap<String, String>();
		try {
			ps = con.prepareStatement("SELECT name FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				name = rs.getString(1);
				userInfoMap.put("name", name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userInfoMap;
	}


}

