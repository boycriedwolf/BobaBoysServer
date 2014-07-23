package HelloBoba.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@WebServlet (value="/checkoutorder", name="Checkout-Order-Servlet")
public class CheckoutOrderServlet extends HttpServlet{

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String jsonReqString = "";

		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if(inFromClient != null) {
				jsonReqString = inFromClient.readLine();
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jsonObj = null;
		JSONObject jsonOrderObj = null;

		//		try {
		jsonObj = new JSONObject(jsonReqString);
		//		} catch (JSONException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}

		int userId = 0;
		int totalPrice = 0;
		//		try {
		jsonOrderObj = jsonObj.getJSONObject("order");
		userId = jsonObj.getInt("user_id");
		//		} catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		totalPrice = calculatePriceOfOrder(jsonOrderObj);
		int numFreePMT = MiscMethods.numberOfFreePearlMilkTeaUserHas(userId);
		int numPMTInOrder = MiscMethods.numberOfPearlMilkTeaInOrder(jsonOrderObj);
		int newTotalPrice = newTotalPrice(totalPrice, numFreePMT, numPMTInOrder);

		JSONObject jsonResObj = new JSONObject();
		//		try {
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
		//		}
		//		catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

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

	private int newTotalPrice(int totalPrice, int numFreePMT, int numPMTInOrder) {
		int newTotalPrice = totalPrice;
		int priceOfPMT = MiscMethods.priceOfPearlMilkTea();
		if(numFreePMT >= numPMTInOrder) {
			newTotalPrice = totalPrice - numPMTInOrder*priceOfPMT;
		}
		else newTotalPrice = totalPrice - numFreePMT*priceOfPMT;
		return newTotalPrice;
	}

	private int calculatePriceOfOrder(JSONObject jsonOrder) {
		Iterator<?> keys = jsonOrder.keys();
		int menuId = 0;
		int totalPrice = 0;
		//		try {
		while( keys.hasNext() ){
			String key = (String)keys.next();
			menuId = Integer.parseInt(key);
			totalPrice += MiscMethods.calculatePriceOfMenuId(menuId)*jsonOrder.getInt(key);
		}
		//		} catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		return totalPrice;
	}


}











