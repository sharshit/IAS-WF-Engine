import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Sender {
	
	public static void  sendMessage(String message, String queueName, String mqIp) throws Exception{
		/*
		 * TODO: Get the message server Queue IP & Port
		 */
		String msgQueueServerIP = mqIp;
		String msgQueueServerUname = "admin";
		String msgQueueServerPass = "admin";
		int msgQueueServerPort = 5672;
		
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(msgQueueServerIP);
	    factory.setPort(msgQueueServerPort);
	    factory.setUsername(msgQueueServerUname);
	    factory.setPassword(msgQueueServerPass);
	    
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.queueDeclare(queueName, false, false, false, null);
	    channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
	    
	    // TODO: To log
	    System.out.println(" SENT on queue " + message + "'"+msgQueueServerIP);

	    channel.close();
	    connection.close();
		
		
	}
	public static void main(String[] args)
	{
		try {
			if(args.length>=1)
			{
				sendMessage(args[0], args[1], args[2]);
			//	sendMessage(args[0], "healthcare" + "_" + "platform" + "_" + "workflowEngine" + "_" + "instance1");
			}
			else 
			{
			//sendMessage("999", "healthcare" + "_" + "platform" + "_" + "syncService.jar" + "_" + "instance1" , "192.168.1.106");
				
			//	sendMessage("BPHighService.py 10", "healthcare" + "_" + "platform" + "_" + "BPHighService.py" + "_" + "instance1",  "192.168.1.106");
				sendMessage("highBPWorkflowMail.wf", "healthcare" + "_" + "gateway1" + "_" + "workflowEngine" + "_" + "instance1",  "192.168.1.101");
			//	sendMessage("highBPWorkflowMail.wf", "healthcare" + "_" + "is1" + "_" + "workflowEngine" + "_" + "instance1",  "192.168.1.105");
				//sendMessage("highBPWorkflowMail.wf", "healthcare" + "_" + "platform" + "_" + "workflowEngine" + "_" + "instance1",  "192.168.1.105");

			//	sendMessage("recursiveWf.wf", "healthcare" + "_" + "platform" + "_" + "workflowEngine" + "_" + "instance1", "192.168.1.106");
 			//	sendMessage("sensor#Honeywell123#15-event#1", "healthcare" + "_" + "platform" + "_" + "eventEngine" + "_" + "instance1",  "192.168.1.106");
				
			//sendMessage("145", "healthcare" + "_" + "platform" + "_" + "BPHighService.jar" + "_" + "instance1",  "192.168.1.106");
			//	sendMessage("harshit1605@gmail.com#TestSubject#Hello", "healthcare" + "_" + "platform" + "_" + "sendMail.jar" + "_" + "instance1",  "192.168.1.106");
			//	sendMessage("+918376071713#Hello", "healthcare" + "_" + "platform" + "_" + "sendSms.jar" + "_" + "instance1",  "192.168.1.106");
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}