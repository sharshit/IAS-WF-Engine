
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import xmlParser.Gateway;
import xmlParser.Sensor;
import xmlParser.TopologyData;
import xmlParser.TopologyParser;

public class WfEngine {
	boolean execute_else=false;
	Boolean execute_then=false;
	
	boolean expecting_start=false;
	boolean expecting_if=false;
	boolean expecting_else=false;
	boolean expecting_then=false;
	boolean expecting_end=false;
	boolean expecting_sensor=false;
	String globalAppName = "";
	
	void resetAll()
	{
		expecting_start=false;
		expecting_if=false;
		expecting_else=false;
		expecting_end=false;
		expecting_sensor=false;
	}
	
	
	
	
	public  void run (String  workflowFileLoc, String topologyXmlPath , String appName, String domain, String mqIp) {
		System.out.println("Inside workflow"+workflowFileLoc);
		LoggingService.addLogs(appName, "executing_workflow", "executing workflow");
		// String filname to be taken as argument 
		globalAppName = appName;
		//String workflow_file_loc="/home/harshit/Documents/IAS/XMLParser/resources/wtf.wf";
		try(BufferedReader br=new BufferedReader(new FileReader(workflowFileLoc)))
		{
			String line= br.readLine();
			LoggingService.addLogs(appName, "executing_workflow", "executing workflow :" + line);
			
			//System.out.println(line);
			
			resetAll();
			expecting_start=true;
			while(line!=null)
			{
				
				StringTokenizer st = new StringTokenizer(line," ");
				
				while(st.hasMoreTokens())
				{
					String token=st.nextToken();
					System.out.println("token is "+token);
					if (token.equals("START") && expecting_start==true )
					{
						resetAll();
						expecting_if=true;
						expecting_sensor=true;
						// ignore
					}
					else if (   ( token.equals("IF")  && expecting_if ) ||  (expecting_else && token.equals("ELSE") && execute_else) )
					{
						//read untill THEN encounter
						resetAll();
						System.out.println("I am here\n");
						if(execute_else)
						{
							execute_else=false;
							expecting_end=true;
							
						}
						else
						{
							expecting_then=true;
						}
						
						try
						{
							
							
							while(!(token=st.nextToken()).equals("THEN") && !(token.equals("END")) )
 							{
								String sensor_data="";
								System.out.println(token);
								LoggingService.addLogs(appName, "executing_workflow", token);
								if(token.startsWith("SENSOR"))
								{
									String[] sensor=token.split("#:#");
									String sensor_id=sensor[1];
									// fetch sensor data
									//   "/home/harshit/Documents/IAS/XMLParser/resources/sensor_data_mapping.txt"
									sensor_data=fetch_sensor_data(appName ,sensor_id, topologyXmlPath);
									LoggingService.addLogs(appName, "executing_workflow", "fetching sensor data");
									
									if(sensor_data.equals("no response"))
									{
										// sensor not find 
										System.out.println("NO Sensor found....WORKFLOW EXECUTION STOPED");
										LoggingService.addLogs(appName, "executing_workflow", "NO Sensor found....WORKFLOW EXECUTION STOPED");
										
										return ;
										//throw new java.lang.RuntimeException("Sensort data not found");
									}
									System.out.println("Sensor data is "+sensor_data);
									LoggingService.addLogs(appName, "executing_workflow", "Sensor data is "+sensor_data);
									
									
								}
								
								// fetch service 
								token=st.nextToken();
								//System.out.println("service is "+token);
								if(token.startsWith("SERVICE"))
								{
									String[] service=token.split("#:#");
									String service_id=service[1];
									// execute jar of service_id
									// assuming service_id is name of jar 
									// also jar conatins only one class service_id
									String jarPath = System.getProperty("user.dir")+"/"+appName+"/"+service_id + ".jar";
									if( processJar(appName,jarPath,sensor_data).equals("TRUE") )
									{	
										execute_then=true;
									
									}
									else
									{
										execute_then=false;
									}
									//System.out.println("Execute then");
									//System.out.println(execute_then);
									
									
								}
								else if(token.startsWith("WORKFLOW"))
								{  
									//
									
								}
							}
							
							System.out.println("breaking loop"+token);
							if(token.equals("THEN") && expecting_then )
							{
								token=st.nextToken();
								//System.out.println("token inside then"+token);
								
								
								resetAll();
								expecting_else=true;
								expecting_end=true;		// for third case 
								
								
								if(execute_then==null)
								{
									System.out.println("jar returned nothing ..");
									
								}
								else if(execute_then)
								{
									// execute workflow engine

									
									if(token.startsWith("WORKFLOW"))
									{
										//put msg into msg queue
										/*
										 * new Sender().sendMessage(msgToSnd, QueueName);
										 */
										//System.out.println(" is the token"+token );
										System.out.println("Going to call other workflow.");
										LoggingService.addLogs(appName, "executing_workflow", "Calling other workflow");
										String[] ar=token.split("#:#");
										
//										String apiPath="/home/harshit/Documents/IAS/WfEngineAPI.jar";
//										String wfLocationPath2 = "/home/harshit/Documents/IAS/XMLParser/resources/"+ar[1];
//										String sensorDataPath2 = "/home/harshit/Documents/IAS/XMLParser/resources/sensor_data_mapping.txt";
//										//String[] sendString= {apiPath, wfLocationPath2, sensorDataPath2};
										// You need to send thhis string but since i have set environment variable in my sender.java 
										// I will just pass name  of workflow
										
										
										
										String message = "";
										String messageQueueName = "";
										String messageQueueIp=mqIp;
										if(domain.contains("platform"))
										{
											message = appName+"$$workflow$$" + ar[1].toString();
											messageQueueName = "platform_loadBalancer";
											LoggingService.addLogs(appName, "executing_workflow", "Calling other workflow on MQ : "+messageQueueName + messageQueueIp);
											String[] sendString= {message, messageQueueName, messageQueueIp};
											Sender.main(sendString);
										}
										else
										{
											message = ar[1].toString();
											messageQueueName = appName + "_" + domain + "_" + "workflowEngine" + "_" + "instance1";
											LoggingService.addLogs(appName, "executing_workflow", "Calling other workflow on MQ : "+messageQueueName + messageQueueIp);
											String[] sendString= {message, messageQueueName, messageQueueIp};
											Sender.main(sendString);
											
										}
										
									
										//new WfEngine().run(wfLocationPath2, sensorDataPath2);
										
									}//System.out.println("inside workflow and value is "+token); 
 									else if (token.contains("SENDMAIL") || token.contains("SENDSMS"))
									{
 										Class<?> c=null;
 										System.out.println("Token is "+token+"Sms or email");
 										new Thread(new ExecWf(c, (token.split("#:#"))[2] , topologyXmlPath, "platformServiceJar" , appName, domain , mqIp)).start();
									}
									else
									{
										//syntax error
										System.out.println("Grammar of workFlow is wrong  ");
										LoggingService.addLogs(appName, "executing_workflow", "Grammar of workFlow is wrong");
										
										return ;
									}
								}
								else 
								{
									
									execute_else=true;
								}
								
							}
							else if (token.equals("END") && expecting_end)
							{
								LoggingService.addLogs(appName, "executing_workflow", "*****END*****");
								System.out.println("********END***********");
								// Ends here
							}
							else
							{
								System.out.println(" 'THEN' or 'END' not found after if condition or 'THEN' / 'END' is mis-spelled..Wrong GRAMMAR");
								return ;
							}
						}
						catch (Exception e) 
						{
							System.out.println("Exception after Token is IF or ELSE");
							LoggingService.addLogs(appName, "executing_workflow", "error " + e.getMessage());
							
							return ; 
							//e.printStackTrace();
						}
						
					}
					else if (token.startsWith("SENSOR") && expecting_sensor)
					{
						resetAll();
						String[] sensor=token.split("#:#");
						String sensor_id=sensor[1];
						// fetch sensor data
						//   "/home/harshit/Documents/IAS/XMLParser/resources/sensor_data_mapping.txt"
						String sensor_data=fetch_sensor_data( appName ,sensor_id, topologyXmlPath);
						System.out.println("sensor data " + sensor_data);
						LoggingService.addLogs(appName, "executing_workflow","sensor data " + sensor_data);
						
						if(sensor_data.equals("no response"))
						{
							// sensor not find 
							System.out.println("NO Sensor found....WORKFLOW EXECUTION STOPED");
							LoggingService.addLogs(appName, "executing_workflow","NO Sensor found....WORKFLOW EXECUTION STOPED");
							
							return; 
						}
						while(st.hasMoreTokens() && (token=st.nextToken()).startsWith("SERVICE")  )
						{
							String[] service=token.split("#:#");
							String service_id=service[1];
							// execute jar of service_id
							// assuming service_id is name of jar 
							// also jar conatins only one class service_id
							//String jarPath = "/home/harshit/Documents/IAS/"+service_id+".jar";
							
							String jarPath=System.getProperty("user.dir")+"/"+appName+"/"+service_id+".jar";
							//System.out.println("Looking for service on path"+jarPath);
							LoggingService.addLogs(appName, "executing_workflow","Jar path "+jarPath);
							sensor_data=processJar(appName,jarPath,sensor_data);
							LoggingService.addLogs(appName, "executing_workflow","Sensor Data after processing jar "+sensor_data);
							//System.out.println("sensor data is "+sensor_data );
						}
						if(token.equals("END"))
						{
							System.out.println("******Executed Successfully******");
							LoggingService.addLogs(appName, "executing_workflow","******Executed Successfully******");
							
							
						}
						
					}
					
				}
				line = br.readLine();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LoggingService.addLogs(appName, "executing_workflow","error in executing workflow " + e.getMessage());
			
		}
		
		
	}

	private  String processJar(String appName, String jarPath, String sensor_data) {
		// TODO Auto-generated method stub
		//String methodName = "run";
		String returnVal = null;
		LoggingService.addLogs(appName, "executing_workflow","processing service" + jarPath);

		//System.out.println("jarpath is "+jarPath);
		try {
			@SuppressWarnings("resource")
			JarFile jarFile = new JarFile(jarPath);
	        Enumeration<JarEntry> e = jarFile.entries();
	        URL[] urls = { new URL("jar:file:" + jarPath+"!/") };
	        URLClassLoader cl = URLClassLoader.newInstance(urls);
	        Class<?> c = null;
	        
	        while (e.hasMoreElements()) {
	            JarEntry je = e.nextElement();
	            if(je.isDirectory() || !je.getName().endsWith(".class")) {
	                continue;
	            }
	            String className = je.getName().substring(0,je.getName().length()-6);
	            className = className.replace('/', '.');
	        //    System.out.println(className + "class name is ");
	            c = cl.loadClass(className);	       
	        }
	        LoggingService.addLogs(appName, "executing_workflow","loaded successfully" );
	        Object o = c.newInstance();
	        @SuppressWarnings("rawtypes")
			Class[] partypes = new Class[1];
			partypes[0] = String.class;
			
			
			Object[] argToRun = new Object[1];
			argToRun[0] = sensor_data;
			
			
			LoggingService.addLogs(appName, "executing_workflow","Calling Run method" );
			Method method = o.getClass().getMethod("run", partypes);
			
			
		//	Method method = className.getMethod("run", partypes);
			returnVal = (String)method.invoke(o, argToRun);	
			LoggingService.addLogs(appName, "executing_workflow","Output of Service"+ returnVal);
	        
		}
		catch(Exception e) 
		{
			System.out.println("Error in data from service ");
			System.out.println(e.getMessage());
			LoggingService.addLogs(appName, "executing_workflow","Error in  Service"+ e.getMessage());
	        
		}
		
		return returnVal;
	}

	private  String  fetch_sensor_data(String appName , String sensor_id,String topologyXmlPath) // sensorId , xmlfilePath
	{
//		try(BufferedReader br=new BufferedReader(new FileReader(sensor_data_mapping_path)))
//		{
//			String line=br.readLine();
//			while(line!=null)
//			{
//				String[] str=line.split(" ");
//				if(str[0].equals(sensor_id))
//				{
//					return Integer.parseInt(str[1]);
//				}
//				line=br.readLine();
//			}
//		}
//		catch(Exception e)
//		{
//			System.out.println("in fectch sensor data function");
//			e.printStackTrace();
//		}
//		return -1;
//		
		
		try {
				TopologyParser topoParser=new TopologyParser();
				TopologyData topoData= topoParser.getTopologyData(topologyXmlPath);
				List<Gateway>   gatewayList= topoData.topo.getGateways();
				String sensorDataVal="no response";
				for(int i=0;i<gatewayList.size();i++)
				{
					Gateway gatewayObj=gatewayList.get(i);
					List<Sensor> listOfSensor=gatewayObj.getSensors();
					for(int j=0;j<listOfSensor.size();j++)
					{
						if(listOfSensor.get(j).getSensorId().equals(sensor_id))
						{
							String gatewayAddr=gatewayObj.getIP()+":"+gatewayObj.getPort();
							//LoggingService.addLogs(appName, "executing workflow","getting sensor data from " + gatewayAddr);

				//			System.out.println("Gateway is "+gatewayAddr+ " sensorId is "+ sensor_id);
							
							LoggingService.addLogs(appName,"executing_workflow", "Rest call on "+ gatewayAddr);
							
							sensorDataVal=getSensorDataFromGateway(gatewayAddr, sensor_id);
							LoggingService.addLogs(appName, "executing_workflow","got sensor data from " + sensorDataVal);

							if(sensorDataVal.equals("no response"))
							{
								System.out.println(" No data found for "+sensor_id);
								LoggingService.addLogs(appName, "executing_workflow"," No data found for "+sensor_id);

								
							}
							System.out.println("val of Sensor data for sensor"+sensor_id +"is "+sensorDataVal);
							LoggingService.addLogs(appName, "executing_workflow","val of Sensor data for sensor"+sensor_id +"is "+sensorDataVal);

							return sensorDataVal;
						}
					}
				}
				if(sensorDataVal.equals("no response"))
				{
					LoggingService.addLogs(appName,"executing_workflow", "Sensor data not found");
					System.out.println("Sensor value not found in fetch sensor data ");
				}
				
		}
		catch (Exception e)
		{
			System.out.println("Error in fetching data (Fetch sensor data )");
			LoggingService.addLogs(appName, "executing_workflow","Error in fetching data (Fetch sensor data )" + e.getMessage());

			//e.printStackTrace();
		}
		return "-1";
		
	}
	
	private String getSensorDataFromGateway(String gatewayIp , String sensorId)
	{
		String output="no response";
		  try 
		  {

			// 			http://10.2.132.176:8080/sensorDataApi/getSensorData/SensorID
			  
			  String sendingUrl="http://"+gatewayIp+"/sensorDataApi/getSensorData/"+sensorId; 
			  System.out.println(sendingUrl+"  URL");
				LoggingService.addLogs(globalAppName, "executing_workflow","URL is"+sendingUrl);

			  URL url = new URL(sendingUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "text/plain");

			if (conn.getResponseCode() != 200) 
			{
				LoggingService.addLogs(globalAppName, "executing_workflow"," error in rest call :" + "Failed : HTTP error code : "
						+ conn.getResponseCode());

				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			
			System.out.println("Output from Server .... \n");
			//while 
			output = br.readLine() ; 
			
			System.out.println(output);
			LoggingService.addLogs(globalAppName, "executing_workflow", "output from server"+output);

			
			conn.disconnect();
			
		  }
		  catch (MalformedURLException e) 
		  {

			//e.printStackTrace();
			  LoggingService.addLogs(globalAppName, "executing_workflow", "error in executing workflow" + e.getMessage());

				
			  System.out.println("Error in rest call...getSensorDataFromGateway");

		  }
		  catch (IOException e) 
		  {
			  LoggingService.addLogs(globalAppName, "executing_workflow", "error in executing workflow" + e.getMessage());

			  System.out.println("Error in rest call ..IO exception");
			//e.printStackTrace();

		  }
		  return output;
	}
	

}

