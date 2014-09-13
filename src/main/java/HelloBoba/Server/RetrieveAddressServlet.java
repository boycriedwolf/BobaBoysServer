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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/retrieveaddress", name="Retrieve-Address-Servlet")
public class RetrieveAddressServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		int userId = 0;

		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if(inFromClient != null) {
				jsonReqString = inFromClient.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject jsonObj = new JSONObject(jsonReqString);
		userId = jsonObj.getInt("user_id");

		JSONObject jsonResObj = new JSONObject();
		Map<String, String> addressMap = retrieveUserAddress(userId);
		
		jsonResObj.put("default_delivery_address", addressMap);	
		
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

	private Map<String, String> retrieveUserAddress(int userId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		Map<String, String> map = new HashMap<String, String>();
		String defaultAddress = "";
		String address2 = "";
		String address3 = "";
		String address4 = "";
		String address5 = "";
		
		try {
			ps = con.prepareStatement("SELECT * FROM " + ServerConstants.DB_USER_ADDRESS_TABLE +
					" WHERE user_id = ?");
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				defaultAddress = rs.getString("default_address"); 
				address2 = rs.getString("address_2");
				address3 = rs.getString("address_3");
				address4 = rs.getString("address_4");
				address5 = rs.getString("address_5");
				if(defaultAddress == null) {
					map.put("default_address", "none");
				}
				else map.put("default_address", defaultAddress);
				if(address2 == null) {
					map.put("address_2", "none");
				}
				else map.put("address_2", address2);
				if(address3 == null) {
					map.put("address_3", "none");
				}
				else map.put("address_3", address3);
				if(address4 == null) {
					map.put("address_4", "none");
				}
				else map.put("address_4", address4);
				if(address5 == null) {
					map.put("address_5", "none");
				}
				else map.put("address_5", address5);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}



}




