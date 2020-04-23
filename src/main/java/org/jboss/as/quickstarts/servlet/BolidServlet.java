package org.jboss.as.quickstarts.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.as.quickstarts.mdb.PitStopMDB;
import org.json.JSONObject;

@JMSDestinationDefinitions(
	    value = {
	        @JMSDestinationDefinition(
	            name = "java:/topic/BolidTopic",
	            interfaceName = "javax.jms.Topic",
	            destinationName = "BolidTopic"
	        ),
	        @JMSDestinationDefinition(
	                name = "java:/queue/DriverQueue",
	                interfaceName = "javax.jms.Queue",
	                destinationName = "DriverQueue"
	        ),
	        @JMSDestinationDefinition(
	                name = "java:/queue/MechanicQueue",
	                interfaceName = "javax.jms.Queue",
	                destinationName = "MechanicQueue"
	        ),
	        @JMSDestinationDefinition(
	                name = "java:/queue/ReplayQueue",
	                interfaceName = "javax.jms.Queue",
	                destinationName = "ReplayQueue"
	        )
	    })

@WebServlet("/BolidServlet")
@MessageDriven(name = "BolidServlet", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/DriverQueue"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class BolidServlet extends HttpServlet implements MessageListener{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String message;
	
	@Inject
    private PitStopMDB pitStopMDB;

	private PrintWriter out;
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        out = resp.getWriter();
        try {
        	out.write("<h1>BOLID SYSTEM 2020</h1>");
            out.write("<h2>"+message+"</h2>");
            out.write("<form method=\"post\"><input type=\"submit\" value=\"PitStop request\" /></form>");
            int pitStopApproval = -1;
            if(req.getAttribute("pitStopApproval") != null) {
            	pitStopApproval = (int)req.getAttribute("pitStopApproval");
            }
            if(pitStopApproval == 1) {
               out.write("PitStop approved!");
            } else if(pitStopApproval == 0) {
                out.write("PitStop NOT approved!");
            }
         } finally {
             if (out != null) {
                 out.close();
             }
         }

    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
        	JSONObject response = pitStopMDB.sendPitStopRequest();
        	req.setAttribute("pitStopApproval", response.get("requeststatus"));
        	doGet(req, resp);
        }catch (JMSException e) {
        	e.printStackTrace();
        }
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
