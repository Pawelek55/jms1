package org.jboss.as.quickstarts.bean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

@Singleton
@Startup
public class Bolid {
	
	private String message;
	
    @Inject
    private JMSContext context;
	
    @Resource(lookup = "java:/topic/BolidTopic")
    private Topic topic;
	
	@Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
	@PostConstruct
	private void generateRandomValues(){
		String engineTemperature = String.valueOf(new Random().nextInt(100));
		String tirePressure = String.valueOf(new Random().nextInt(5));
		String oilPressure = String.valueOf(new Random().nextInt(40));
		String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

		message= engineTemperature+", "+tirePressure+", "+oilPressure+", "+time;
		
        final Destination destination = topic;
        context.createProducer().send(destination, message);
        		
	}
	
	public String getMeasurementList (){
		return message;
	}

	
	
	
}
