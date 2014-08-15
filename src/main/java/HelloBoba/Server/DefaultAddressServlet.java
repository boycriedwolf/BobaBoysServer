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

	private boolean setDefaultDeliveryAddress(int userId, String newDefaultDeliveryAddress) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps, ps1, ps2;
		String oldDefaultAddress = "";
		String address2 = "";
		String address3 = "";
		String address4 = "";
		String address5 = "";
		
		try {
			//check if there's a row with the userid in the table, this checks if it's the
			//first time a user is inputting an address
			ps = con.prepareStatement("SELECT * FROM " + ServerConstants.DB_USER_ADDRESS_TABLE +
					" WHERE user_id = ?");
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) { //user has at least one address associated with account
				oldDefaultAddress = rs.getString("default_address"); //grab the column values for user
				address2 = rs.getString("address_2");
				address3 = rs.getString("address_3");
				address4 = rs.getString("address_4");
				address5 = rs.getString("address_5");
				
				if(address2 == null) { //set the address supplied by client as new default
					ps1 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_ADDRESS_TABLE + 
							" SET default_address = ?, address_2 = ? WHERE user_id = ?");
					ps1.setString(1, newDefaultDeliveryAddress);
					ps1.setString(2, oldDefaultAddress);
					ps1.setInt(3, userId);
					ps1.executeUpdate();
					
				} else if(address3 == null) {
					ps1 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_ADDRESS_TABLE + 
							" SET default_address = ?, address_2 = ?, address_3 = ? WHERE user_id = ?");
					ps1.setString(1, newDefaultDeliveryAddress);
					ps1.setString(2, oldDefaultAddress);
					ps1.setString(3, address2);
					ps1.setInt(4, userId);
					ps1.executeUpdate();
					
				} else if(address4 == null) {
					ps1 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_ADDRESS_TABLE + 
							" SET default_address = ?, address_2 = ?, address_3 = ?, address_4 = ? WHERE user_id = ?");
					ps1.setString(1, newDefaultDeliveryAddress);
					ps1.setString(2, oldDefaultAddress);
					ps1.setString(3, address2);
					ps1.setString(4, address3);
					ps1.setInt(5, userId);
					ps1.executeUpdate();
					
				} else { //regardless of whether address5 is null or not, it's getting replaced
					ps1 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_ADDRESS_TABLE + 
							" SET default_address = ?, address_2 = ?, address_3 = ?, address_4 = ?, address_5 = ? WHERE user_id = ?");
					ps1.setString(1, newDefaultDeliveryAddress);
					ps1.setString(2, oldDefaultAddress);
					ps1.setString(3, address2);
					ps1.setString(4, address3);
					ps1.setString(5, address4);
					ps1.setInt(6, userId);
					ps1.executeUpdate();
				}
				
			} else { //adding new row with userid and their default address
				ps2 = con.prepareStatement("INSERT INTO " + ServerConstants.DB_USER_ADDRESS_TABLE +
					" (user_id, default_address) VALUES(?, ?)");
				ps2.executeUpdate();
			}
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


}
