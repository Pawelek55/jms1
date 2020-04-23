package org.jboss.as.quickstarts.mdb;


import javax.jms.Queue;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(name = "MessageRouterTopicSubscriber", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "topic/BolidTopic"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class MessageRouterMDB implements MessageListener{
	
    private final static Logger LOGGER = Logger.getLogger(LoggerMDB.class.toString());
    
    private String message;
    
    @Inject
    private JMSContext context;
	
    @Resource(lookup = "java:/queue/DriverQueue")
    private Queue driverQueue;
    
    @Resource(lookup = "java:/queue/MechanicQueue")
    private Queue mechanicQueue;
    
	private Destination driver;
    private Destination mechanic;
    
    public void onMessage(Message rcvMessage) {
        TextMessage msg = null;
        try {
            if (rcvMessage instanceof TextMessage) {
                msg = (TextMessage) rcvMessage;
                String [] values = msg.getText().split(", ");
                int engineTemperature = Integer.parseInt(values[0]);
                int tirePressure= Integer.parseInt(values[1]);
                int oilPressure= Integer.parseInt(values[2]);
                
                if(engineTemperature > 80 ||tirePressure < 1 || oilPressure > 35 ){
                	message= "";
                	if(engineTemperature > 80){
                		message+="Danger! High level of engine temperature. ";
                	}else if (tirePressure<1){
                		message+="Danger! High level of tire pressure. ";
                	}else{
                		message+="Danger! High level of oil pressure. ";
                	}

                	driver = driverQueue;
                    context.createProducer().send(driver, message);
                    context.createProducer().send(mechanic, message);
                    LOGGER.info("DANGER");
                }else if(engineTemperature > 60 || tirePressure < 2 || oilPressure > 25){
                	message="";
                	if(engineTemperature > 60){
                		message+="Warning! High level of engine temperature. ";
                	}else if (tirePressure<2){
                		message+="Warning! High level of tire pressure. ";
                	}else{
                		message+="Warning! High level of oil pressure. ";
                	}
                	driver = driverQueue;
                	mechanic = mechanicQueue;
                    context.createProducer().send(driver, message);
                    LOGGER.info("WARNING");
                }else{
                	message="";
                	driver = driverQueue;
                	mechanic = mechanicQueue;
                    context.createProducer().send(driver, message);
                    context.createProducer().send(mechanic, message);
                    LOGGER.info("STABLE");
                }
                LOGGER.info("Received Message from topic: " + msg.getText());
            } else {
                LOGGER.warning("Message of wrong type: " + rcvMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
