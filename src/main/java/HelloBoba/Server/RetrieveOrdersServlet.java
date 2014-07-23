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

@WebServlet (value="/retrieveorders", name="Retrieve-Orders-Servlet")
public class RetrieveOrdersServlet extends HttpServlet{

	private String causeOfFailure = "";
	
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
		//JSONRPC2Request jsonReq = null;
		JSONObject jsonObj = null;

//		try {
			jsonObj = new JSONObject(jsonReqString);
//		} catch (JSONException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		int clientUserId = 0;

//		try {
			clientUserId = jsonObj.getInt("client_user_id");
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		JSONObject jsonOrder = new JSONObject();
		JSONObject jsonResObj = new JSONObject();
		
//		try {
			if(MiscMethods.checkIfAdmin(clientUserId)) {
				//jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.IS_ADMIN_ACCOUNT);
				jsonOrder = retrieveOrders();
				if(causeOfFailure.equalsIgnoreCase("")) {
					jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.ORDERS_EXIST);
					jsonResObj.put("order", jsonOrder);
				}
				else {
					jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
				}
			}
			else {
				jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.CLIENT_IS_NOT_ADMIN);
			}
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


	/*
	 * Order is sent back in a JSONObject, the order is a JSONObject in the JSONObject
	 */

	private JSONObject retrieveOrders() {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		int orderId = 0;
		int userId = 0;
		int payingWithCreditCard;
		String timeOrderPlaced = "";
		String timeToDeliver = "";
		int priceOfOrder = 0;
		String deliveryLocation = "";
		String name;
		JSONObject jsonOrderObject = new JSONObject();

		try {
			ps = con.prepareStatement("SELECT * FROM " + ServerConstants.DB_CURRENT_ORDER_DETAILS_TABLE);
			ResultSet result = ps.executeQuery();
			while(result.next()) {
				orderId = result.getInt(1);
				userId = result.getInt(2);
				name = MiscMethods.getNameCorrespondingToUserId(userId);
				payingWithCreditCard = result.getInt(6);
				priceOfOrder = result.getInt(3); //cents		
				timeOrderPlaced = result.getString(4);
				timeToDeliver = result.getString(5);
				deliveryLocation = result.getString(7);
				
				jsonOrderObject.put("order_id", orderId);
				jsonOrderObject.put("name", name);
				jsonOrderObject.put("order", retrieveCurrentOrders(orderId));
				jsonOrderObject.put("paying_with_credit_card", payingWithCreditCard);
				jsonOrderObject.put("price_of_order", priceOfOrder);
				jsonOrderObject.put("time_order_placed", timeOrderPlaced);
				jsonOrderObject.put("time_to_deliver", timeToDeliver);
				jsonOrderObject.put("delivery_location", deliveryLocation);
				return jsonOrderObject;
			}
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
//		catch (JSONException e) {
//			causeOfFailure = e.getLocalizedMessage();
//			e.printStackTrace();
//		}
		return jsonOrderObject;
	}

	private JSONObject retrieveCurrentOrders(int orderId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		int menuId;
		String menuItemName;
		int quantity = 0;
		JSONObject jsonOrder = new JSONObject();
		try {
			ps = con.prepareStatement("SELECT * FROM " + ServerConstants.DB_CURRENT_ORDER_TABLE + " WHERE order_id = ?");
			ps.setInt(1, orderId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				menuId = rs.getInt(3); 
				menuItemName = MiscMethods.getNameForMenuId(menuId);
				quantity = rs.getInt(4);
				jsonOrder.put(menuItemName, quantity);
			}
			return jsonOrder;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return jsonOrder;
	}


}

