package HelloBoba.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.Test;
import org.objenesis.instantiator.basic.NewInstanceInstantiator;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TestAccountCreationServlet extends TestCase {

	Connection con = TestMiscMethods.establishTestDatabaseConnection();

	@Test
	public void testServlet() throws IOException, ServletException {
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse res = mock(HttpServletResponse.class);

		JSONObject jsonObj = new JSONObject();
		String testEmail = "testemail@gmail.com";
		String testName = "test name";
		String testPassword = "testpassword";
		String testPhoneNumber = "14088968809";

		jsonObj.put("name", testName);
		jsonObj.put("email", testEmail);
		jsonObj.put("password", testPassword);
		jsonObj.put("phone_number", testPhoneNumber);
		String jsonObjString = jsonObj.toString();

		final ByteArrayInputStream is = new ByteArrayInputStream(jsonObjString.getBytes());
		when(req.getInputStream()).thenReturn(new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return is.read();
			}
		});

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		when(res.getOutputStream()).thenReturn(new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				baos.write(b);				
			}
		});

		new AccountCreationTestServlet().doPost(req, res);

		String dbEmail = "";
		String dbPassword = "";
		String dbName = "";
		String dbPhoneNumber = "";

		PreparedStatement ps;
		try {
			ps = con.prepareStatement("SELECT * FROM user WHERE user_email = ?");
			ps.setString(1, testEmail);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) { 
				dbEmail = rs.getString("user_email");
				dbPassword = rs.getString("user_password");
				dbName = rs.getString("name");
				dbPhoneNumber = rs.getString("phone_number");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(testName, dbName);
		assertEquals(testEmail, dbEmail);
		assertEquals(testPassword, dbPassword);
		assertEquals(testPhoneNumber, dbPhoneNumber);

	}







}
