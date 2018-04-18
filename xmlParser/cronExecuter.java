package xmlParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

public class cronExecuter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String xmlPath = "src/resources/cron.xml";
		CronParser cronParser = new CronParser();
		CronData cronData = cronParser.getCronData(xmlPath);
		
		for(int i = 0; i < cronData.cron.tasks.size(); i++) {
			String cronTime = cronData.cron.tasks.get(i).getTime();
			
			LocalTime now = LocalTime.now();
			LocalTime cTime = LocalTime.parse(cronTime);
			System.out.println(now);
			
			SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
			try {
				String startTime = now.toString();
			    String endTime = cronTime;
			    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			    Date d1 = sdf.parse(startTime);
			    Date d2 = sdf.parse(endTime);
			    long elapsed = d2.getTime() - d1.getTime(); 
			    elapsed /= 60000;
			    System.out.println(elapsed);
			    if(elapsed < 10) {
			    	
			    }
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
	}

}
