package com.voody.icecast.player;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;

public class SaxDataHandler extends DefaultHandler { 
 
  // booleans that check whether it's in a specific tag or not 
  private boolean _inSection, _inArea; 
 
  // this holds the data 
  private SaxData _data; 
 
  /** 
   * Returns the data object 
   * 
   * @return 
   */ 
  public String getData() { 
    return _data; 
  } 
 
  /** 
   * This gets called when the xml document is first opened 
   * 
   * @throws SAXException 
   */ 
  @Override 
  public void startDocument() throws SAXException { 
    _data = new SaxData(); 
  } 
 
  /** 
   * Called when it's finished handling the document 
   * 
   * @throws SAXException 
   */ 
  @Override 
  public void endDocument() throws SAXException { 
 
  } 
 
  /** 
   * This gets called at the start of an element. Here we're also setting the booleans to true if it's at that specific tag. (so we 
   * know where we are) 
   * 
   * @param namespaceURI 
   * @param localName 
   * @param qName 
   * @param atts 
   * @throws SAXException 
   */ 
  @Override 
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException { 
 
    if(localName.equals("section")) { 
      _inSection = true; 
 
      _data.sectionId = atts.getValue("id"); 
    } else if(localName.equals("area")) { 
      _inArea = true; 
    } 
  } 
 
  /** 
   * Called at the end of the element. Setting the booleans to false, so we know that we've just left that tag. 
   * 
   * @param namespaceURI 
   * @param localName 
   * @param qName 
   * @throws SAXException 
   */ 
  @Override 
  public void endElement(String namespaceURI, String localName, String qName) throws SAXException { 
    Log.v("endElement", localName); 
 
    if(localName.equals("section")) { 
      _inSection = false; 
    } else if(localName.equals("area")) { 
      _inArea = false; 
    } 
  } 
 
  /** 
   * Calling when we're within an element. Here we're checking to see if there is any content in the tags that we're interested in 
   * and populating it in the Config object. 
   * 
   * @param ch 
   * @param start 
   * @param length 
   */ 
  @Override 
  public void characters(char ch[], int start, int length) { 
    String chars = new String(ch, start, length); 
    chars = chars.trim(); 
 
    if(_inSection) { 
      _data.section = chars; 
    } else if(_inArea) { 
      _data.area = chars; 
    } 
  } 
} 
