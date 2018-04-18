
package xmlParser;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;



public class DB_Inserts {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
			Properties prop = new Properties();
			InputStream input = null;
			String configPath = "/mnt/nfs-share/IAS/ip_config.properties";
			
			try {
				input = new FileInputStream(configPath);
				prop.load(input);
				
				System.out.println(prop.getProperty("MYSQL"));
				String ipPort = prop.getProperty("MYSQL");
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
			
			
		
			String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		  String DB_URL = "jdbc:mysql://" + prop.getProperty("MYSQL")  + "/healthcare";
		  String USER = "gaurav";
		  String PASS = "gaurav";
		
		   
		   Connection conn = null;
		   Statement stmt = null;
		   
		Parser parser = new Parser("/mnt/nfs-share/HealthCare/app.xml");
		List<AppData> appDataList = parser.getAppData();
		List<Event> eventDataList = parser.getEventData();
//		List<Action> actionDataList = parser.getActionData();
		List<SensorType> sensorDataList = parser.getSensorTypeData();
		
		try{
		      //STEP 2: Register JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");

		      //STEP 3: Open a connection
		      System.out.println("Connecting to a selected database...");
		      conn = DriverManager.getConnection(DB_URL, USER, PASS);
		      System.out.println("Connected database successfully...");
		      
		      //STEP 4: Execute a query
		      System.out.println("Inserting records into the table...");
		      stmt = conn.createStatement();
		      
		      for(AppData appData : appDataList)
		      {
		    	  String sql = "INSERT INTO application " +
		                   "VALUES (' "+ appData.getId() + "',' " + appData.getName() + " ')";
		    	  stmt.executeUpdate(sql);
		      }
		      
		      
		      for(Event event : eventDataList)
		      {
		    	  String sql = "INSERT INTO events " +
		                   "VALUES (' "+ event.getEventSensorId() + "',' " + event.getEventTypeName() + "',' " + event.getEventName() + " ')";
		    	  stmt.executeUpdate(sql);
		      }
		      
		      
//		      for(Action action : actionDataList)
//		      {
//		    	  String sql = "INSERT INTO actions " +
//		                   "VALUES (' " + action.getEventId() + "',' " 
//		                   				+ action.getSensorActionId() + "',' "
//		                   				+ action.getActionJarLocation() + "',' "
//		                   				+ action.getClassDescription() + "',' "
//		                   				+ action.getLocation() + " ')";
//		    	  stmt.executeUpdate(sql);
//		      }
//		      
		      
		      for(SensorType sensorType : sensorDataList)
		      {
		    	  String sql = "INSERT INTO sensor " +
		                   "VALUES (' " + sensorType.getSensorTypeId() + "',' "
		                		   		+ appDataList.get(0).getId() + "',' "
		                				+ sensorType.getSensorName() + "',' "
		                   				+ sensorType.getSensorId() + " ')";
		    	  stmt.executeUpdate(sql);
		      }
		      System.out.println("Inserted records into the table...");

		   }catch(SQLException se){
		      //Handle errors for JDBC
		      se.printStackTrace();
		   }catch(Exception e){
		      //Handle errors for Class.forName
		      e.printStackTrace();
		   }finally{
		      //finally block used to close resources
		      try{
		         if(stmt!=null)
		            conn.close();
		      }catch(SQLException se){
		      }// do nothing
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }//end finally try
		   }//end try
		   System.out.println("Goodbye!");
		   
		   
		}//end main
	}//end JDBCExample
