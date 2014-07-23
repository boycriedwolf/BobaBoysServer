package HelloBoba.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.*;


@WebServlet (value="/accountcreation", name="Account-Creation-Servlet")
public class AccountCreationServlet extends HttpServlet{

	private String causeOfFailure;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		String jsonReqString = "";

		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if(inFromClient != null) {
				jsonReqString = inFromClient.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject jsonObj = null;
		jsonObj = new JSONObject(jsonReqString);

		//		try {
		//		} catch (JSONException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}
		String name = "";
		String email = "";
		String password = "";
		String phoneNumber = "";
		int userId;

		name = jsonObj.getString("name");
		email = jsonObj.getString("email");
		password = jsonObj.getString("password");
		phoneNumber = jsonObj.getString("phone_number");

		//		try {
		//		} catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		JSONObject jsonResObj = new JSONObject();

		//		try {
		if(checkIfValidEmail(email)) {
			if(checkIfValidPassword(password)) {
				if(checkIfEmailAlreadyInDB(email)) {
					if(checkIfPhoneNumberAlreadyInDB(phoneNumber)) {
						userId = accountCreation(name, email, password, phoneNumber);
						if(userId != 0) {
							jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.LOGIN_CREATE_SUCCESS);
							jsonResObj.put(ServerConstants.USER_ID, userId);
						}
						else jsonResObj.put(ServerConstants.REQUEST_STATUS, causeOfFailure);
					} else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.PHONE_NUMBER_EXISTS_IN_DB); 
				}
				else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.EMAIL_EXISTS_IN_DB);
			}
			else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.INVALID_PASSWORD);
		}
		else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.INVALID_EMAIL);


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

	public int accountCreation(String name, String email, String password, String phoneNumber) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps1, ps2;
		int userId = 0;
		try {
			String sql = "INSERT INTO " + ServerConstants.DB_USER_TABLE + 
					"(name, user_email, user_password, phone_number, admin_account, in_queue," +
					" failed_to_pay_counter, free_pearl_milk_tea_credits_counter, " +
					"stamp_card_counter) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
			ps1 = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps1.setString(1, name);
			ps1.setString(2, email);
			ps1.setString(3, password);
			ps1.setString(4, phoneNumber);
			ps1.setInt(5, 0);
			ps1.setInt(6, 1);
			ps1.setInt(7, 0);
			ps1.setInt(8, 0);
			ps1.setInt(9, 0);
			ps1.executeUpdate();
			ResultSet rs = ps1.getGeneratedKeys();
			if(rs.next()) {
				userId = rs.getInt(1);
			}
			ps2 = con.prepareStatement("INSERT INTO " + ServerConstants.DB_USER_QUEUE_TABLE + 
					"(user_id, has_credit_card, is_affiliated, fb_connect) VALUES(?, ?, ?, ?)");
			ps2.setInt(1, userId);
			ps2.setInt(2, 0);
			ps2.setInt(3, 0);
			ps2.setInt(4, 0);
			ps2.executeUpdate();
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
		return userId;
	}

	private boolean checkIfEmailAlreadyInDB(String email) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("SELECT * FROM " + ServerConstants.DB_USER_TABLE + 
					" WHERE user_email = ?");
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) { //account with this email already exists
				return false;
			}
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
		return true;
	}

	private boolean checkIfValidEmail(String email) {
		//I have no idea what this does but supposedly it makes sure there isn't syntax errors in email
		final Pattern rfc2822 = Pattern.compile(
				"^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)" +
				"*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
		if(!rfc2822.matcher(email).matches()) {
			causeOfFailure = "invalid_email";
			return false;
		}
		return true;
	}

	private boolean checkIfValidPassword(String password) {
		Pattern p = Pattern.compile("[^a-zA-Z0-9]");
		if(!p.matcher(password).find()) { //checks if password doesnt contain special chars
			if(password.length() < 4) {
				causeOfFailure = "password_too_short";
				return false;
			}
			else return true;
		}
		else {
			causeOfFailure = "invalid_password";
			return false;
		}
	}

	private boolean checkIfPhoneNumberAlreadyInDB(String phoneNumber) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("SELECT * FROM " + ServerConstants.DB_USER_TABLE + 
					" WHERE phone_number = ?");
			ps.setString(1, phoneNumber);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) { //account with this email already exists
				return false;
			}
		} catch (SQLException e) {
			causeOfFailure = e.getLocalizedMessage();
			e.printStackTrace();
		}
		return true;
	}

}
