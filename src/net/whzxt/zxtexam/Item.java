package net.whzxt.zxtexam;

import java.util.ArrayList;

import android.location.Location;

public class Item {
	private int _itemid;
	private String _itemname;
	private String _tts;
	private int _timeout;
	private float _lon, _lat;
	private ArrayList<Action> _actions;
	
	public ArrayList<Action> getActions() {
		return _actions;
	}
	
	public void setActions(ArrayList<Action> actions) {
		_actions = actions;
	}

	public int getItemId() {
		return _itemid;
	}

	public void setItemId(int itemid) {
		_itemid = itemid;
	}

	public String getItemName() {
		return _itemname;
	}

	public void setItemName(String itemname) {
		_itemname = itemname;
	}

	public String getTts() {
		return _tts;
	}

	public void setTts(String tts) {
		_tts = tts;
	}

	public int getTimeout() {
		return _timeout;
	}

	public void setTimeout(int timeout) {
		_timeout = timeout;
	}

	public Location getLatlon() {
		Location loa = new Location("reverseGeocoded");
		loa.setLatitude(_lat);
		loa.setLongitude(_lon);
		return loa;
	}

	public void setLatlon(float lat, float lon) {
		_lat = lat;
		_lon = lon;
	}
}
