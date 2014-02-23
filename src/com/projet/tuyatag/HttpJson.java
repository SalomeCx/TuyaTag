package com.projet.tuyatag;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

/*Creer la requete HTTP pour recuperer les donnees JSON et retourne un objet JSON
Note : dans la chaine JSON, "[ ]" est un tableau, "{}" est un objet*/

public class HttpJson  {
	private DefaultHttpClient httpClient;
	//private static String json = "";

	private static final String DEBUG_TAG = "Discovart/HttpJson";

	public HttpJson(){}

	public JSONArray getJSONFromUrl(String url) {
		return getJSONFromUrl(url, null);
	}
	
	public JSONArray getJSONFromUrl(String url, List<NameValuePair> values) {
		JSONArray returnValue = null;
		InputStream is = null;
		boolean KO = false;
		try {		
			httpClient = new DefaultHttpClient();
			HttpParams httpParameters = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
			HttpConnectionParams.setSoTimeout(httpParameters, 20000);
			HttpConnectionParams.getConnectionTimeout(httpParameters);
			HttpPost httpPost = new HttpPost(url);
			if( values != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(values));
			}
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();   
		} catch (Exception e) {
			KO = true;
			Log.e(DEBUG_TAG, "HTTP error");
			e.printStackTrace();
			returnValue = null;
		}

				
		String json = "";
		if(!KO){
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8000);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				is.close();
				json = sb.toString();
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Read buffer error");
				e.printStackTrace();
			}

			try {
				returnValue = new JSONArray(json);
				httpClient.getConnectionManager().shutdown();
			}
			catch (JSONException e){
				Log.e(DEBUG_TAG, "JSON Parser, Error parsing data");
				e.printStackTrace();
				returnValue = null;;
			}
		}
		return returnValue;
	}
}