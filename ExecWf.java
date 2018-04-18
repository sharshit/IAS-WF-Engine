import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

class ExecWf implements Runnable
{
	public Class<?> c;
	public String wfPath;
	public String topologyXmlPath;
	public String type;	// workflowEngine / service / eventengine 
	public String appName;
	public String domain;
	public String mqIp;
	public ExecWf(Class<?> c , String wfPath, String topologyXmlPath, String type, String appName, String domain, String mqIp )
	{
		this.c=c;
		this.wfPath=wfPath;
		this.topologyXmlPath=topologyXmlPath;
		this.type = type;
		this.appName=appName;
		this.domain = domain;
		this.mqIp=mqIp;
	} 
	
	 public String call(String message, String messageQueueIp) throws IOException, InterruptedException 
	 {
		 try
		 {
		  	Connection connection;
		   Channel channel;
		   ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost(messageQueueIp);
		    factory.setPort(5672);
		    factory.setUsername("admin");
		    factory.setPassword("admin");
		    connection = factory.newConnection();
		    channel = connection.createChannel();
		    String replyQueueName = channel.queueDeclare().getQueue();

		    final String corrId = UUID.randomUUID().toString();

		    AMQP.BasicProperties props = new AMQP.BasicProperties
		            .Builder()
		            .correlationId(corrId)
		            .replyTo(replyQueueName)
		            .build();

		    channel.basicPublish("", "platform_synchronous_queue", props, message.getBytes("UTF-8"));

		    final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

		    channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
		      @Override
		      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
		        if (properties.getCorrelationId().equals(corrId)) {
		          response.offer(new String(body, "UTF-8"));
		        }
		      }
		    });

		    return response.take();
		    
		 }
		 catch(Exception e)
		 {
			 System.out.println(e.getMessage());
			 return "no analytics service available ";
		 }
		  }
	
	

	@Override
	public void run() {
		try 
		{
			LoggingService.addLogs(appName, type, "New thread created ");
			if(type.equals("workflowEngine"))
			{
				Object o = c.newInstance();
				@SuppressWarnings("rawtypes")
				
				Class[] partypes = new Class[5];
				partypes[0] = String.class;
				partypes[1] = String.class;
				partypes[2] = String.class;
				partypes[3] = String.class;
				partypes[4] = String.class;
				
				Object[] argToRun = new Object[5];
				argToRun[0] = wfPath;
				argToRun[1] = topologyXmlPath;
				argToRun[2] = appName;
				argToRun[3] = domain;
				argToRun[4] = mqIp;
				
				Method method = o.getClass().getMethod("run", partypes);
				
				LoggingService.addLogs(appName, type, "Invoking WF Engine"+wfPath);
//		Method method = className.getMethod("run", partypes);
				method.invoke(o, argToRun);
			}
			
			else if(type.equals("appServiceJar"))
			{
				Object o = c.newInstance();
				@SuppressWarnings("rawtypes")
				
				Class[] partypes = new Class[1];
				partypes[0] = String.class;
				
				String argToRun = wfPath;
				
	
				Method method = o.getClass().getMethod("run", partypes);
				
				LoggingService.addLogs(appName, type, "appService "+wfPath);
//		Method method = className.getMethod("run", partypes);
				method.invoke(o, argToRun);
			}
			
			else if(type.equals("platformServiceJar"))
			{
				
				//System.out.println(appName+"yyy"+wfPath);
				final Runtime rt = Runtime.getRuntime();
				int count =0;
				for (int i=0;i<wfPath.length();i++)
				{
					if(wfPath.charAt(i)=='#')
					{
						count++;
					}
				}
//				System.out.println(count);
//				System.out.println(appName+"xxx");
				try
				{
					if(count==1)
					{
						// sendSms
						LoggingService.addLogs(appName, type, "send SMS   "+wfPath);
						System.out.println("send message "+wfPath);
						String[] val= wfPath.split("#");
						Process proc = rt.exec("java -jar ./" + appName + "/sendSms.jar "+ val[0]+" "+val[1]  );
						BufferedReader stdInput = new BufferedReader(new 
							     InputStreamReader(proc.getInputStream()));
								
							BufferedReader stdError = new BufferedReader(new 
							     InputStreamReader(proc.getErrorStream()));
							
							// read the output from the command
							System.out.println("Here is the standard output of the command:\n");
							LoggingService.addLogs(appName, type, "Here is the standard output of the command:\n");
							
							String s = null;
							while ((s = stdInput.readLine()) != null) {
							    System.out.println(s);
							    LoggingService.addLogs(appName, type, s);
								
							}

							// read any errors from the attempted command
							System.out.println("Here is the standard error of the command (if any):\n");
							LoggingService.addLogs(appName, type, "Here is the standard error of the command (if any):\n");
								
							while ((s = stdError.readLine()) != null) {
							    System.out.println(s);
							    LoggingService.addLogs(appName, type, s);
								
							} 

						System.out.println("Done ");
						LoggingService.addLogs(appName, type, "Done");
							
					}
					else if (count==2)
					{
						String[] val= wfPath.split("#");
						LoggingService.addLogs(appName, type, "send Mail "+wfPath);
						System.out.println("send mail ");
						
						Process proc = rt.exec("java -jar ./" + appName + "/sendMail.jar "+val[0]+" "+val[1]+" "+val[2]  );
						BufferedReader stdInput = new BufferedReader(new 
							     InputStreamReader(proc.getInputStream()));

							BufferedReader stdError = new BufferedReader(new 
							     InputStreamReader(proc.getErrorStream()));

							// read the output from the command
							System.out.println("Here is the standard output of the command:\n");
							LoggingService.addLogs(appName, type, "Here is the standard output of the command:\n");
							
							
							String s = null;
							while ((s = stdInput.readLine()) != null) {
							    System.out.println(s);
							    LoggingService.addLogs(appName, type, s);
								
							}

							// read any errors from the attempted command
							System.out.println("Here is the standard error of the command (if any):\n");
							LoggingService.addLogs(appName, type, "Here is the standard error of the command (if any)");
							
							while ((s = stdError.readLine()) != null) {
							    System.out.println(s);
							    LoggingService.addLogs(appName, type, s);
								
							}

					}
					else
					{
						
						System.out.println("invalid message in app service");
					    LoggingService.addLogs(appName, type, "invalid message in app service");
						
					}
				}
				catch(Exception e)
				{
					System.out.println("Exception in executing App service \n"+e.getMessage());
					LoggingService.addLogs(appName, type, "Exception in executing App service \n"+e.getMessage());
						
				}
				
				
			}
			else if(type.equals("eventEngine"))
			{
				LoggingService.addLogs(appName, type, "Executing eventEngine ");
				
				Object o = c.newInstance();
				@SuppressWarnings("rawtypes")
				
				Class[] partypes = new Class[7];
				partypes[0] = String.class;
				partypes[1] = String.class;
				partypes[2] = String.class;
				partypes[3] = String.class;
				partypes[4] = String.class;
				partypes[5] = String.class;
				partypes[6] = String.class;
				
				String[] argToRun = new String[7];
				argToRun[0] = wfPath;
				argToRun[1] = "WorkflowEngine";
				argToRun[2] = EventEngineWrapper.messageQueueIp;
				argToRun[3] = appName;
				argToRun[4] = EventEngineWrapper.location;
				argToRun[5] = "service1.jar";
				argToRun[6] = "instance1";
						
	
				Method method = o.getClass().getMethod("processMessage", partypes);
				try
				{
					final Runtime rt = Runtime.getRuntime();
					Process proc = rt.exec("java -jar eventEngine.jar " + argToRun[0] + " " + argToRun[1] + " " + argToRun[2] + " " + argToRun[3] + " " + argToRun[4] + " " + argToRun[5] + " " + argToRun[6]);
					BufferedReader stdInput = new BufferedReader(new 
						     InputStreamReader(proc.getInputStream()));

						BufferedReader stdError = new BufferedReader(new 
						     InputStreamReader(proc.getErrorStream()));

						// read the output from the command
						System.out.println("Here is the standard output of the command:\n");
						LoggingService.addLogs(appName, type, "Here is the standard output of the command:\n");
						
						String s = null;
						while ((s = stdInput.readLine()) != null) {
						    System.out.println(s);
						    LoggingService.addLogs(appName, type, s);
							
						}

						// read any errors from the attempted command
						System.out.println("Here is the standard error of the command (if any):\n");
						LoggingService.addLogs(appName, type, "Here is the standard error of the command (if any):\n");
						
						while ((s = stdError.readLine()) != null) {
						    System.out.println(s);
							LoggingService.addLogs(appName, type, s);
							
						}
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
					LoggingService.addLogs(appName, "workflowEngine", "error in executing event engine :" + e.getMessage());
					
					
				}
				
//		Method method = className.getMethod("run", partypes);
//				method.invoke(o, argToRun);
				
				
			}
			else if(type.equals("appServicePython"))
			{
				
				try
				{
					String loc = "./" + appName + "/" + wfPath;
					LoggingService.addLogs(appName, "workflowEngine", "executing python file " + loc);
					
					Process p = Runtime.getRuntime().exec("python " + loc );
					BufferedReader stdInput = new BufferedReader(new 
						     InputStreamReader(p.getInputStream()));

						BufferedReader stdError = new BufferedReader(new 
						     InputStreamReader(p.getErrorStream()));

						// read the output from the command
						System.out.println("Here is the standard output of the command:\n");
						LoggingService.addLogs(appName, type, "Here is the standard output of the command:\n");
						
						String s = null;
						while ((s = stdInput.readLine()) != null) {
						    System.out.println(s);
							LoggingService.addLogs(appName, type, s);
							
						}

						// read any errors from the attempted command
						System.out.println("Here is the standard error of the command (if any):\n");
						LoggingService.addLogs(appName, type, "Here is the standard error of the command (if any):\n");
						
						while ((s = stdError.readLine()) != null) {
						    System.out.println(s);
							LoggingService.addLogs(appName, type, s);
							
						}

				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
					LoggingService.addLogs(appName, "appServicePython", e.getMessage());
				}
			}
			else if (type.equals("syncServiceJar"))
			{	
				try
				{
					String response = call(wfPath, ServiceWrapper.messageQueueIp);
					System.out.println("response = " +  response);
					LoggingService.addLogs(appName, "syncServiceJar", response);
				}
				catch(Exception e)
				{
					LoggingService.addLogs(appName, "syncServiceJar", "no response");
					System.out.println("no response available ! :" + e.getMessage());
				}
				
			}
			
			
			
			
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
			LoggingService.addLogs(appName, "execWf_error", e.getMessage());
			
		} 
		catch (IllegalAccessException e) 
		{
			e.printStackTrace();
			LoggingService.addLogs(appName, "execWf_error", e.getMessage());
			
		} 
		catch (NoSuchMethodException e) 
		{
			e.printStackTrace();
			LoggingService.addLogs(appName, "execWf_error", e.getMessage());
			
		} 
		catch (SecurityException e) 
		{
			e.printStackTrace();
			LoggingService.addLogs(appName, "execWf_error", e.getMessage());
			
		} 
		catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
			LoggingService.addLogs(appName, "execWf_error", e.getMessage());
			
		} 
		catch (InvocationTargetException e) 
		{
			e.printStackTrace();
			LoggingService.addLogs(appName, "execWf_error", e.getMessage());
			
		}			
		
	}
	


}