package xmlParser;

import java.util.List;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Parser parser = new Parser("/mnt/nfs-share/HealthCare/app.xml");
		List<AppData> appData = parser.getAppData();
		List<Event> eventData = parser.getEventData();
		List<SensorType> sensorData = parser.getSensorTypeData();
		System.out.println(appData.get(0).getId());
		System.out.println(appData.get(0).getName());
		System.out.println(eventData.get(0).getEventName());
		System.out.println(sensorData.get(0).getSensorName());
	}

}
