//	healthcare platform appServiceJar instance1 localhost admin admin 5672 checkbp.jar CheckBP
//	healthcare platform platformServiceJar instance1 localhost admin admin 5672 sendSms.jar SendSms




import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


public class ServiceWrapper {
	static public Class<?> c = null;
	static public String wfEngineApiPath;
	static public String topologyXmlPath;
	static public String wfLocationPath;
	static public String messageQueueIp;
	public static String mqIp;
	//  "/home/harshit/Documents/IAS/WfEngineAPI.jar"
	public static void main(String[] args) {		// appName , domain , type=workflowEngine , instance , mqIp ,mqUserName , mqPassword , mqPort , workflowEngineName
		
		// jarpath server knows where jar is 
		if(args.length<10)
		{
			System.out.println("Less Number of parameter");
			return ;
		}
		
		 String appName = args[0];		
		 String domain = args[1] ;       		//     gatweay / is / platform
		 String workflowEngine = args[2];		// wheather it is event engine / workflowengine/ ruleengine
		 String instance = args[3] ; 			//instance1		
		 mqIp= args[4] ;					//    mq username, mq password, mq port
		 String mqUserName= args[5];
		 String mqPassword =args[6];
		 String mqPort = args[7];
		 String workflowEngineName = args[8];
		 String runClassName = args[9];
		 messageQueueIp = mqIp;
	
		wfEngineApiPath =	 System.getProperty("user.dir")+"/"+ appName + "/" + workflowEngineName;
		wfLocationPath =  System.getProperty("user.dir")+"/" + appName + "/";	//	/home/harshit/Documents/IAS/XMLParser/resources/
		topologyXmlPath = 	 System.getProperty("user.dir")+"/"+appName+"/"+"topology.xml"; 							//	/home/harshit/Documents/IAS/XMLParser/resources/sensor_data_mapping.txt
		
		System.out.println(wfEngineApiPath +"  "+ wfLocationPath +" "+ topologyXmlPath);
		LoggingService.addLogs(appName, workflowEngine, wfEngineApiPath +"  "+ wfLocationPath +" "+ topologyXmlPath);
	    

		// start pinging monitoring queue
		LoggingService.addLogs(appName, "ping_monitoring_" + workflowEngineName, " starting pinging monitoring ");
		
		HeartBeat heartBeat = new HeartBeat(appName, domain, 
				mqIp, mqPort, 
							mqUserName, mqPassword, 
							instance, workflowEngineName);
		Thread heartBeatThread = new Thread(heartBeat);
		heartBeatThread.start();
		
		
		
		// start pinging loadBalancing
		if(domain.contains("platform"))
		{
			LoggingService.addLogs(appName, workflowEngineName + "_" +  "Ping_LB", "Started Sending cpu usage Messages to LoadBalancer");
			PingLB pingUsage = new PingLB(appName, workflowEngineName, mqIp, mqPort, 
								 				mqUserName, mqPassword, 
												instance);
			Thread pingLBThread = new Thread(pingUsage);
			pingLBThread.start();
		}
		
		

		init(wfEngineApiPath, runClassName, appName, workflowEngine);
		run (appName , domain,workflowEngine ,instance , mqIp , mqUserName ,mqPassword , mqPort, workflowEngineName) ; 	//run(appName , domain , workflowEngine , instance, mqUsername, mqPassword , mqport , mqIp);	 
		end();
	}
	
	static void init (String jarPath, String runClassName, String appName  , String workflowEngine) {
		
		
		//Object returnVal = null;
		try 
		{
			@SuppressWarnings("resource")
			JarFile jarFile = new JarFile(jarPath);
	        Enumeration<JarEntry> e = jarFile.entries();
	        URL[] urls = { new URL("jar:file:" + jarPath+"!/") };
	        URLClassLoader cl = URLClassLoader.newInstance(urls);
	        
	        if(!(jarPath.contains("sendMail.jar") || jarPath.contains("sendSms.jar") || jarPath.contains("syncQueueMainJar.jar") ))
	        {
	        	while (e.hasMoreElements()) 
	   	        {
	   	            JarEntry je = e.nextElement();
	   	            if(je.isDirectory() || !je.getName().endsWith(".class")) 
	   	            {
	   	                continue;
	   	            }
	   	            String className = je.getName().substring(0,je.getName().length()-6);
	   	            className = className.replace('/', '.');
	   	            //System.out.println(className);
	   	            if(className.equals(runClassName))
	   	            	c = cl.loadClass(className);
	   	            else
	   	            {
	   	            	System.out.println(className + "  xxxxxxxxxx");
	   	            	cl.loadClass(className);
	   	            }	
	   	        }
	        	
	        	LoggingService.addLogs(appName, workflowEngine, "Srvice  loaded successfully in Memory");
	        }
	        
	        
	     	        
		}
		catch(Exception e) 
		{
			LoggingService.addLogs(appName, workflowEngine, "error in loading service " + e.getMessage());
		      
			
			e.printStackTrace();
		}
		
	}

	static void run(String appName , String domain,String workflowEngine , String instance , String mqIp , String mqUname , String mqPasswd , String mqPort, String jarName) {//run(String appName ,String  domain , String workflowEngine , String instance, String mqUname , String mqPasswd, String mqPort , String mqIp) {
		// While(1)
		//Listen over queue/Socket
		//For each new message, create separate thread and process request
		// Each thread create new instance of workflow/Service class and execute it.
		
		//String queueName="prat";
		String queueName = "";
		if(workflowEngine.equals("workflowEngine"))
			queueName = appName + "_" + domain + "_" + workflowEngine + "_" + instance;
		else if(workflowEngine.equals("appServiceJar") || workflowEngine.equals("platformServiceJar")||workflowEngine.equals("syncServiceJar" ))
			queueName = appName + "_" + domain + "_" + jarName + "_" + instance;
			
		
		LoggingService.addLogs(appName, workflowEngine, "listening on queue" + queueName);
		 
		 System.out.println(queueName);

		//LoggingService.addLogs(appName, workflowEngine, "listening on queue" + queueName);
			
		 //  set connection of M.Q
		try 
		{
			
			System.out.println("In wrapper listening to queue..");
			LoggingService.addLogs(appName, workflowEngine, "In wrapper listening to queue..");
			
			String msgQueueServerIP =mqIp ; 
			String msgQueueServerUname = mqUname ; 
			String msgQueueServerPass =mqPasswd ;
			int msgQueueServerPort =Integer.parseInt(mqPort);
			
		    ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost(msgQueueServerIP);
		    factory.setPort(msgQueueServerPort);
		    factory.setUsername(msgQueueServerUname);
		    factory.setPassword(msgQueueServerPass);
		    
		    Connection connection = factory.newConnection();
		    Channel channel = connection.createChannel();
		    channel.queueDeclare(queueName, false, false, false, null);
		    
		    LoggingService.addLogs(appName, workflowEngine, "Listening on "+queueName);
		    
		    Consumer consumer = new DefaultConsumer(channel) {
			      @Override
			      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
			          throws IOException {
			        
			    	  System.out.println("received message");
			    	  String wfName = new String(body, "UTF-8");
			    	  
			    	  System.out.println("received message  "+wfName);
			    	  LoggingService.addLogs(appName, workflowEngine, "received message  "+wfName);
			  		
			    	  // Creating a new thread to handle the incoming message
			    	  
			    	  // change work flow path and sensor data path
			    	  LoggingService.addLogs(appName, workflowEngine, "Creating new thread ");
			    	  new Thread(new ExecWf(c, wfName, topologyXmlPath, workflowEngine ,appName, domain , mqIp)).start();
			    	  
			    	  //System.out.println(" [x] Received '" + wfName+ "'");
			      }
			    };
			    channel.basicConsume(queueName, true, consumer);
		}
		catch (Exception e)
		{
			System.out.print("Connectino issue in wfCaller.run() ");
			LoggingService.addLogs(appName, workflowEngine, queueName + "CONNECTION ISSUE IN");
			e.printStackTrace();
		}
		
	}
	
	static void end() 
	{
		
	}
	
}
	    

