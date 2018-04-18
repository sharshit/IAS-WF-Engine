import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class HeartBeat implements Runnable {
	
	String appName;
	String domain;
	String messageQueueIp;
	String messageQueuePort;
	String messageQueueUsername;
	String messageQueuePassword;
	String instanceNumber;
	String serviceJarName;

	public HeartBeat(String appName, String domain, String messageQueueIp, String messageQueuePort,
			String messageQueueUsername, String messageQueuePassword, String instanceNumber, String serviceJarName) {
		// TODO Auto-generated constructor stub
		this.appName = appName;
		this.domain = domain;
		this.messageQueueIp = messageQueueIp;
		this.messageQueuePort = messageQueuePort;
		this.messageQueueUsername = messageQueueUsername;
		this.messageQueuePassword = messageQueuePassword;
		this.instanceNumber = instanceNumber;
		this.serviceJarName = serviceJarName;
	}
	
	public void sendMessage(String message, String queueName) throws Exception{
		/*
		* TODO: Get the message server Queue IP & Port
		*/		
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(messageQueueIp);
		factory.setPort(Integer.parseInt(messageQueuePort));
		factory.setUsername(messageQueueUsername);
		factory.setPassword(messageQueuePassword);
		
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.queueDeclare(queueName, false, false, false, null);
		channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
		
		// TODO: To log
		LoggingService.addLogs(appName, "ping_monitoring_" + serviceJarName, " [x] Sent '" + message + "'");
		System.out.println(" [x] Sent '" + message + "'");
		
		channel.close();
		connection.close();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String heartBeatMessage = "";
		String delim = "##";
		
		File file = new File("myVMIp.txt");		 
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			
			String IP = line.split("_")[0];
			String alias = line.split("_")[1];
			String password = line.split("_")[2];
			
			heartBeatMessage = appName + delim + domain + delim + 
									IP + delim + serviceJarName + delim + 
									instanceNumber + delim + alias + delim + password; 
			
			
			String monitorQueue = "platform_monitoringQueue";
			LoggingService.addLogs(appName, "ping_monitoring_" + serviceJarName, "HeartBeatQueue: " + monitorQueue);
			System.out.println("HeartBeatQueue: " + monitorQueue);
			
			while(true) {
				LoggingService.addLogs(appName, "ping_monitoring_" + serviceJarName, "HeartBeatMessage: " + heartBeatMessage);
				System.out.println("CronEngine HeartBeatMessage: " + heartBeatMessage);
				sendMessage(heartBeatMessage, monitorQueue);
				Thread.sleep(1000 * 10);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LoggingService.addLogs(appName, "ping_monitoring_" + serviceJarName, "error: " + e.getMessage());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LoggingService.addLogs(appName, "ping_monitoring_" + serviceJarName, "error: " + e.getMessage());
			
		} catch (InterruptedException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			LoggingService.addLogs(appName, "ping_monitoring_" + serviceJarName, "error: " + e.getMessage());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LoggingService.addLogs(appName, "ping_monitoring_" + serviceJarName, "error: " + e.getMessage());
			
		}
	}
}
