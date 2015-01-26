package HelloBoba.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

/**
 * @desc Servlet that gets passed the user making the order as well as the
 * contents of the order.  checks to see if any free coupons can be applied 
 * to the order and returns the old and new price of the order along with 
 * how many free coupons were used
 * @author Byron Tang byronyugontang@gmail.com
 */

@WebServlet (value="/checkoutorder", name="Checkout-Order-Servlet")
public class CheckoutOrderServlet extends HttpServlet{

  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException {
    String jsonReqString = "";
    int userId = 0;
    int totalPrice = 0;

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
    JSONObject jsonOrderObj = jsonObj.getJSONObject("order");
    userId = jsonObj.getInt("user_id");

    totalPrice = calculatePriceOfOrder(jsonOrderObj);
    int numFreePMT = MiscMethods.numberOfFreePearlMilkTeaUserHas(userId);
    int numPMTInOrder = MiscMethods.numberOfPearlMilkTeaInOrder(jsonOrderObj);
    int newTotalPrice = newTotalPrice(totalPrice, numFreePMT, numPMTInOrder);

    JSONObject jsonResObj = new JSONObject();

    if(numFreePMT != 0) {
      if(numFreePMT >= numPMTInOrder) {
        jsonResObj.put(ServerConstants.NUM_FREE_PMT, numPMTInOrder);
        jsonResObj.put("original_total_price", totalPrice);
        jsonResObj.put("new_total_price", newTotalPrice);
      }
      else {
        jsonResObj.put(ServerConstants.NUM_FREE_PMT, numFreePMT);
        jsonResObj.put("original_total_price", totalPrice);
        jsonResObj.put("new_total_price", newTotalPrice);
      }
    }
    else {
      jsonResObj.put(ServerConstants.NUM_FREE_PMT, numFreePMT);
    }

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
   * @desc calculates what the new total price is by looking up how many coupons
   * user has and applying them
   * @param int totalPrice - the total price of their order
   * @param int numFreePMT - how many free coupons user has
   * @param int numPMTInOrder - number of drinks in the order, what the coupons 
   * can be applied to
   * @return int - the new discounted price of order
   */

  private int newTotalPrice(int totalPrice, int numFreePMT, int numPMTInOrder) {
    int newTotalPrice = totalPrice;
    int priceOfPMT = MiscMethods.priceOfPearlMilkTea();
    if(numFreePMT >= numPMTInOrder) {
      newTotalPrice = totalPrice - numPMTInOrder*priceOfPMT;
    }
    else newTotalPrice = totalPrice - numFreePMT*priceOfPMT;
    return newTotalPrice;
  }

  /**
   * @desc input is a jsonobject containing the details of the order, what menu 
   * item along with quantity
   * @param JSONObject jsonOrder - contains the order details
   * @return int - the total cost of the order
   */

  private int calculatePriceOfOrder(JSONObject jsonOrder) {
    Iterator<?> keys = jsonOrder.keys();
    int menuId = 0;
    int totalPrice = 0;
    while( keys.hasNext() ){
      String key = (String)keys.next();
      menuId = Integer.parseInt(key);
      totalPrice += MiscMethods.calculatePriceOfMenuId(menuId)*jsonOrder.getInt(key);
    }
    return totalPrice;
  }


}











