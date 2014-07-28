package HelloBoba.Server;


import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStream;

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

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;

@WebServlet (value="/notifyviasms", name="Notify-Via-SMS-Servlet")
public class NotifyViaSMSServlet extends HttpServlet{

	private static final String ACCOUNT_SID = "ACe65b5376193513afd04d16decdc82f06";
	private static final String AUTH_TOKEN = "996cdd9d9764576b3ca7a36b7a89c1a3";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		int clientUserId = 0;
		int orderId = 0;
	
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
		orderId = jsonObj.getInt("order_id");
	
		int customerUserId = MiscMethods.getUserIdCorrespondingToOrderId(orderId);
		String customerName = MiscMethods.getNameCorrespondingToUserId(customerUserId);
		String phoneNumber = MiscMethods.getPhoneNumberCorrespondingToUserId(customerUserId);
		JSONObject jsonResObj = new JSONObject();
		
		if(MiscMethods.checkIfAdmin(clientUserId)) {
			if(sendDeliveryRelatedSMS(phoneNumber, customerName)) {
				jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.SMS_SEND_SUCCESS);
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

	private boolean sendDeliveryRelatedSMS(String phoneNumber, String customerName) {
		TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

		// Build a filter for the SmsList
		Map<String, String> params = new HashMap<String, String>();
		params.put("Body", "Hey " + customerName + "! Your HelloBoba driver has arrived." +
				" Please do not respond to this text.");
		params.put("To", "+" + phoneNumber);
		params.put("From", "+14088907380");

		SmsFactory messageFactory = client.getAccount().getSmsFactory();
		Sms message;
		try {
			message = messageFactory.create(params);
			return true;
		} catch (TwilioRestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}





