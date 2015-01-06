package com.example.crowdbikemobilecar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncServidor extends AsyncTask <String, Void, String> {
	
	private Context contexto;
	
	public AsyncServidor(Context ctx) {
		this.contexto = ctx;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	@Override
	protected String doInBackground(String... params) {
		
		String latitude  = params[0];
		String longitude = params[1];
		
		String line;
		String result = "false";
		String resultado = "";
		
		/*
		 * Esta linha deve ser modificada.
		 * Aqui deve ser informada a uri do serviço que recebe as coordenadas
		 * geográficas 
		 * 
		 */
		String uri = "http://10.0.2.2:8080/projects/contexto/coordenada.php";
		
		int responseCode = 0;
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(uri);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			
			nameValuePairs.add(new BasicNameValuePair("latitude",  latitude ));
			nameValuePairs.add(new BasicNameValuePair("longitude", longitude));
			
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			int executeCount = 0;
			HttpResponse response;
			do {
				executeCount++;
				Log.v("TENTATIVA", "tentativa número:" + executeCount);

				// Execute HTTP Post Request
				response = client.execute(httppost);
				responseCode = response.getStatusLine().getStatusCode();						
				
			} while (executeCount < 5 && responseCode == 408);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			while ((line = rd.readLine()) != null){
				result = line.trim();
			}

			//Neste ponto result já guarda o Json puro
			Log.v("STATUS", result);

		} catch (Exception e) {
			responseCode = 408;
			e.printStackTrace();
		}

		return result;
		
	}

	@Override
	protected void onPostExecute(String result) {
		Log.v("SERVIDOR", "Retorno do servidor");
		super.onPostExecute(result);
		((MainActivity) contexto).retornoServidor(result);
	}
	
}
