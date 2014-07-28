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

@WebServlet (value="/defaultaddress", name="Default-Address-Servlet")
public class DefaultAddressServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		int userId = 0;
		String defaultDeliveryAddress = "";
		JSONObject jsonResObj = new JSONObject();
		
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
		defaultDeliveryAddress = jsonObj.getString("default_delivery_address");

		if(setDefaultDeliveryAddress(userId, defaultDeliveryAddress)) {
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.DEFAULT_ADDRESS_SET_SUCCESS);
		}
		else {
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.GENERIC_FAILURE);
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

	private boolean setDefaultDeliveryAddress(int userId, String defaultDeliveryAddress) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;

		try {
			ps = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
					" SET default_delivery_address = ? WHERE user_id = ?");
			ps.setString(1, defaultDeliveryAddress);
			ps.setInt(2, userId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


}
