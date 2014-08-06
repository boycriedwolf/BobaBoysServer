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

@WebServlet (value="/updateorderstatus", name="Update-Order-Status-Servlet")
public class UpdateOrderStatusServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		JSONArray ordersUpdated;
		int clientUserId = 0;
		int numCompleted;
		Map<Integer, String> updatedOrdersMap = new HashMap<Integer, String>();
		int orderId = 0;
		String status = "";
		JSONObject jsonResObj = new JSONObject();

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

		clientUserId = jsonObj.getInt("client_user_id");
		ordersUpdated = jsonObj.getJSONArray("orders_completed");
		numCompleted = ordersUpdated.length(); 

		for(int i=0; i<numCompleted; i++) {
			JSONObject json = new JSONObject();
			json = ordersUpdated.getJSONObject(i);
			orderId = json.getInt("order_id");
			status = json.getString("status");
			updatedOrdersMap.put(orderId, status);
		}


		if(MiscMethods.checkIfAdmin(clientUserId)) {
			if(removeFromCurrentOrdersAndSaveToLogs(updatedOrdersMap)) {
				jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.REMOVE_FROM_CURRENT_ORDERS_AND_UPDATE_LOGS_SUCCESS);	
			}
			else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.GENERIC_FAILURE);
		}
		else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.CLIENT_IS_NOT_ADMIN);

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

	private boolean removeFromCurrentOrdersAndSaveToLogs(Map<Integer, 
			String> updatedOrdersMap) {
		Connection con = MiscMethods.establishDatabaseConnection();
		String sql1 = "INSERT INTO " + ServerConstants.
				DB_ORDER_HISTORY_DETAILS_TABLE +  "(order_id, user_id," +
				" price_of_order, time_order_placed, " +
				" paying_with_credit_card, delivery_location, " +
				"number_of_free_pearl_milk_tea_used) SELECT order_id, user_id," +
				" price_of_order, time_order_placed, " +
				" paying_with_credit_card, delivery_location, " +
				"number_of_free_pearl_milk_tea_used FROM " +
				ServerConstants.DB_CURRENT_ORDER_DETAILS_TABLE + " " +
				"WHERE order_id = ?";
		String sql2 = "UPDATE " + ServerConstants.DB_ORDER_HISTORY_DETAILS_TABLE
				+ " SET status = ? WHERE order_id = ?";
		String sql5 = "DELETE FROM " + ServerConstants.DB_CURRENT_ORDER_DETAILS_TABLE + " WHERE order_id = ?";
		String sql6 = "INSERT INTO " + 
				ServerConstants.DB_ORDER_HISTORY_TABLE + " (order_id, menu_id, quantity) " +
				"SELECT order_id, menu_id, quantity FROM " + ServerConstants.DB_CURRENT_ORDER_TABLE + 
				" WHERE order_id = ?";
		String sql7 = "DELETE FROM " + ServerConstants.DB_CURRENT_ORDER_TABLE + " WHERE order_id = ?";
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;

		try {
			PreparedStatement ps1 = con.prepareStatement(sql1);
			PreparedStatement ps2 = con.prepareStatement(sql2);
			PreparedStatement ps5 = con.prepareStatement(sql5);
			PreparedStatement ps6 = con.prepareStatement(sql6);
			PreparedStatement ps7 = con.prepareStatement(sql7);
			for(Map.Entry<Integer, String> entry : updatedOrdersMap.entrySet()) {
				int orderId = entry.getKey();
				ps1.setInt(1, orderId);
				ps1.addBatch();
				ps6.setInt(1, orderId);
				ps6.addBatch();
			}
			ps1.executeBatch();
			ps6.executeBatch();
			for(Map.Entry<Integer, String> entry : updatedOrdersMap.entrySet()) {
				ps2.setString(1, entry.getValue());
				ps2.setInt(2, entry.getKey());
				ps2.addBatch();
				if(entry.getValue().equals("incomplete")) {
					ps3 = con.prepareStatement("SELECT user_id FROM " + 
							ServerConstants.DB_ORDER_HISTORY_DETAILS_TABLE + " WHERE order_id = ?");
					ps3.setInt(1, entry.getKey());
					ResultSet rs = ps3.executeQuery();
					if(rs.next()) {
						int userId = rs.getInt(1);
						//						if(removeStamps(userId, entry.getKey())) {
						if(decrementPMTPurchasedCounter(userId, entry.getKey())) {
							ps4 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
									" SET failed_to_pay_counter = failed_to_pay_counter + 1 WHERE user_id = ?");
							ps4.setInt(1, userId);
							ps4.executeUpdate();
						}
						//}
					}
				}
			}
			ps2.executeBatch();
			for(Map.Entry<Integer, String> entry : updatedOrdersMap.entrySet()) {
				ps5.setInt(1, entry.getKey());
				ps5.addBatch();
			}
			ps5.executeBatch();
			for(Map.Entry<Integer, String> entry : updatedOrdersMap.entrySet()) {
				ps7.setInt(1, entry.getKey());
				ps7.addBatch();
			}
			ps7.executeBatch();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return false;
	}

	private boolean decrementPMTPurchasedCounter(int userId, int orderNumber) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		int numberOfPMTIncremented = 0;
		try {
			ps1 = con.prepareStatement("SELECT quantity FROM " + 
					ServerConstants.DB_CURRENT_ORDER_TABLE + " WHERE order_id = ? AND menu_id = ?");
			ps1.setInt(1, orderNumber);
			ps1.setInt(2, 1);
			ResultSet rs = ps1.executeQuery();
			if(rs.next()) {
				numberOfPMTIncremented = rs.getInt(1);
			}

			ps2 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE 
					+ " SET number_pmt_bought_counter = number_pmt_bought_counter - ? WHERE user_id = ?");
			ps2.setInt(1, numberOfPMTIncremented);
			ps2.setInt(2, userId);
			ps2.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	/* not in first release */

	//		private boolean removeStamps(int userId, int orderNumber) {
	//			Connection con = MiscMethods.establishDatabaseConnection();
	//			PreparedStatement ps1 = null;
	//			PreparedStatement ps2 = null;
	//			PreparedStatement ps3 = null;
	//			int numberOfStampsReceived = 0;
	//			int newNumberOfStamps = 0;
	//			try {
	//				ps1 = con.prepareStatement("SELECT quantity FROM " + 
	//						ServerConstants.DB_CURRENT_ORDER_TABLE + " WHERE order_id = ? AND menu_id = ?");
	//				ps1.setInt(1, orderNumber);
	//				ps1.setInt(2, 1);
	//				ResultSet rs = ps1.executeQuery();
	//				if(rs.next()) {
	//					numberOfStampsReceived = rs.getInt(1);
	//				}
	//				int numOfStamps = MiscMethods.numberOfStampsUserHas(userId);
	//				if(numOfStamps >= numberOfStampsReceived) {
	//					newNumberOfStamps = numOfStamps - numberOfStampsReceived;
	//				}
	//				else {
	//					newNumberOfStamps = numOfStamps + 10 - numberOfStampsReceived;
	//					ps3 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
	//							" SET free_pearl_milk_tea_credits_counter = " +
	//							"free_pearl_milk_tea_credits_counter - 1 WHERE user_id = ?");
	//					ps3.setInt(1, userId);
	//					ps3.executeUpdate();
	//				}
	//				ps2 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE 
	//						+ " SET stamp_card_counter = ? WHERE user_id = ?");
	//				ps2.setInt(1, newNumberOfStamps);
	//				ps2.setInt(2, userId);
	//				ps2.executeUpdate();
	//				return true;
	//			} catch (SQLException e) {
	//				causeOfFailure = e.getMessage();
	//				e.printStackTrace();
	//			}
	//			return false;
	//		}


}
