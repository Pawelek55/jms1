package org.jboss.as.quickstarts.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.json.JSONException;
import org.json.JSONObject;

@JMSDestinationDefinitions(
        value = {
                @JMSDestinationDefinition(
                        name = "java:/queue/ManagerQueue",
                        interfaceName = "javax.jms.Queue",
                        destinationName = "ManagerQueue"
                )
        })


@MessageDriven(name = "ManagerMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/ManagerQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ManagerMDB implements MessageListener {

    private final static java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(ManagerMDB.class.toString());

    @Inject
    private JMSContext context;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message) {

        try {
            TextMessage tm = (TextMessage) message;
            System.out.println("Message recived by manager" + tm.getText());
            JSONObject jsonMessage = new JSONObject(tm.getText());
            JSONObject jsonResponse = new JSONObject();
            if(jsonMessage.getString("type").equals("pitstoprequest")) {
                jsonResponse.put("requeststatus", (int)Math.round(Math.random()));
            } else {
                jsonResponse.put("requeststatus", 404);
            }
            TextMessage reply = context.createTextMessage();
            reply.setText(jsonResponse.toString());
            Destination replyDestination = message.getJMSReplyTo();
            context.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(replyDestination, reply);

        } catch (JMSException e) {
            e.printStackTrace();
        } 

    }
}
