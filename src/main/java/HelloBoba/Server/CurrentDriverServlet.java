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
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.*;


@WebServlet (value="/currentdriver", name="Current-Driver-Servlet")
public class CurrentDriverServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		String jsonReqString = "";
		String day = "";
		int time = 0;  //1:30pm is 1330
		String timeBlock = "";
		int driverId = 0;
		
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if(inFromClient != null) {
				jsonReqString = inFromClient.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jsonObj = new JSONObject(jsonReqString);
		day = jsonObj.getString("day");
		time = jsonObj.getInt("time");
		timeBlock = convertTimeToTimeBlock(time);
		driverId = findCurrentDriver(day, timeBlock);
		
		JSONObject jsonResObj = new JSONObject();

		if(checkIfDriverIsAdmin(driverId)) {
			jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.RETRIEVE_DRIVER_SUCCESS);
			jsonResObj.put(ServerConstants.DRIVER_ID, driverId);
		} else jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.DRIVER_IS_NOT_ADMIN);



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

//	public int convertDayToInt(String day) {
//		if(day.equalsIgnoreCase("Monday")) 
//			return 1;
//		else if (day.equalsIgnoreCase("Tuesday"))
//			return 2;
//		else if (day.equalsIgnoreCase("Wednesday"))
//			return 3;
//		else if (day.equalsIgnoreCase("Thursday"))
//			return 4;
//		else if (day.equalsIgnoreCase("Friday"))
//			return 5;
//		else if (day.equalsIgnoreCase("Saturday"))
//			return 6;
//		else return 7;
//		
//	}
	
	public String convertTimeToTimeBlock(int time) {
		time = time - 1200;
		if(time < 30) 
			return "1200";
		else if(time<60)
			return "1230";
		else if(time<90)
			return "100";
		else if(time<120)
			return "130";
		else if(time<150)
			return "200";
		else if(time<180)
			return "230";
		else if(time<210)
			return "300";
		else if(time<240)
			return "330";
		else if(time<270)
			return "400";
		else if(time<300)
			return "430";
		else if(time<330)
			return "500";
		else if(time<360)
			return "530";
		else if(time<390)
			return "600";
		else if(time<420)
			return "630";
		else if(time<450)
			return "700";
		else if(time<480)
			return "730";
		else if(time<510)
			return "800";
		else if(time<540)
			return "830";
		else if(time<570)
			return "900";
		else return "930";
		
	}
	
	public int findCurrentDriver(String day, String timeBlock) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		ResultSet rs;
		int driverId = 0;
		
		try {
			ps = con.prepareStatement("SELECT " + timeBlock + " FROM " + ServerConstants.DB_DRIVER_SCHEDULE_TABLE + " WHERE day = ?");
			ps.setString(1, day);
			rs = ps.executeQuery();
			if(rs.next()) {
				driverId = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return driverId;
	}
	
	public int getUserIdOfDriver(int driverId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		ResultSet rs;
		int userId = 0;
		
		try {
			ps = con.prepareStatement("SELECT userId FROM " + ServerConstants.DB_DRIVER_TABLE + " WHERE driverId = ?");
			ps.setInt(1, driverId);
			rs = ps.executeQuery();
			if(rs.next()) {
				userId = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return userId;
	}
	
	
	public boolean checkIfDriverIsAdmin(int driverId) {
		Connection con = MiscMethods.establishDatabaseConnection();
		PreparedStatement ps;
		ResultSet rs;
		int userId = getUserIdOfDriver(driverId);
		int adminAccount = 0;
		
		try {
			ps = con.prepareStatement("SELECT admin_account FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			if(rs.next()) {
				adminAccount = rs.getInt(1);	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(adminAccount == 0) {
			return false;
		} else return true;
	}
	
	
	
	
	
	
	
	
	
	
	
}