package org.jboss.as.quickstarts.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.as.quickstarts.bean.Bolid;

@WebServlet("/MechanicServlet")
@MessageDriven(name = "MechanicQueueListener", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/MechanicQueue"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class MechanicServlet extends HttpServlet implements MessageListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

private String message;
	
	private PrintWriter out;
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        out = resp.getWriter();
        out.write("<h1>MECHANICS SYSTEM 2020</h1>");
        out.write("<h2>"+message+"</h2>");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

	@Override
	public void onMessage(Message rcvMessage) {
		TextMessage msg = null;
        try {
            if (rcvMessage instanceof TextMessage) {
            	 msg = (TextMessage) rcvMessage;
                 message = msg.getText();                 
            }
        }catch (JMSException e) {
        	throw new RuntimeException(e);
        }
	}

}
