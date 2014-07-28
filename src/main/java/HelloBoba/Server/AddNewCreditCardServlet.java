package HelloBoba.Server;


import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.HashMap;
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
import com.stripe.model.Token;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/addnewcreditcard", name="Add-New-Credit-Card-Servlet")
public class AddNewCreditCardServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		String stripeTokenString = "";
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

		stripeTokenString = jsonObj.getString("stripe_token");
		userId = jsonObj.getInt("user_id");
		JSONObject jsonResObj = new JSONObject();

		
		if(createNewCustomerCreditCard(stripeTokenString, userId) && setHasCreditCardInUserQueue(userId)) {
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.CARD_ADD_SUCCESS);
			MiscMethods.giveFreePearlMilkTea(userId, 1);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Adds a new credit card for Stripe customer and makes it default card
	 */
	private boolean createNewCustomerCreditCard(String stripeTokenString, int userId) {
		Stripe.apiKey = "sk_test_CY8QQMarcq8pB4nhhQB8dZ6g";

		Map<String, Object> customerParams = new HashMap<String, Object>();
		Map<String, Object> updateParams = new HashMap<String, Object>();
		
		try {
			Customer cu = Customer.retrieve(MiscMethods.getCustomerId(userId));
			Token stripeToken = Token.retrieve(stripeTokenString);
		
			customerParams.put("card", stripeToken.getId());
			Card newCard = cu.createCard(customerParams);
			
			updateParams.put("default_card", newCard.getId());
			cu.update(updateParams);
			return true;
		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (InvalidRequestException e) {
			e.printStackTrace();
		} catch (APIConnectionException e) {
			e.printStackTrace();
		} catch (CardException e) {
			e.printStackTrace();
		} catch (APIException e) {
			e.printStackTrace();
		}
		return false;

	}

	private boolean setHasCreditCardInUserQueue(int userId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_QUEUE_TABLE + 
					" SET has_credit_card = ? WHERE user_id = ?");
			ps.setInt(1, 1);
			ps.setInt(2, userId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}



}






