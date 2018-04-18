import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class PingMonitorQueue implements Runnable
{
	String appName ;
	String serviceName;
	String instanceNumber;
	public PingMonitorQueue(String appName , String serviceName , String instanceNumber)
	{
		this.appName=appName;
		this.serviceName=serviceName;
		this.instanceNumber=instanceNumber;
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		
		// Ping load balancer 

		  OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		  for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) 
		  {
		    method.setAccessible(true);
		    if (method.getName().startsWith("getSystemCpuLoad")&& Modifier.isPublic(method.getModifiers())) 
		    {
		            Object value;
		        
		          while(true) 
		          {
			          try 
			          {
			            value = method.invoke(operatingSystemMXBean);
			            // send message to Monitoring Queue
			            System.out.println("Ping Monitoring Queue");
			            Thread.sleep(1000); // sleep for 1000 ms 
			          } 
			          catch (Exception e) 
			          {
			            value = e;
			          } // try
			        
		          }
		          //System.out.println(method.getName() + " = " + value);
		    } // if
		  } // for
		
	}
	
}



