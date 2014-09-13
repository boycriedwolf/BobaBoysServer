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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/validatelogin", name="Validate-Login-Servlet")
public class ValidateLoginServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";
		String email = "";
		String password = "";
		JSONObject jsonResObj = new JSONObject();
		int userId, inQueue;
		
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

		email = jsonObj.getString("email");
		password = jsonObj.getString("password");

		boolean success = validateLogin(email, password);

		if(checkIfEmailInDB(email)) {
			if(success) {
				jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.LOGIN_SUCCESS);
				//jsonResObj.put(ServerConstants.USER_ID, userId);
				
			}
			else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.GENERIC_FAILURE);
		}
		else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.EMAIL_DOESNT_EXIST);
		
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

	private boolean checkIfEmailInDB(String email) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("SELECT * FROM " + ServerConstants.DB_USER_TABLE + 
					" WHERE user_email = ?");
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) { //account with this email already exists
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean validateLogin(String email, String password) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		int userId = 0;
		int inQueue;
		String databasePassword = "";
		List<Integer> returnList = new ArrayList<Integer>();
		try {
			ps = con.prepareStatement("SELECT user_password FROM " + ServerConstants.DB_USER_TABLE + 
					" WHERE user_email = ?");
			ps.setString(1, email);
			ResultSet result = ps.executeQuery();
			if(result.next()) {
				databasePassword = result.getString(1);
			}
			if(databasePassword.equals(password)) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}




}
