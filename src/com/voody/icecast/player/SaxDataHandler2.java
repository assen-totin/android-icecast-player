package com.voody.icecast.player;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxDataHandler2 extends SaxDataHandler {
	// booleans that check whether it's in a specific tag or not 
	//private boolean _inSection, _elementOn;
 
	// this holds the data 
	private SaxData _data; 
	
	// This holds the value of each element
	private String _elementValue;
 
	// String Builder
	StringBuilder sb;
	
	// Returns the data object  
	public SaxData getData() {
		return _data;
	}
	
	// Sets the data object
	public void setData(SaxData data) {
		_data = data;
	}
 
	// This gets called when the xml document is first opened 
	@Override 
	public void startDocument() throws SAXException { 
		_data = new SaxData(); 
	} 
 
	// Called when it's finished handling the document 
	@Override 
	public void endDocument() throws SAXException {
	} 
 
	// This gets called at the start of an element. 
	// Here we're also setting the booleans to true if it's at that specific tag 
	// (so we know where we are) 
 
	@Override 
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		String name, value;

		if(localName.equals("result")) {
			// Do nothing; this is the opening tag for whole file 
		}
		else if(localName.equals("station")) {
			for (int i=0; i<atts.getLength(); i++) {
				name = atts.getLocalName(i);
				value = atts.getValue(i);

				switch(name) {
					case "name":
						_data.setServerName(value);
						break;

					case "url":
						_data.setListenUrl(value);
						break;

					case "bitrate":
						_data.setBitrate(value);
						break;

					case "tags":
						_data.setGenre(value);
						break;
				}
			}

			_data.setCount();
		}
	} 
 
    // Called at the end of the element. 
	// Setting the booleans to false, so we know that we've just left that tag. 
	@Override 
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	} 

	// Calling when we're within an element. 
	// Here we're checking to see if there is any content in the tags that we're interested in 
    // and populating it in the Config object. 
	@Override 
	public void characters(char ch[], int start, int length) { 
		//String chars = new String(ch, start, length); 
		//chars = chars.trim();
	}
}
