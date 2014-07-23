//package HelloBoba.Server;
//
//
//import java.net.InetSocketAddress;
//import com.sun.net.httpserver.*;
//
//
//
//public class App {
//	
//	public static void main(String argv[]) throws Exception {
//		//create instance of httpserver
//		//InetAddress locIP = InetAddress.getByName("108.216.110.252");
//		//String hostname = "http://localhost";
//		//HttpServer server = HttpServer.create(new InetSocketAddress("54.209.197.94", 80), 0);
//		HttpServer server = HttpServer.create(new InetSocketAddress("54.209.197.94", 80), 0);
//		//when context is account creation, handle the request with accountcreationhandler
//		server.createContext("/test", new TestServlet());
//		server.createContext("/accountaffiliation", new AccountAffiliationHandler());
//		server.createContext("/accountcreation", new AccountCreationHandler());
//		server.createContext("/accountcreationviafbconnect", new AccountCreationViaFBConnectHandler());
//		server.createContext("/checkoutorder", new CheckoutOrderServlet());
//		server.createContext("/customercreation", new CustomerCreationHandler());
//		server.createContext("/defaultaddress", new DefaultAddressServlet());
//		server.createContext("/fbconnect", new FBConnectServlet());
//		server.createContext("/notifyviasms", new NotifyViaSMSServlet());
//		server.createContext("/purchaseorder", new PurchaseOrderServlet());
//		server.createContext("/retrieveaffiliationanddefaultaddress", new RetrieveAffiliationAndDefaultAddressServlet());
//		server.createContext("/retrievecreditcards", new RetrieveCreditCardsServlet());
//		server.createContext("/retrieveorders", new RetrieveOrdersServlet());
//		server.createContext("/setnewdefaultcreditcard", new SetNewDefaultCreditCardServlet());
//		server.createContext("/updatecreditcard", new AddNewCreditCardServlet());
//		server.createContext("/updateorderstatus", new UpdateOrderStatusServlet());
//		server.createContext("/updatequeue", new UpdateQueueServlet());
//		server.createContext("/validatelogin", new ValidateLoginServlet());
//		
//		server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(100)); //100 threads
//		server.start();
//	}
//
//	
//	
//}
