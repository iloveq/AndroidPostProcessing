package com.woaiqw.orm.db.sqlite;

import java.util.HashMap;

public class DbModel {

	private HashMap<String, Object> dataMap = new HashMap<String, Object>();
	
	public Object get(String column){
		return dataMap.get(column);
	}
	
	public String getString(String column){
		return String.valueOf(get(column));
	}
	
	public int getInt(String column){
		return Integer.valueOf(getString(column));
	}
	
	public boolean getBoolean(String column){
		return Boolean.valueOf(getString(column));
	}
	
	public double getDouble(String column){
		return Double.valueOf(getString(column));
	}
	
	public float getFloat(String column){
		return Float.valueOf(getString(column));
	}
	
	public long getLong(String column){
		return Long.valueOf(getString(column));
	}
	
	public void set(String key,Object value){
		dataMap.put(key, value);
	}
	
	public HashMap<String, Object> getDataMap(){
		return dataMap;
	}
}
