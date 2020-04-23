package org.jboss.as.quickstarts.mdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.as.quickstarts.servlet.BolidServlet;

@MessageDriven(name = "LoggerTopicSubscriber", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "topic/BolidTopic"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class LoggerMDB implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(LoggerMDB.class.toString());
	
	private PrintWriter out = null;
	
	private BolidServlet bolidServlet;
		
	public void onMessage(Message rcvMessage) {
        TextMessage msg = null;
        try {
            if (rcvMessage instanceof TextMessage) {
                msg = (TextMessage) rcvMessage;
                if(out == null){
                	//lokalizacja pliku : /home/osboxes/wildfly-10.0.0.Final/bin
	                out = new PrintWriter("measurement.txt");
	                out.println(msg.getText());
	                out.flush();
                }else{
                    out.println(msg.getText());
                    out.flush();
                }
                LOGGER.info("Received Message from topic: " + msg.getText());
            } else {
                LOGGER.warning("Message of wrong type: " + rcvMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
}
