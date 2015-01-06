package com.example.crowdbikemobilecar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private String latitudeString  = "";
	private String longitudeString = "";
	private GPSTracker gps;
	private Tempo tempoLocal = new Tempo();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		posicaoGeografica();
	}
	
	 /**
     * Este m�todo executa as tarefas paralelas para:
     *  - Acesso ao servidor
     *  - Receber informa��es de tempo
     */
    public void tarefasParalelas(){
    	Toast.makeText(getApplicationContext(), "[TP] Sua localiza��o � - \nLat: " + latitudeString + "\nLong: " + longitudeString, Toast.LENGTH_LONG).show();
    	
        //Instanciando a asynctask para contato com o servidor e acesso ao ardu�no
        AsyncServidor task = new AsyncServidor(this);
        task.execute(latitudeString, longitudeString);
        
        //Instanciando a asynktask para contato com o servi�o de tempo
        AsyncTempo tempo = new AsyncTempo(this);
        tempo.execute(latitudeString, longitudeString);
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
	
    //Se necess�rio tratar algum retorno do servidor
    public void retornoServidor(String retorno){
    	TextView  txtInformacao = (TextView) findViewById(R.id.txtMensagem);
    	txtInformacao.setText(retorno);
    }
    
}
