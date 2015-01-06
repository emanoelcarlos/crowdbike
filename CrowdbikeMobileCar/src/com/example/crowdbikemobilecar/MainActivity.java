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
     * Este método executa as tarefas paralelas para:
     *  - Acesso ao servidor
     *  - Receber informações de tempo
     */
    public void tarefasParalelas(){
    	Toast.makeText(getApplicationContext(), "[TP] Sua localização é - \nLat: " + latitudeString + "\nLong: " + longitudeString, Toast.LENGTH_LONG).show();
    	
        //Instanciando a asynctask para contato com o servidor e acesso ao arduíno
        AsyncServidor task = new AsyncServidor(this);
        task.execute(latitudeString, longitudeString);
        
        //Instanciando a asynktask para contato com o serviço de tempo
        AsyncTempo tempo = new AsyncTempo(this);
        tempo.execute(latitudeString, longitudeString);
    }
	
	/**
     * Este método identifica a posição geográfica do aparelho
     * A posição é armazenada nos atributos privados latitudeString e longitudeString,
     * 
     */
    public void posicaoGeografica(){
        //Instancia um objeto GPSTracker
        gps = new GPSTracker(MainActivity.this);

        // Checa se o GPS está habilitado
 		if(gps.canGetLocation()){

 			latitudeString  = String.valueOf(gps.getLatitude());
 			longitudeString = String.valueOf(gps.getLongitude());
 			
 			tarefasParalelas();
 			
 		}else{
 			// Não pode pegar a localização
 			// GPS or Network está desabilitado
 			// Pede para o usuário habilitar
 			gps.showSettingsAlert();
 		}
    }
    
	/**
	 * Este método seta o atributo tempoMain.
	 * Este atributo armazena as informações de tempo
	 * 
	 * @param tempoMain	objeto Tempo setado com as informações coletadas do tempo
	 */

	public void setTempoMain(Tempo tempoMain) {
		this.tempoLocal = tempoMain;
		
		if(tempoMain.getTemperatura() != null){
			ImageView iconWeather = (ImageView)findViewById(R.id.iconWeather);
			TextView  txtTemp 	  = (TextView) findViewById(R.id.temperatura);
			TextView  txtDesc 	  = (TextView) findViewById(R.id.previsao);
			TextView  txtUom 	  = (TextView) findViewById(R.id.txt_uom_temp);
			
			Integer temperatura = Double.valueOf(tempoLocal.getTemperatura()).intValue();
			
			//Exibindo o ícone
			iconWeather.setBackgroundDrawable(getResources().getDrawable(tempoLocal.getIcone()));
			
			//Exibindo a temperatura
			txtTemp.setText(temperatura.toString());
			
			//Exibindo ºC
			txtUom.setText("ºC");
			
			//Exibindo a descrição
			txtDesc.setText(tempoLocal.getDescricao());
		}
	}
	
    //Se necessário tratar algum retorno do servidor
    public void retornoServidor(String retorno){
    	TextView  txtInformacao = (TextView) findViewById(R.id.txtMensagem);
    	txtInformacao.setText(retorno);
    }
    
}
