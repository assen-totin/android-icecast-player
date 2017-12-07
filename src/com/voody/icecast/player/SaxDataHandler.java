package com.voody.icecast.player;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
public class SaxDataHandler extends DefaultHandler {
	// booleans that check whether it's in a specific tag or not 
	private boolean _inSection, _elementOn; 
 
	// this holds the data 
	private SaxData _data; 

	// Returns the data object  
	public SaxData getData() {
		return _data;
	}
	
	// Sets the data object
	public void setData(SaxData data) {
		_data = data;
	}
 
	// Called when the XML document is first opened
	@Override 
	public void startDocument() throws SAXException { 
		_data = new SaxData(); 
	} 
 
	// Called when it's finished handling the document 
	@Override 
	public void endDocument() throws SAXException { 
 
	}
}
*/
public abstract class SaxDataHandler extends DefaultHandler {
	// Returns the data object
	public SaxData getData() {
		return null;
	}

	// Sets the data object
	public void setData(SaxData data) {
	}

	// Called when the XML document is first opened
	public void startDocument() throws SAXException {
	}

	// Called when it's finished handling the document
	public void endDocument() throws SAXException {
	}
}