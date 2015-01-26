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

/**
 * @desc Servlet that parses the JSON returned, containing fields for user accounts
 * to save into database. Helper methods like checkIfValidEmail and checkIfValidPassword
 * to verify user inputs are valid 
 * @author Byron Tang byronyugontang@gmail.com
 */

@WebServlet (value="/accountcreation", name="Account-Creation-Servlet")
public class AccountCreationServlet extends HttpServlet{

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    String jsonReqString = "";
    String name = "";
    String email = "";
    String password = "";
    String phoneNumber = "";
    int userId;

    try { //grabs JSON string
      BufferedReader inFromClient = new BufferedReader(new 
          InputStreamReader(request.getInputStream()));
      if(inFromClient != null) {
        jsonReqString = inFromClient.readLine();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    //saves data from JSON into variables
    JSONObject jsonObj = new JSONObject(jsonReqString);
    name = jsonObj.getString("name");
    email = jsonObj.getString("email");
    password = jsonObj.getString("password");
    phoneNumber = jsonObj.getString("phone_number");

    JSONObject jsonResObj = new JSONObject();

    //verify the data is valid
    if(checkIfValidEmail(email)) {
      if(checkIfValidPassword(password)) {
        if(checkIfEmailAlreadyInDB(email)) {
          if(checkIfPhoneNumberAlreadyInDB(phoneNumber)) {
            //add new user to database
            userId = accountCreation(name, email, password, phoneNumber);  
            if(userId != 0) {  //successful insertion into user table
              jsonResObj.put(ServerConstants.REQUEST_STATUS, 
                  ServerConstants.ACCOUNT_CREATE_SUCCESS);
              jsonResObj.put(ServerConstants.USER_ID, userId); //return userId we just made
            }
            else jsonResObj.put(ServerConstants.REQUEST_STATUS, 
                ServerConstants.GENERIC_FAILURE);  //status strings to return to client
          } else {
            jsonResObj.put(ServerConstants.REQUEST_STATUS, 
                ServerConstants.PHONE_NUMBER_EXISTS_IN_DB); 
          }
        }
        else jsonResObj.put(ServerConstants.REQUEST_STATUS, 
            ServerConstants.EMAIL_EXISTS_IN_DB);
      }
      else jsonResObj.put(ServerConstants.REQUEST_STATUS, 
          ServerConstants.INVALID_PASSWORD);
    }
    else jsonResObj.put(ServerConstants.REQUEST_STATUS, 
        ServerConstants.INVALID_EMAIL);

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

  /**
   * @desc 
   * @param string name - name of the new user
   * @param string email - new user's email
   * @param string password - new user's password
   * @param string phoneNumber - new user's phone number
   * @return int - the userId representing the user in the table
   */

  public int accountCreation(String name, String email, String password, 
      String phoneNumber) {
    Connection con = MiscMethods.establishDatabaseConnection();
    PreparedStatement ps1;
    int userId = 0;
    try {
      //create query to insert into user table, some info initialized to 0
      String sql = "INSERT INTO " + ServerConstants.DB_USER_TABLE + 
          "(name, user_email, user_password, phone_number, admin_account," +
          " failed_to_pay_counter, free_pearl_milk_tea_credits_counter, " +
          "stamp_card_counter, number_pmt_bought_counter) " +
          "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
      ps1 = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps1.setString(1, name);
      ps1.setString(2, email);
      ps1.setString(3, password);
      ps1.setString(4, phoneNumber);
      ps1.setInt(5, 0);
      ps1.setInt(6, 0);
      ps1.setInt(7, 0);
      ps1.setInt(8, 0);
      ps1.setInt(9, 0);
      ps1.executeUpdate();
      ResultSet rs = ps1.getGeneratedKeys();
      if(rs.next()) {
        userId = rs.getInt(1);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;
  }

  /**
   * @desc searches the user table for an email matching the one inputted
   * @param string email - make sure the inputted email doesn't already exist in table 
   * @return bool - true if email not in DB, false if it is
   */

  private boolean checkIfEmailAlreadyInDB(String email) {
    Connection con = MiscMethods.establishDatabaseConnection();
    PreparedStatement ps;
    try {
      ps = con.prepareStatement("SELECT user_id FROM " + ServerConstants.DB_USER_TABLE + 
          " WHERE user_email = ?");
      ps.setString(1, email);
      ResultSet rs = ps.executeQuery();
      if(rs.next()) { //account with this email already exists
        return false;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   * @desc analyzes email to check if it follows the words@words format
   * @param string email - the user's email
   * @return bool - true if no special characters
   */

  private boolean checkIfValidEmail(String email) {
    //
    final Pattern rfc2822 = Pattern.compile(
        "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)" +
        "*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
    if(!rfc2822.matcher(email).matches()) {
      return false;
    }
    return true;
  }

  /**
   * @desc analyzes password to make sure there's no special characters and it's length
   * @param string password - the password user input
   * @return bool - false if password too short (less than 4 characters) or if 
   * contains special characters
   */

  private boolean checkIfValidPassword(String password) {
    Pattern p = Pattern.compile("[^a-zA-Z0-9]");
    if(!p.matcher(password).find()) { //checks if password doesnt contain special chars
      if(password.length() < 4) {
        return false;
      }
      else return true;
    }
    else {
      return false;
    }
  }

  /**
   * @desc checks that there isn't already user with same phone number
   * @param string phoneNumber - the phone number user inputted
   * @return bool - false if there is a user with same phone number
   */

  private boolean checkIfPhoneNumberAlreadyInDB(String phoneNumber) {
    Connection con = MiscMethods.establishDatabaseConnection();
    PreparedStatement ps;
    try {
      ps = con.prepareStatement("SELECT user_id FROM " + ServerConstants.DB_USER_TABLE + 
          " WHERE phone_number = ?");
      ps.setString(1, phoneNumber);
      ResultSet rs = ps.executeQuery();
      if(rs.next()) { //account with this number already exists
        return false;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return true;
  }

}
