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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/retrieveaffiliationanddefaultaddress", name="Retrieve-Affiliation-And-Default-Address-Servlet")
public class RetrieveDefaultAddressServlet extends HttpServlet{

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		int userId = 0;
		String defaultString = "default";
		String defaultDeliveryAddress = "default";
		String affiliation = "default";

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

		defaultDeliveryAddress = retrieveDefaultAddress(userId);
//		affiliation = retrieveAffiliation(userId);
//		if(!defaultString.equalsIgnoreCase(affiliation)) {
			if(!defaultString.equalsIgnoreCase(defaultDeliveryAddress)) {
//				if(affiliation != null) {
//					jsonResObj.put("affiliation", affiliation);
//				}
//				else jsonResObj.put("affiliation", ServerConstants.NO_AFFILIATION);
				if(defaultDeliveryAddress != null) {
					jsonResObj.put("default_delivery_address", defaultDeliveryAddress);
				}
				else jsonResObj.put("default_delivery_address", ServerConstants.NO_DEFAULT_DELIVERY_ADDRESS);
//				jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.AFFILIATION_AND_ADDRESS_RETRIEVE_SUCCESS);
			}
			else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.RETRIEVE_ADDRESS_FAIL);
//		}
//		else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.RETRIEVE_AFFILIATION_FAIL);

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

//	private String retrieveAffiliation(int userId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		String affiliation = null;
//		try {
//			ps = con.prepareStatement("SELECT affiliation FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
//			ps.setInt(1, userId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				affiliation = rs.getString(1);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return affiliation;
//	}

	private String retrieveDefaultAddress(int userId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		String defaultDeliveryAddress = null;
		try {
			ps = con.prepareStatement("SELECT default_delivery_address FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				defaultDeliveryAddress = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return defaultDeliveryAddress;
	}



}



