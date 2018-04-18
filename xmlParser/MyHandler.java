package xmlParser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class MyHandler extends DefaultHandler {

    //List to hold Employees object
    private List<AppData> appList = null;
    private List<SensorType> sensorTypeList = null;
    private List<Event> eventList = null;
//    private List<Action> actionList = null;
    
    private AppData app = null;
    private SensorType sensorType = null;
    private Event event = null;
//    private Action action = null;

    //getter method for employee list
    public List<AppData> getAppList() {
        return appList;
    }
    
    public List<SensorType> getSensorTypeList() {
        return sensorTypeList;
    }
    
    public List<Event> getEventList() {
        return eventList;
    }
    
//    public List<Action> getActionList() {
//        return actionList;
//    }

    boolean bAppId = false;
    boolean bAppName = false;
    
    boolean bEventTypeName = false;
	boolean bEventSensorId = false;
	boolean bEventName = false;
	
	boolean bEventId = false;
	boolean bSensorActionId = false;
	boolean bActionJarLocation = false;
	boolean bClassDescription = false;
	boolean bLocation = false;
	
	boolean bSensorTypeId = false;
	boolean bSensorName = false;
	boolean bSensorId = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

    	// For Application Data
        if (qName.equalsIgnoreCase("appInfo")) {
            app = new AppData();
            if (appList == null)
            	appList = new ArrayList<>();
        } else if (qName.equalsIgnoreCase("name")) {
	       	bAppName = true;
	    } else if (qName.equalsIgnoreCase("id")) {
	       	bAppId = true;
	    } 
        
        // For SensorType Data
	    if (qName.equalsIgnoreCase("sensorType")) {
	    	sensorType = new SensorType();
            if (sensorTypeList == null)
            	sensorTypeList = new ArrayList<>();
    	} else if (qName.equalsIgnoreCase("sensorTypeId")) {
	    	bSensorTypeId = true;
	    } else if (qName.equalsIgnoreCase("sensorName")) {
	    	bSensorName = true;
	    } else if (qName.equalsIgnoreCase("sensorId")) {
	    	bSensorId = true;
	    }
	    
        // For Event Data
        if (qName.equalsIgnoreCase("event")) {
            event = new Event();
            if (eventList == null)
            	eventList = new ArrayList<>();
        } else if (qName.equalsIgnoreCase("eventTypeName")) {
	    	bEventTypeName = true;
	    } else if (qName.equalsIgnoreCase("eventSensorId")) {
	    	bEventSensorId = true;
	    } else if (qName.equalsIgnoreCase("eventName")) {
	    	bEventName = true;
	    } 
	    
        // For Action Data
//        if (qName.equalsIgnoreCase("action")) {
//        	action = new Action();
//            if (actionList == null)
//            	actionList = new ArrayList<>();
//    	} else if (qName.equalsIgnoreCase("eventId")) {
//	    	bEventId = true;
//	    } else if (qName.equalsIgnoreCase("sensorActionId")) {
//	    	bSensorActionId = true;
//	    } else if (qName.equalsIgnoreCase("actionJarLocation")) {
//	    	bActionJarLocation = true;
//	    } else if (qName.equalsIgnoreCase("classDescription")) {
//	    	bClassDescription = true;
//	    } else if (qName.equalsIgnoreCase("location")) {
//	    	bLocation = true;
//	    } 
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("appInfo")) {
            //add Employee object to list
            appList.add(app);
        }
        if (qName.equalsIgnoreCase("sensorType")) {
            //add Employee object to list
            sensorTypeList.add(sensorType);
        }
        if (qName.equalsIgnoreCase("event")) {
            //add Employee object to list
            eventList.add(event);
        }        
//        if (qName.equalsIgnoreCase("action")) {
//            //add Employee object to list
//            actionList.add(action);
//        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

        if (bAppId) {
        	app.setId(new String(ch, start, length));
            bAppId = false;
        } else if (bAppName) {
            app.setName(new String(ch, start, length));
            bAppName = false;
        } 
        else if (bSensorTypeId) {
            sensorType.setSensorTypeId(new String(ch, start, length));
            bSensorTypeId = false;
        }
        else if (bSensorName) {
            sensorType.setSensorName(new String(ch, start, length));
            bSensorName = false;
        }
        else if (bSensorId) {
            sensorType.setSensorId(new String(ch, start, length));
            bSensorId = false;
        }
        else if (bEventTypeName) {
            event.setEventTypeName(new String(ch, start, length));
            bEventTypeName = false;
        } 
        else if (bEventSensorId) {
            event.setEventSensorId(new String(ch, start, length));
            bEventSensorId = false;
        } 
        else if (bEventName) {
            event.setEventName(new String(ch, start, length));
            bEventName = false;
        }
//        else if (bEventId) {
//            action.setEventId(new String(ch, start, length));
//            bEventId = false;
//        }
//        else if (bSensorActionId) {
//            action.setSensorActionId(new String(ch, start, length));
//            bSensorActionId = false;
//        }
//        else if (bActionJarLocation) {
//            action.setActionJarLocation(new String(ch, start, length));
//            bActionJarLocation = false;
//        }
//        else if (bClassDescription) {
//            action.setClassDescription(new String(ch, start, length));
//            bClassDescription = false;
//        }
//        else if (bLocation) {
//            action.setLocation(new String(ch, start, length));
//            bLocation = false;
//        }
    }
}