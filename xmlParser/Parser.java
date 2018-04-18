package xmlParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import xmlParser.AppData;
import xmlParser.SensorType;
import xmlParser.Event;


public class Parser {
	
	SAXParserFactory saxParserFactory;
	SAXParser saxParser;
	MyHandler handler;
	
	public Parser(String xmlFilePath)
	{
		try
		{
	  		 saxParserFactory = SAXParserFactory.newInstance();
			 saxParser = saxParserFactory.newSAXParser();
			 handler = new MyHandler();
			 saxParser.parse(new File(xmlFilePath), handler);
		}
		catch(ParserConfigurationException | SAXException | IOException e) {
	        e.printStackTrace();
		}
	}
	
	public List<AppData> getAppData()
	{
		return handler.getAppList();
	}
	
	public List<Event> getEventData()
	{
		return handler.getEventList();
	}
	
//	public List<Action> getActionData()
//	{
//		return handler.getActionList();
//	}
	
	public List<SensorType> getSensorTypeData()
	{
		return handler.getSensorTypeList();
	}
	
}
