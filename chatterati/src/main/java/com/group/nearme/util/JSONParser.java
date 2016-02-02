package com.group.nearme.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;


public class JSONParser 
{
	
// constructor
	public JSONParser() {
	}
	public String getJSONString(String urlStr)
	{
	
		HttpURLConnection connection = null;
		int serverResponseCode;
		StringBuilder sb = null;
		try {
			System.setProperty("http.keepAlive", "false");
			URL url = new URL(urlStr);
		    connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.setUseCaches(false);
			connection.setRequestProperty("Accept", "application/json, */*");
			connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
			connection.connect();
			serverResponseCode = connection.getResponseCode();
			
			switch (serverResponseCode) {
			case HttpURLConnection.HTTP_OK:
				sb = new StringBuilder();
				InputStream in = connection.getInputStream();
				try {
					InputStreamReader inReader = new InputStreamReader(in);
					try {
						BufferedReader reader = new BufferedReader(inReader);
						try {
							String line;
							while ((line = reader.readLine()) != null) {
								sb.append(line);
							}
						} finally {
							if (reader != null)
								try {
									reader.close();
								} catch (IOException e) {
								}
							reader = null;
						}
					} finally {
						if (inReader != null)
							try {
								inReader.close();
							} catch (IOException e) {
							}
						inReader = null;
					}

				} finally {
					if (in != null)
						try {
							in.close();
						} catch (IOException e) {
						}
					in = null;
				}
				connection.disconnect();
				connection = null;
			
			}
		} catch (Exception e) {
           return "NA";
		}
		if(sb==null)
			return "NA";
		else
			return sb.toString();
	}
}
		
	
		

