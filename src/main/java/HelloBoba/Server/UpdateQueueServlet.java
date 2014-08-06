//package HelloBoba.Server;
//
//
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.json.*;
//
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.twilio.sdk.TwilioRestClient;
//import com.twilio.sdk.TwilioRestException;
//import com.twilio.sdk.resource.factory.SmsFactory;
//import com.twilio.sdk.resource.instance.Sms;
//
//@WebServlet (value="/updatequeue", name="Update-Queue-Servlet")
//public class UpdateQueueServlet extends HttpServlet{
//
//	private static final String ACCOUNT_SID = "ACe65b5376193513afd04d16decdc82f06";
//	private static final String AUTH_TOKEN = "996cdd9d9764576b3ca7a36b7a89c1a3";
//	private int numberUsersAccepted;
//	private int numberTextsSent;
//
//	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
//		String jsonReqString = "";
//		int clientUserId = 0;
//		int numberToAccept = 0;
//
//		try {
//			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
//			if(inFromClient != null) {
//				jsonReqString = inFromClient.readLine();
//			}
//		}catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		JSONObject jsonObj = new JSONObject(jsonReqString);
//
//		clientUserId = jsonObj.getInt("client_user_id");
//		numberToAccept = jsonObj.getInt("number_to_accept"); 
//
//		JSONObject jsonResObj = new JSONObject();
//
//		numberTextsSent = 0;
//		numberUsersAccepted = 0;
//
//		if(MiscMethods.checkIfAdmin(clientUserId)) {
//			if(updateQueue(numberToAccept)) {
//				jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.UPDATE_QUEUE_SUCCESS);
//				jsonResObj.put(ServerConstants.NUM_ACCEPTED, numberUsersAccepted);
//				jsonResObj.put(ServerConstants.NUM_TEXTS_SENT, numberTextsSent);
//			}
//			else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.GENERIC_FAILURE);
//		}
//
//		else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.CLIENT_IS_NOT_ADMIN);
//
//		response.setContentType("application/json");
//		String jsonResString = jsonResObj.toString();
//
//		response.setBufferSize(jsonResString.length());  //lets client know how long
//		OutputStream outputStream;
//		try {
//			outputStream = response.getOutputStream();
//			outputStream.write(jsonResString.getBytes());
//			outputStream.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//	}
//
//	private boolean updateQueue(int numberToAccept) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps = null;
//		PreparedStatement ps2 = null;
//		PreparedStatement ps3 = null;
//		PreparedStatement ps4 = null;
//		PreparedStatement ps5 = null;
//		PreparedStatement ps6 = null;
//		PreparedStatement ps7 = null;
//		LinkedList<Integer> queue = new LinkedList<Integer>(); //holds the primary key of table rows
//
//		try {
//			ps = con.prepareStatement("SELECT * FROM " + 
//					ServerConstants.DB_USER_QUEUE_TABLE + " WHERE is_affiliated = 1 AND " +
//					"has_credit_card = 1 AND fb_connect = 1 ORDER BY user_queue_id DESC");
//			ResultSet rs = ps.executeQuery();
//			while(rs.next()) {
//				queue.add(rs.getInt("user_queue_id"));
//			}
//			ps2 = con.prepareStatement("SELECT * FROM " + 
//					ServerConstants.DB_USER_QUEUE_TABLE + " WHERE is_affiliated = 1 AND " +
//					"has_credit_card = 0 AND fb_connect = 1 ORDER BY user_queue_id DESC");
//			ResultSet rs2 = ps2.executeQuery();
//			while(rs2.next()) {
//				queue.add(rs2.getInt("user_queue_id"));
//			}
//			ps3 = con.prepareStatement("SELECT * FROM " + 
//					ServerConstants.DB_USER_QUEUE_TABLE + " WHERE is_affiliated = 1 AND " +
//					"has_credit_card = 0 AND fb_connect = 0 ORDER BY user_queue_id DESC");
//			ResultSet rs3 = ps3.executeQuery();
//			while(rs3.next()) {
//				queue.add(rs3.getInt("user_queue_id"));
//			}
//			ps4 = con.prepareStatement("SELECT * FROM " + 
//					ServerConstants.DB_USER_QUEUE_TABLE + " WHERE is_affiliated = 1 AND " +
//					"has_credit_card = 1 AND fb_connect = 0 ORDER BY user_queue_id DESC");
//			ResultSet rs4 = ps4.executeQuery();
//			while(rs4.next()) {
//				queue.add(rs4.getInt("user_queue_id"));
//			}
//			String name;
//			String phoneNumber;
//			boolean works = true;
//			int queuedUserId = 0;
//			if(!queue.isEmpty()) {
//				int j = 0;
//				while(j < numberToAccept && queue.size() > 0) {
//					int rowNumber = queue.pop();
//					ps5 = con.prepareStatement("SELECT * FROM " + 
//							ServerConstants.DB_USER_QUEUE_TABLE + " WHERE user_queue_id = ?");
//					ps5.setInt(1, rowNumber);
//					ResultSet result = ps5.executeQuery();
//					if(result.next()) {
//						queuedUserId = result.getInt("user_id");
//					}
//					ps6 = con.prepareStatement("DELETE FROM " + ServerConstants.DB_USER_QUEUE_TABLE +
//							" WHERE user_queue_id = ?");
//					ps6.setInt(1, rowNumber);
//					ps6.executeUpdate();
//					ps7 = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
//							" SET in_queue = ? WHERE user_id = ?");
//					ps7.setInt(1, 0);
//					ps7.setInt(2, queuedUserId);
//					ps7.executeUpdate();
//					numberUsersAccepted++;
//					name = MiscMethods.getNameCorrespondingToUserId(queuedUserId);
//					phoneNumber = MiscMethods.getPhoneNumberCorrespondingToUserId(queuedUserId);
//					if(sendQueueRelatedSMS(phoneNumber, name)) {
//						j++;
//						numberTextsSent++;
//					}
//					else {
//						works = false;
//						break;
//					}
//				}
//			}
//
//			if(works) {
//				return true;
//			}
//			else {
//				return false;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//	private boolean sendQueueRelatedSMS(String phoneNumber, String customerName) {
//		TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
//
//		// Build a filter for the SmsList
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("Body", "Hey " + customerName + ", the wait is over! Your HelloBoba account is now ready to order." +
//				" Please do not respond to this text.");
//		params.put("To", "+" + phoneNumber);
//		params.put("From", "+14088907380");
//
//		SmsFactory messageFactory = client.getAccount().getSmsFactory();
//		Sms message;
//		try {
//			message = messageFactory.create(params);
//			return true;
//		} catch (TwilioRestException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//}
//
