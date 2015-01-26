package HelloBoba.Server;


import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

/**
 * @desc Servlet that creates a customer.  A customer is object Stripe has 
 * defined.  We can charge customers, which charges the credit card 
 * associated with the customer.  Stripe gives us a customer id token that
 * we can use to process transactions, maintaining a form of disconnect 
 * between us and a user's credit card data.  Stripe is in charge of 
 * protecting and saving cc info
 * @author Byron Tang - byronyugontang@gmail.com
 *
 */

@WebServlet (value="/customercreation", name="Customer-Creation-Servlet")
public class CustomerCreationServlet extends HttpServlet{

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    String jsonReqString = "";
    String stripeTokenString = "";
    int userId = 0;

    try {
      BufferedReader inFromClient = new BufferedReader(new 
          InputStreamReader(request.getInputStream()));
      if(inFromClient != null) {
        jsonReqString = inFromClient.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    JSONObject jsonObj = new JSONObject(jsonReqString);

    stripeTokenString = jsonObj.getString("stripe_token");
    userId = jsonObj.getInt("user_id");

    JSONObject jsonResObj = new JSONObject();
    if(customerCreation(stripeTokenString, userId)) {
      jsonResObj.put(ServerConstants.REQUEST_STATUS,
          ServerConstants.CUSTOMER_CREATE_SUCCESS);
      MiscMethods.giveFreePearlMilkTea(userId, 1);
    }
    else jsonResObj.put(ServerConstants.REQUEST_STATUS, false);

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
   * @desc Use the token Stripe returned to us to create the customer
   * and save the customer id for future access
   * @param int stripeTokenString - this is the token Stripe passed to
   * us after we send credit card info initially to Stripe. We use this 
   * one time token to create a customer
   * @param int userId - user id of user we create customer for
   * @return boolean - true if customer creation was success and we could 
   * save the customer id to database
   */


  private boolean customerCreation(String stripeTokenString, int userId) {
    Stripe.apiKey = "sk_test_CY8QQMarcq8pB4nhhQB8dZ6g"; //change to prod key at launch

    Map<String, Object> customerParams = new HashMap<String, Object>();
    customerParams.put("description", "Customer for user id: " + userId);

    Customer customer;
    String customerId = "";

    String email = getUserEmail(userId);


    try {
      Token stripeToken = Token.retrieve(stripeTokenString);
      customerParams.put("card", stripeToken.getId());
      if(!email.equalsIgnoreCase("error")) {
        customerParams.put("email", email);
      }
      customer = Customer.create(customerParams);		
      customerId = customer.getId();	
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

    if(saveCustomerIdToDatabase(customerId, userId)) {
      return true;
    }
    else return false;

  }

  /**
   * @desc save customer id in the user table 
   * @param int customerId - the customer id we use to access a
   * specific customer in the future
   * @param int userId - the user id that the customer is associated with
   * @return boolean - true if we successfully save the customer id
   * to database
   */
  
  private boolean saveCustomerIdToDatabase(String customerId, int userId) {
    Connection con = MiscMethods.establishDatabaseConnection();
    try {
      PreparedStatement ps = con.prepareStatement("UPDATE " + 
          ServerConstants.DB_USER_TABLE + 
          " SET user_customer_id = ? WHERE user_id = ?");
      ps.setString(1, customerId);
      ps.setInt(2, userId);
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  //	private boolean setHasCreditCardInUserQueue(int userId) {
  //		Connection con = MiscMethods.establishDatabaseConnection();
  //		try {
  //			PreparedStatement ps = con.prepareStatement("UPDATE " + 
  //	ServerConstants.DB_USER_QUEUE_TABLE + 
  //					" SET has_credit_card = ? WHERE user_id = ?");
  //			ps.setInt(1, 1);
  //			ps.setInt(2, userId);
  //			ps.executeUpdate();
  //			return true;
  //		} catch (SQLException e) {
  //			e.printStackTrace();
  //		}
  //		return false;
  //	}

  /**
   * @desc retrieve user email so that we can associate it with the
   * customer object we create through stripe. stripe uses the email to
   * send receipts 
   * @param userId - the user's user id
   * @return string - the user's email
   */
  
  private String getUserEmail(int userId) {
    Connection con = MiscMethods.establishDatabaseConnection();
    String email = "error";
    try {
      PreparedStatement ps = con.prepareStatement("SELECT user_email " +
          "FROM user WHERE user_id = ?");
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      if(rs.next()) {
        email = rs.getString("user_email");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return email;
  }


}
