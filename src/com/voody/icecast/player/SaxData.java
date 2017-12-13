package com.voody.icecast.player;

import java.util.ArrayList;

public class SaxData {  
  //public String server_name; 
  //public String listen_url; 
  //public String bitrate;
  //public String genre;
 
  private ArrayList<String> server_name = new ArrayList<String>();
  private ArrayList<String> listen_url = new ArrayList<String>();
  private ArrayList<String> bitrate = new ArrayList<String>();
  private ArrayList<String> genre = new ArrayList<String>();

    private int count = 0;

  public SaxData() { 
	  
  }
  
  public ArrayList<String> getServerName() {
      return server_name;
  }
  public void setServerName(String server_name) {
      this.server_name.add(server_name);
  }
  
  public ArrayList<String> getListenUrl() {
      return listen_url;
  }
  public void setListenUrl(String listen_url) {
      this.listen_url.add(listen_url);
  }
  
  public ArrayList<String> getBitrate() {
      return bitrate;
  }
  public void setBitrate(String bitrate) {
      this.bitrate.add(bitrate);
  }

  public ArrayList<String> getGenre() {
      return genre;
  }
  public void setGenre(String genre) {
      this.genre.add(genre);
  }

  public void setCount() {
      count ++;
  }

    public int getCount() {
        return count;
    }
}