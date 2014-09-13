//package HelloBoba.Server;
//
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.text.Format;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Iterator;
//
//import org.json.*;
//
//public class TestMiscMethods {
//
//
//	public static Connection establishTestDatabaseConnection() {
//		try {
//			Class.forName("com.mysql.jdbc.Driver").newInstance();
//		} catch (ClassNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String connectionUrl = "jdbc:mysql://helloboba.cjqc09pyraps.us-east-1.rds.amazonaws.com:3306/test";
//		Connection con = null;
//		try {
//			con = DriverManager.getConnection(connectionUrl, "admin", "robashen123");
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return con;
//	}
//	
//	public static String getCustomerId(int userId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		String customerId = "";
//		try {
//			ps = con.prepareStatement("SELECT user_customer_id FROM user WHERE user_id=?");
//			ps.setInt(1, userId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				customerId = rs.getString(1);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return customerId;
//	}
//
//	public static String getNameCorrespondingToUserId(int userId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		String name = "";
//		try {
//			ps = con.prepareStatement("SELECT name FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
//			ps.setInt(1, userId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				name = rs.getString(1);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return name;
//	}
//
//	public static String getPhoneNumberCorrespondingToUserId(int userId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		String phoneNumber = "";
//		try {
//			ps = con.prepareStatement("SELECT phone_number FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
//			ps.setInt(1, userId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				phoneNumber = rs.getString(1);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return phoneNumber;
//	}
//
//	public static String convertTime(long time) {
//		Date date = new Date(time);
//		Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		return format.format(date).toString();
//	}
//
//	public static int priceOfPearlMilkTea() {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		int price = 0;
//
//		try {
//			ps = con.prepareStatement("SELECT price_of_order FROM " + ServerConstants.DB_MENU_TABLE + " WHERE name = ?");
//			ps.setString(1, "pearl_milk_tea");
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				price = rs.getInt(1);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return price;
//	}
//
//	public static void giveFreePearlMilkTea(int userId, int numOfFreePMT) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		int numberOfFreePMT = numberOfFreePearlMilkTeaUserHas(userId);
//		int newNumberOfFreePMT = numberOfFreePMT + numOfFreePMT;
//		try {
//			ps = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
//					" SET free_pearl_milk_tea_credits_counter = " +
//					newNumberOfFreePMT + " WHERE user_id = ?");
//			ps.setInt(1, userId);
//			ps.executeUpdate();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static void removeFreePearlMilkTeaUsed(int userId, int numberOfFreePearlMilkTeaUsed) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		int numberOfFreePMT = numberOfFreePearlMilkTeaUserHas(userId);
//		int newNumberOfFreePMT = numberOfFreePMT - numberOfFreePearlMilkTeaUsed;
//		try {
//			ps = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
//					" SET free_pearl_milk_tea_credits_counter = " +
//					newNumberOfFreePMT + " WHERE user_id = ?");
//			ps.setInt(1, userId);
//			ps.executeUpdate();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static int numberOfPearlMilkTeaInOrder(JSONObject jsonOrderObj) {
//		Iterator<String> orderKeys = jsonOrderObj.keys(); 
//		int pearlMilkTeaQuantity = 0;
//
//		while(orderKeys.hasNext()) {
//			String menuItemName = orderKeys.next();
//			if(menuItemName.equals("1")) {
//				//				try {
//				pearlMilkTeaQuantity = jsonOrderObj.getInt(menuItemName);
//				//				} catch (JSONException e) {
//				//					// TODO Auto-generated catch block
//				//					e.printStackTrace();
//				//				}	
//			}
//		}
//		return pearlMilkTeaQuantity;
//	}
//
//	public static int numberOfFreePearlMilkTeaUserHas(int userId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		int numFree = 0;
//		try {
//			ps = con.prepareStatement("SELECT free_pearl_milk_tea_credits_counter FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
//			ps.setInt(1, userId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				numFree = rs.getInt(1);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return numFree;
//	}
//
//	/* not in first release */
//
//	//	public static int numberOfStampsUserHas(int userId) {
//	//		Connection con = MiscMethods.establishDatabaseConnection();
//	//		PreparedStatement ps;
//	//		int numStamps = 0;
//	//		try {
//	//			ps = con.prepareStatement("SELECT stamp_card_counter FROM " + ServerConstants.DB_USER_TABLE + " WHERE user_id = ?");
//	//			ps.setInt(1, userId);
//	//			ResultSet rs = ps.executeQuery();
//	//			if(rs.next()) {
//	//				numStamps = rs.getInt(1);
//	//			}
//	//		} catch (SQLException e) {
//	//			// TODO Auto-generated catch block
//	//			e.printStackTrace();
//	//		}
//	//		return numStamps;
//	//	}
//
//	public static boolean checkIfAdmin(int userId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		int adminAccount = 0;
//		try {
//			ps = con.prepareStatement("SELECT admin_account FROM " + ServerConstants.DB_USER_TABLE + 
//					" WHERE user_id = ?");
//			ps.setInt(1, userId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				adminAccount = rs.getInt(1);
//				if(adminAccount == 1) {
//					return true;
//				}
//			}
//			else return false;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//	public static String getNameForMenuId(int menuId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		String menuItemName = "";
//		try {
//			ps = con.prepareStatement("SELECT name FROM " + ServerConstants.DB_MENU_TABLE + 
//					" WHERE menu_id = ?");
//			ps.setInt(1, menuId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				menuItemName = rs.getString(1);
//			}
//			return menuItemName;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return menuItemName;
//	}
//
//	public static int calculatePriceOfMenuId(int menuId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		int price = 0;
//		try {
//			ps = con.prepareStatement("SELECT price_of_order FROM " + ServerConstants.DB_MENU_TABLE + 
//					" WHERE menu_id = ?");
//			ps.setInt(1, menuId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				price = rs.getInt(1);
//			}
//			return price;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return price;
//	}
//
//	public static int getUserIdCorrespondingToOrderId(int orderId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		int userId = 0;
//		try {
//			ps = con.prepareStatement("SELECT user_id FROM " + ServerConstants.DB_CURRENT_ORDER_DETAILS_TABLE + 
//					" WHERE order_id = ?");
//			ps.setInt(1, orderId);
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()) {
//				userId = rs.getInt(1);
//			}
//			return userId;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return userId;
//	}
//
//	public static boolean updateFBToken(int userId, String fbToken) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		try {
//			ps = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_TABLE + 
//					" SET fb_token = ? WHERE user_id = ?");
//			ps.setString(1, fbToken);
//			ps.setInt(2, userId);
//			ps.executeUpdate();
//			return true;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//	public static boolean setFBConnectForUserQueue(int userId) {
//		Connection con = MiscMethods.establishDatabaseConnection();
//		PreparedStatement ps;
//		try {
//			ps = con.prepareStatement("UPDATE " + ServerConstants.DB_USER_QUEUE_TABLE + 
//					" SET fb_connect = ? WHERE user_id = ?");
//			ps.setInt(1, 1);
//			ps.setInt(2, userId);
//			ps.executeUpdate();	
//			return true;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}		
//		return false;
//	}
//
//}
//
