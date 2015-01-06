package com.example.crowdbikemobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import at.abraxas.amarino.Amarino;

public class MainActivity extends Activity implements SensorEventListener{

	private static final String DEVICE_ADDRESS = "00:12:00:09:03:01";
	private String latitudeString  = "";
	private String longitudeString = "";
	private Tempo tempoLocal = new Tempo();
	private GPSTracker gps;
	private int bgColor = 0;
	private SensorManager mSensorManager;
	private Sensor sluminosity;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        //Captando a posi��o geogr�fica
        posicaoGeografica();
        
        //Executando tarefas paralelas
        tarefasParalelas();
        
        //Setando a cor de fundo. Padr�o: verde
        setarCorDeFundo(R.color.verde);
        
        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sluminosity    = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        
        //Conectando ao ardu�no
        Amarino.connect(this, DEVICE_ADDRESS);
    }

    @Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, sluminosity, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
    /**
     * Este m�todo seta a cor de fundo do aplicativo
     * @param cor	C�digo inteiro da cor. A lista de cores dispon�veis est� em res/calues/colors.xml
     */
    public void setarCorDeFundo(int intColor){
    	bgColor = intColor;
    	
    	String stringColor = getResources().getString(intColor);
    	LinearLayout layoutApp = (LinearLayout)findViewById(R.id.backgroundApp);
    	layoutApp.setBackgroundColor(Color.parseColor(stringColor));
    }
    
    /**
     * Este m�todo identifica a posi��o geogr�fica do aparelho
     * A posi��o � armazenada nos atributos privados latitudeString e longitudeString,
     * 
     */
    public void posicaoGeografica(){
    	
        //Instancia um objeto GPSTracker
        gps = new GPSTracker(MainActivity.this);

        // Checa se o GPS est� habilitado
 		if(gps.canGetLocation()){

 			latitudeString  = String.valueOf(gps.getLatitude());
 			longitudeString = String.valueOf(gps.getLongitude());
 			
 			tarefasParalelas();
 			
 		}else{
 			// N�o pode pegar a localiza��o
 			// GPS or Network est� desabilitado
 			// Pede para o usu�rio habilitar
 			gps.showSettingsAlert();
 		}
    }
    
    /**
     * Este m�todo executa as tarefas paralelas para:
     *  - Acesso ao servidor
     *  - Receber informa��es de tempo
     */
    public void tarefasParalelas(){
    	
    	//Toast.makeText(getApplicationContext(), "[TP] Sua localiza��o � - \nLat: " + latitudeString + "\nLong: " + longitudeString, Toast.LENGTH_LONG).show();
    	
        //Instanciando a asynctask para contato com o servidor e acesso ao ardu�no
        AsyncServidor task = new AsyncServidor(this);
        task.execute(latitudeString, longitudeString);
        
        //Instanciando a asynktask para contato com o servi�o de tempo
        AsyncTempo tempo = new AsyncTempo(this);
        tempo.execute(latitudeString, longitudeString);
    }
    
	/**
	 * Este m�todo seta o atributo tempoMain.
	 * Este atributo armazena as informa��es de tempo
	 * 
	 * @param tempoMain	objeto Tempo setado com as informa��es coletadas do tempo
	 */
	public void setTempoMain(Tempo tempoMain) {
		this.tempoLocal = tempoMain;
		
		if(tempoMain.getTemperatura() != null){
			ImageView iconWeather = (ImageView)findViewById(R.id.iconWeather);
			TextView  txtTemp 	  = (TextView) findViewById(R.id.temperatura);
			TextView  txtDesc 	  = (TextView) findViewById(R.id.previsao);
			TextView  txtUom 	  = (TextView) findViewById(R.id.txt_uom_temp);
			
			Integer temperatura = Double.valueOf(tempoLocal.getTemperatura()).intValue();
			
			//Exibindo o �cone
			iconWeather.setBackgroundDrawable(getResources().getDrawable(tempoLocal.getIcone()));
			
			//Exibindo a temperatura
			txtTemp.setText(temperatura.toString());
			
			//Exibindo �C
			txtUom.setText("�C");
			
			//Exibindo a descri��o
			txtDesc.setText(tempoLocal.getDescricao());
		}
	}
    
	/**
	 * M�todo que chama a ActivitySendNotification
	 *  
	 */
	public void sendNotification(View v){
		if(!latitudeString.equals("") && !longitudeString.equals("")){
			LinearLayout layout = (LinearLayout)findViewById(R.id.backgroundApp);
			
			Bundle bundle = new Bundle();
			Intent it = new Intent(this, ActivitySendNotification.class);
		
			bundle.putString("latitude", latitudeString);
			bundle.putString("longitude", longitudeString);
			bundle.putInt("cor", bgColor);
			
			it.putExtras(bundle);
			startActivityForResult(it, 100);
			
		}else{
			Toast.makeText(getApplicationContext(), "Problemas ao coletar sua localiza��o", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Este m�todo recebe a resposta da chamada da ActivitySendNotification  
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 100) {
            if(resultCode == Activity.RESULT_OK) {
            	Toast.makeText(getApplicationContext(), "Retorno", Toast.LENGTH_LONG).show();
            	setarCorDeFundo(R.color.vermelho);
            }
        }
    }
	
	/**
	 * Este m�todo recebe o retorno do servidor sobre o local atual
	 * Neste m�todo ser� exibida a notifica��o do lugar na tela,
	 * a cor de background ser� setada
	 * 
	 * @param retorno	Resposta do servidor
	 */
	public void retornoServidor(String retorno){
		Log.v("ARDUINO", "FAROL");
		
		//Esta linha deve ser alterada. A cor depende do resultado do servidor
        setarCorDeFundo(R.color.verde);
        
        TextView mensagem = (TextView)findViewById(R.id.txtMensagem);
        mensagem.setText("Via segura");
	}
	
	////////////////////////////////////////////////////////////////////////////
	/*
	 * M�todo que envia comando ao ardu�no
	 * 
	 */
	public void sendInformationToArduino(String informationText){
		Log.v("ARDUINO", "FAROL");
		
        String text = null;
        text = informationText;
        
    	Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'A', text);
    	Amarino.disconnect(this, DEVICE_ADDRESS);
    }
	
	////////////////////////////////////////////////////////////////////////////
	/*
	 * Os m�todos a seguir tratam do sensor de luminosidade
	 * 
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float lux = event.values[0];
		
		if(event.sensor.getType() == Sensor.TYPE_LIGHT){
	        lux = event.values[0];
	        Log.v("LUX", "Ligar farol. Lux = " + lux);
	        if (lux < 50){
	        	sendInformationToArduino("FAROL");
	        }
	     }
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
}
