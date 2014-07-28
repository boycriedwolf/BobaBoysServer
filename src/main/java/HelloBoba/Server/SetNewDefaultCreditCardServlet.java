package HelloBoba.Server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.stripe.model.CustomerCardCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/setnewdefaultcreditcard", name="Set-New-Default-Credit-Card-Servlet")
public class SetNewDefaultCreditCardServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		String last4 = "";
		int expMonth = 0;
		int expYear = 0;
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

		last4 = jsonObj.getString("last4");
		expMonth = jsonObj.getInt("exp_month");
		expYear = jsonObj.getInt("exp_year");
		userId = jsonObj.getInt("user_id");
		
		JSONObject jsonResObj = new JSONObject();
		if(setNewDefaultCustomerCreditCard(userId, last4, expMonth, expYear)) {
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.SET_NEW_DEFAULT_CREDIT_CARD_SUCCESS);
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
	private boolean setNewDefaultCustomerCreditCard(int userId, String last4, int expMonth, int expYear) {
		Stripe.apiKey = "sk_test_CY8QQMarcq8pB4nhhQB8dZ6g";

		try {
			Customer cu = Customer.retrieve(TestMiscMethods.getCustomerId(userId));
			Map<String, Object> updateParams = new HashMap<String, Object>();
			CustomerCardCollection listOfCards = cu.getCards();
			List<Card> customerCardsList = listOfCards.getData();
			Iterator<Card> it = customerCardsList.iterator();
			while(it.hasNext()) {
				Card card = it.next();
				if((card.getLast4() == last4) && (card.getExpMonth() == expMonth) && (card.getExpYear() == expYear)) {
					updateParams.put("default_card", card.getId());
				}
			}
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




}





