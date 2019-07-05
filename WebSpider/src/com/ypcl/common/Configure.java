package com.ypcl.common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configure {
	private Map<String, String> conf = new HashMap<String, String>();
	
	public Configure() throws ParserConfigurationException, SAXException, IOException {
		this("conf.xml");
	}
	
	public Configure(String path) throws ParserConfigurationException, SAXException, IOException {
		File f = new File(path);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(f);
		NodeList nl = doc.getElementsByTagName("property");
		
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				NodeList ns = node.getChildNodes();

				String key = null, value = null;
                for (int j = 0; j < ns.getLength(); j++) {
                    Node record = ns.item(j);

                    if (record.getNodeType() == Node.ELEMENT_NODE) {
	                    if (record.getNodeName().equals("name"))
	                    {
	                        key = record.getTextContent().trim();
	                    } else if (record.getNodeName().equals("value")) {
	                    	value = record.getTextContent().trim();
	                    }
                    }
                }
                
                if (key != null && value != null) {
                	conf.put(key, value);
                }
			}
		}
	}
	
	public Configure setConf(String key, String value) {
		conf.put(key,  value);
		return this;
	}
	
	public String getConf(String key) {
		return conf.get(key);
	}
	
	public String getString(String key, String def) {
		String s = conf.get(key);
		if (s == null) {
			return def;
		} else {
			return s;
		}
	}
	
	public int getInt(String key, int def) {
		String s = conf.get(key);
		if (s == null) {
			return def;
		} else {
			return Integer.parseInt(s);
		}
	}
	
	public float getFloat(String key, float def) {
		String s = conf.get(key);
		if (s == null) {
			return def;
		} else {
			return Float.parseFloat(s);
		}
	}
	
	public double getDouble(String key, double def) {
		String s = conf.get(key);
		if (s == null) {
			return def;
		} else {
			return Double.parseDouble(s);
		}
	}
	
	public byte getByte(String key, byte def) {
		String s = conf.get(key);
		if (s == null) {
			return def;
		} else {
			return Byte.parseByte(s);
		}
	}
	
	public boolean getBoolean(String key, boolean def) {
		String s = conf.get(key);
		if (s == null) {
			return def;
		} else {
			return Boolean.parseBoolean(s);
		}
	}
}
