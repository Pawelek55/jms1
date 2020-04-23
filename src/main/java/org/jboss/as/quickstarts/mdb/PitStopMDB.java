package org.jboss.as.quickstarts.mdb;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSConnectionFactoryDefinition;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.json.JSONException;
import org.json.JSONObject;

@JMSConnectionFactoryDefinition(
        name="java:app/jms/MyConnectionFactory"
)

@JMSDestinationDefinitions(
        value = {
                @JMSDestinationDefinition(
                        name = "java:/queue/RequestQueue",
                        interfaceName = "javax.jms.Queue",
                        destinationName = "RequestQueue"
                )
        })

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class PitStopMDB{

	/**
	 * 
	 */
	private final static java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(PitStopMDB.class.toString());

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:/queue/ManagerQueue")
    private Queue managerQueue;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public JSONObject sendPitStopRequest() throws JMSException, JSONException {
        TemporaryQueue replyToQueue = context.createTemporaryQueue();
        TextMessage request = context.createTextMessage();
        request.setJMSReplyTo(replyToQueue);
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("type", "pitstoprequest");
        request.setText(jsonMessage.toString());
        context.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(managerQueue, request);
        Message reply = context.createConsumer(replyToQueue).receive();
        TextMessage replyTm = (TextMessage) reply;
        return new JSONObject(replyTm.getText());
    }


}
