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
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import com.stripe.model.Charge;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/purchaseorders", name="Purchase-Orders-Servlet")
public class PurchaseOrderServlet extends HttpServlet{

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
		JSONObject jsonOrderObj = null;

		//		try {
		jsonObj = new JSONObject(jsonReqString);
		//		} catch (JSONException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}

		int userId = 0;
		int totalPrice = 0;
		int numberOfFreePearlMilkTeaUsed = 0;
		boolean futureOrder;
		boolean payingWithCreditCard = false;
		String timeToDeliver = "";
		String deliveryLocation = "";
		//		try {
		jsonOrderObj = jsonObj.getJSONObject("order");
		userId = jsonObj.getInt("user_id");
		totalPrice = jsonObj.getInt("total_price");
		payingWithCreditCard = jsonObj.getBoolean("paying_with_credit_card");
		futureOrder = jsonObj.getBoolean("future_order");
		if(futureOrder) {
			timeToDeliver = jsonObj.getString("time_to_deliver");
		}
		else timeToDeliver = "now";
		numberOfFreePearlMilkTeaUsed = jsonObj.getInt("number_of_free_pearl_milk_tea_used");
		deliveryLocation = jsonObj.getString("delivery_location");
		//		} catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		Date date = new Date();
		long currentLong = date.getTime();
		String currentTime = MiscMethods.convertTime(currentLong);

		JSONObject jsonResObj = new JSONObject();
		int numOfPMTOrdered = MiscMethods.numberOfPearlMilkTeaInOrder(jsonOrderObj);
		int updatedNumOfStamps = 0;
		//		try {
		if(payingWithCreditCard) {
			if(purchaseOrder(userId, totalPrice)) {
				int orderNumber = saveCustomerOrderDetailsToDatabase(userId, totalPrice, 
						timeToDeliver, 1, deliveryLocation, numberOfFreePearlMilkTeaUsed,
						currentTime);
				if(orderNumber != 0) {
					if(saveCustomerOrderToDatabase(jsonOrderObj, orderNumber)) {
						updatedNumOfStamps = incrementStampCardAndFreePMTCounter(
								userId, numOfPMTOrdered, 
								numberOfFreePearlMilkTeaUsed);
						jsonResObj.put(ServerConstants.REQUEST_STATUS, 
								ServerConstants.ORDER_PURCHASED_AND_SAVED_SUCCESS);
						jsonResObj.put("order_number", orderNumber);
						jsonResObj.put("number_of_stamps", updatedNumOfStamps);
					}
					else jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
				}
				else jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
			}
			else jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
		}
		else {
			updatedNumOfStamps = incrementStampCardAndFreePMTCounter(userId, 
					numOfPMTOrdered, numberOfFreePearlMilkTeaUsed);				
			int orderNumber = saveCustomerOrderDetailsToDatabase(userId, totalPrice, 
					timeToDeliver, 0, deliveryLocation, numberOfFreePearlMilkTeaUsed,
					currentTime);
			if(orderNumber != 0) {
				if(saveCustomerOrderToDatabase(jsonOrderObj, orderNumber)) {
					updatedNumOfStamps = incrementStampCardAndFreePMTCounter(
							userId, numOfPMTOrdered, 
							numberOfFreePearlMilkTeaUsed);
					jsonResObj.put(ServerConstants.REQUEST_STATUS, 
							ServerConstants.ORDER_PURCHASED_AND_SAVED_SUCCESS);
					jsonResObj.put("order_number", orderNumber);
					jsonResObj.put("number_of_stamps", updatedNumOfStamps);
				}
				else jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
			}
			else jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
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

	private boolean purchaseOrder(int userId, int totalPrice) {

		Stripe.apiKey = "sk_test_CY8QQMarcq8pB4nhhQB8dZ6g";	
		Map<String, Object> chargeParams = new HashMap<String, Object>();
		chargeParams.put("amount", totalPrice);
		chargeParams.put("currency", "usd");

		String customerId = MiscMethods.getCustomerId(userId);
		chargeParams.put("customer", customerId);
		try {
			Charge.create(chargeParams);
			return true;
		} catch (AuthenticationException e) {
			causeOfFailure = e.getMessage();
			e.printStackTrace();
		} catch (InvalidRequestException e) {
			causeOfFailure = e.getMessage();
			e.printStackTrace();
		} catch (APIConnectionException e) {
			causeOfFailure = e.getMessage();
			e.printStackTrace();
		} catch (CardException e) {
			causeOfFailure = e.getCode();
			e.printStackTrace();
		} catch (APIException e) {
			causeOfFailure = e.getMessage();
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * jsonOrderObj has enums as keys in Integer form, and quantity as values
	 */
	private boolean saveCustomerOrderToDatabase(JSONObject jsonOrderObj, int orderNumber) {
		Connection con = MiscMethods.establishDatabaseConnection();
		String sql = "INSERT INTO " + ServerConstants.DB_CURRENT_ORDER_TABLE +
				" (order_id, menu_id, quantity) VALUES (?, ?, ?)";

		String menuIdString;
		int quantity = 0;

		try {
			Iterator<String> orderKeys = jsonOrderObj.keys(); 
			PreparedStatement ps = con.prepareStatement(sql);
			while(orderKeys.hasNext()) {
				menuIdString = orderKeys.next();
				quantity = jsonOrderObj.getInt(menuIdString);
				int menuId = Integer.parseInt(menuIdString);
				ps.setInt(1, orderNumber);
				ps.setInt(2, menuId);
				ps.setInt(3, quantity);
				ps.addBatch();
			}
			ps.executeBatch();
			return true;
		} catch (SQLException e) {
			causeOfFailure = e.getMessage();
			e.printStackTrace();
		}
		//		} catch (JSONException e) {
		//			causeOfFailure = e.getMessage();
		//			e.printStackTrace();
		//		}
		return false;
	}

	private int saveCustomerOrderDetailsToDatabase(int userId, 
			int totalPrice, String timeToDeliver, int payingWithCreditCard,
			String deliveryLocation, int numFreePMT, String currentTime) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		int orderNumber = 0;

		//insert the order into current orders table
		try {
			ps = con.prepareStatement("INSERT INTO " + 
					ServerConstants.DB_CURRENT_ORDER_DETAILS_TABLE + " (user_id, price_of_order," +
					" time_order_placed, time_to_deliver, paying_with_credit_card," +
					" delivery_location, number_of_free_pearl_milk_tea_used) " +
					"VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, userId);
			ps.setInt(2, totalPrice);
			ps.setString(3, currentTime);
			ps.setString(4, timeToDeliver);
			ps.setInt(5, payingWithCreditCard);
			ps.setString(6, deliveryLocation);
			ps.setInt(7, numFreePMT); 
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if(rs.next()) {
				orderNumber = rs.getInt(1);
			}	
			return orderNumber;
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
		return orderNumber;
	}

	private int incrementStampCardAndFreePMTCounter(int userId, int numOfPMTOrdered, int numberOfFreePearlMilkTeaUsed) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps1, ps2;
		int numOfStamps = 0;
		int numOfAdditionalFreePMT = 0;
		MiscMethods.removeFreePearlMilkTeaUsed(userId, numberOfFreePearlMilkTeaUsed);

		try {
			ps1 = con.prepareStatement("SELECT stamp_card_counter FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
			ps1.setInt(1, userId);
			ResultSet rs = ps1.executeQuery();
			if(rs.next()) {
				numOfStamps = rs.getInt(1);
			}
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
		//figuring out new number of stamps and "completed" stamp cards
		int updatedNumOfStamps = numOfStamps + numOfPMTOrdered - numberOfFreePearlMilkTeaUsed;
		if(updatedNumOfStamps > ServerConstants.STAMP_CARD_NUMBER - 1) {
			numOfAdditionalFreePMT = updatedNumOfStamps/ServerConstants.STAMP_CARD_NUMBER;
			MiscMethods.giveFreePearlMilkTea(userId, numOfAdditionalFreePMT);
			updatedNumOfStamps = updatedNumOfStamps%ServerConstants.STAMP_CARD_NUMBER;
		}
		//save their new # of stamps
		try {
			ps2 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + " SET stamp_card_counter = ? WHERE user_id = ? ");
			ps2.setInt(1, updatedNumOfStamps);
			ps2.setInt(2, userId);
			ps2.executeUpdate();
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
		return updatedNumOfStamps;
	}


}







