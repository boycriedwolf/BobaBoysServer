package HelloBoba.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.Iterator;
import java.util.List;

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
import com.sun.net.httpserver.HttpServer;

@WebServlet (value="/retrievecreditcards", name="Retrieve-Credit-Cards-Servlet")
public class RetrieveCreditCardsServlet extends HttpServlet{

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
		//JSONRPC2Request jsonReq = null;
		JSONObject jsonObj = null;

		//		try {
		jsonObj = new JSONObject(jsonReqString);
		//		} catch (JSONException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}

		int clientUserId = 0;
		int customerUserId = 0;
		//		try {
		clientUserId = jsonObj.getInt("client_user_id");
		customerUserId = jsonObj.getInt("customer_user_id");
		//		} catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		JSONObject jsonResObj = new JSONObject();
		JSONArray jsonCards = new JSONArray();
		String userCustomerId = MiscMethods.getCustomerId(customerUserId);

		//		try {
		if(MiscMethods.checkIfAdmin(clientUserId)) {
			jsonCards = retrieveCustomerCreditCards(userCustomerId);
			try {
				jsonCards.getJSONObject(1);
				jsonResObj.put("cards", jsonCards);
			}
			catch (JSONException e) {
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

	private JSONArray retrieveCustomerCreditCards(String userCustomerId) {
		Stripe.apiKey = "sk_test_CY8QQMarcq8pB4nhhQB8dZ6g";	
		Customer cu;
		JSONArray jsonCards = new JSONArray();
		try {
			cu = Customer.retrieve(userCustomerId);
			String defaultCardId = cu.getDefaultCard();
			CustomerCardCollection listOfCards = cu.getCards();
			List<Card> customerCardsList = listOfCards.getData();
			Iterator<Card> it = customerCardsList.iterator();
			while(it.hasNext()) {
				Card card = it.next();

				JSONObject jsonCard = new JSONObject();
				jsonCard.put("last4", card.getLast4());
				jsonCard.put("exp_month", card.getExpMonth());
				jsonCard.put("exp_year", card.getExpYear());
				if(card.getId().equals(defaultCardId)) {
					jsonCard.put("default_card", true);
				}
				else jsonCard.put("default_card", false);
				jsonCards.put(jsonCard);
			}
			return jsonCards;
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
			causeOfFailure = e.getMessage();
			e.printStackTrace();
		} catch (APIException e) {
			causeOfFailure = e.getMessage();
			e.printStackTrace();
		}
		//		} catch (JSONException e) {
		//			causeOfFailure = e.getMessage();
		//			e.printStackTrace();
		//		}
		return jsonCards;

	}



}

