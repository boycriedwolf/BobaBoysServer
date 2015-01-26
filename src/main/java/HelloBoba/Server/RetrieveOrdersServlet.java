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

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		int clientUserId = 0;
		JSONArray jsonOrderArray = new JSONArray();
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

		if(MiscMethods.checkIfAdmin(clientUserId)) {
			jsonOrderArray = retrieveOrders();
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.ORDERS_EXIST);
			jsonResObj.put("order", jsonOrderArray);
		}
		else {
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.CLIENT_IS_NOT_ADMIN);
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


	//jsonarray holds jsonobjects with order details, each jsonobject represents one current order

	private JSONArray retrieveOrders() {
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
		JSONArray jsonOrderArray = new JSONArray();

		try {
			ps = con.prepareStatement("SELECT * FROM " + 
					ServerConstants.DB_CURRENT_ORDER_DETAILS_TABLE);
			ResultSet result = ps.executeQuery();

			while(result.next()) {
				JSONObject jsonOrderObject = new JSONObject();
				orderId = result.getInt("order_id");
				userId = result.getInt("user_id");

				name = MiscMethods.getNameCorrespondingToUserId(userId);
				priceOfOrder = result.getInt("price_of_order"); //cents		
				timeOrderPlaced = result.getString("time_order_placed");
				payingWithCreditCard = result.getInt("paying_with_credit_card");
				deliveryLocation = result.getString("delivery_location");


				jsonOrderObject.put("order_id", orderId);
				jsonOrderObject.put("name", name);
				jsonOrderObject.put("order", retrieveCurrentOrders(orderId));
				jsonOrderObject.put("paying_with_credit_card", payingWithCreditCard);
				jsonOrderObject.put("price_of_order", priceOfOrder);
				jsonOrderObject.put("time_order_placed", timeOrderPlaced);
				jsonOrderObject.put("delivery_location", deliveryLocation);
				jsonOrderArray.put(jsonOrderObject);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return jsonOrderArray;
	}

	private JSONObject retrieveCurrentOrders(int orderId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		int menuId;
		String menuItemName;
		int quantity = 0;
		JSONObject jsonOrder = new JSONObject();
		try {
			ps = con.prepareStatement("SELECT menu_id, quantity FROM " + ServerConstants.DB_CURRENT_ORDER_TABLE + " WHERE order_id = ?");
			ps.setInt(1, orderId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				menuId = rs.getInt("menu_id"); 
				menuItemName = MiscMethods.getNameForMenuId(menuId);
				quantity = rs.getInt("quantity");
				jsonOrder.put(menuItemName, quantity);
			}
			return jsonOrder;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonOrder;
	}


}

