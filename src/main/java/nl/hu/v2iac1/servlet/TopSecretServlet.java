package nl.hu.v2iac1.servlet;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.hu.v2iac1.domein.DefaultUser;
import nl.hu.v2iac1.domein.User;
import nl.hu.v2iac1.auth.*;

public class TopSecretServlet extends HttpServlet {
	private HttpServletResponse resp;
	private HttpServletRequest req;
	HttpServletRequest req2 = (HttpServletRequest) req;
	ServletContext context;
	DefaultUser dU;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		resp = response;
		req = request;
		context = request.getServletContext();
		String username = (String) request.getParameter("username");
		String password = (String) request.getParameter("password");
		String email = (String) request.getParameter("email");
		login(username, password, email);
	}

	@SuppressWarnings("unchecked")
	public void login(String username, String password, String email)
			throws ServletException, IOException {
		RequestDispatcher rd = null;
		boolean returnLogin = true;		
		dU = (DefaultUser)context.getAttribute("defaultuser");	
		returnLogin = returnLogin && (username.equals(dU.getName()));
		returnLogin = returnLogin && (password.equals(dU.getPassword()));
		if (returnLogin) {
			String mailKey = UUID.randomUUID().toString();
			sendEmail(email, mailKey);
			User u = new User(username, password, email, mailKey);
			req.getSession().setAttribute("topsecretuser", u);
			rd = req.getRequestDispatcher("/checkEmailkey.jsp");
			req.setAttribute("msgs", "Kopieer de code die is verstuurd naar "
					+ email + " en plak deze in het tekstveld");
		} else {
			rd = req.getRequestDispatcher("/loginTopsecret.jsp");
			req.setAttribute("msgs", "Verkeerde gegevens ingevoerd!");
		}
		rd.forward(req, resp);
	}

	private void sendEmail(String email, String mailKey) {
		try {
			String sendTo = email;
			String host = "smtp.gmail.com";
			String from = dU.getEmail();
			String pass = dU.getEmailPassword();

			// Setup mail server
			Properties props = System.getProperties();
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.user", from);
			props.put("mail.smtp.password", pass);
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");
			props.put("mail.debug", "true");

			// Get the default Session object.
			Session session = Session.getInstance(props,
					new GMailAuthenticator(from, pass));
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set email from.
			message.setFrom(new InternetAddress(from));

			// Create message.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					sendTo));
			message.setSubject("Two-Step authentication");
			message.setText(mailKey);
			Transport.send(message);
			System.out.println("Sent message successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
