package HelloBoba.Server;

import java.io.BufferedReader;
import java.io.IOException;
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

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.Token;

/**
 * @desc Servlet that parses the JSON returned, containing fields for the id of
 * user and a stripe token.  Stripe token is the token Stripe returns to us when
 * we pass them user's credit card info.  User's cc info is never saved in our
 * database
 * @author Byron Tang byronyugontang@gmail.com
 */

@WebServlet (value="/addnewcreditcard", name="Add-New-Credit-Card-Servlet")
public class AddNewCreditCardServlet extends HttpServlet{

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
    }catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    JSONObject jsonObj = new JSONObject(jsonReqString);

    stripeTokenString = jsonObj.getString("stripe_token");
    userId = jsonObj.getInt("user_id");
    JSONObject jsonResObj = new JSONObject();

    if(createNewCustomerCreditCard(stripeTokenString, userId)) {
      jsonResObj.put(ServerConstants.REQUEST_STATUS, ServerConstants.CARD_ADD_SUCCESS);
      MiscMethods.giveFreePearlMilkTea(userId, 1); 
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

  /**
   * @desc get customer representing user from stripe using stored customer token
   * and add this new card to the customer. this new card is also the new
   * default credit card
   * @param string stripeTokenString - the token stripe returned to us
   * @param string userId - the userid corresponding to user
   * @return bool - true if attaching card to customer is success
   */
  
  private boolean createNewCustomerCreditCard(String stripeTokenString, int userId) {
    //api key to allow us to interact with stripe
    Stripe.apiKey = "sk_test_CY8QQMarcq8pB4nhhQB8dZ6g";

    Map<String, Object> customerParams = new HashMap<String, Object>();
    Map<String, Object> updateParams = new HashMap<String, Object>();

    try {
      Customer cu = Customer.retrieve(MiscMethods.getCustomerId(userId));
      Token stripeToken = Token.retrieve(stripeTokenString);

      customerParams.put("card", stripeToken.getId());
      Card newCard = cu.createCard(customerParams);

      updateParams.put("default_card", newCard.getId());
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
