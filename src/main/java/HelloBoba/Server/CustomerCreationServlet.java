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
import com.stripe.exception.*;

import com.stripe.model.Customer;
import com.stripe.model.Token;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/customercreation", name="Customer-Creation-Servlet")
public class CustomerCreationServlet extends HttpServlet{

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

		//		try {
		jsonObj = new JSONObject(jsonReqString);
		//		} catch (JSONException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}

		String stripeTokenString = "";
		int userId = 0;
		//		try {
		stripeTokenString = jsonObj.getString("stripe_token");
		userId = jsonObj.getInt("user_id");
		//		} catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		JSONObject jsonResObj = new JSONObject();
		//		try {
		if(customerCreation(stripeTokenString, userId) && setHasCreditCardInUserQueue(userId)) {
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.CUSTOMER_CREATE_SUCCESS);
			MiscMethods.giveFreePearlMilkTea(userId, 1);
		}
		else jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
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

	private boolean customerCreation(String stripeTokenString, int userId) {
		Stripe.apiKey = "sk_test_CY8QQMarcq8pB4nhhQB8dZ6g";

		Map<String, Object> customerParams = new HashMap<String, Object>();
		customerParams.put("description", "Customer for user id: " + userId);

		Customer customer;
		String customerId = "";

		try {
			Token stripeToken = Token.retrieve(stripeTokenString);
			customerParams.put("card", stripeToken.getId());
			customer = Customer.create(customerParams);		
			customerId = customer.getId();	
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

		if(saveCustomerIdToDatabase(customerId, userId)) {
			return true;
		}
		else return false;

	}

	private boolean saveCustomerIdToDatabase(String customerId, int userId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
					" SET user_customer_id = ? WHERE user_id = ?");
			ps.setString(1, customerId);
			ps.setInt(2, userId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
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
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
		return false;
	}




	//	/*
	//	 * Used to parse through the http post body
	//	 */
	//
	//	private void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
	//		if(query != null) {
	//			String pairs[] = query.split("[&]");
	//			for(String pair: pairs) {
	//				String param[] = pair.split("[=]");
	//				String key = null;
	//				String value = null;
	//				if(param.length > 0) {
	//					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
	//
	//				}
	//				if(param.length > 1) {
	//					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
	//				}
	//				if(parameters.containsKey(key)) {
	//					Object obj = parameters.get(key);
	//					if(obj instanceof List<?>) {
	//						List<String> values = (List<String>)obj;
	//						values.add(value);
	//					}
	//					else if(obj instanceof String) {
	//						List<String> values = new ArrayList<String>();
	//						values.add((String)obj);
	//						values.add(value);
	//						parameters.put(key, values);
	//					}
	//				}
	//				else {
	//					parameters.put(key, value);
	//				}
	//			}
	//		}
	//	}
}






