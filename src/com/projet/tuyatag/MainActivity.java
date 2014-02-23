package com.projet.tuyatag;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import fr.irisa.discovart.compte.GetWebSession;
import fr.irisa.discovart.compte.SignUPActivity;

public class MainActivity extends Activity {

	public Button login_oui;
	public Button bouton_connexion;
	public Button bouton_inscription;
	
	 @SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ecran_accueil);
		login_oui = (Button) findViewById(R.id.buttonPhoto);
		bouton_connexion = (Button) findViewById(R.id.buttonConnexion);
		//bouton_inscription = (Button) findViewById(R.id.acces_inscription);
		 StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
         .detectDiskReads()
         .detectDiskWrites()
         .detectNetwork()   // or .detectAll() for all detectable problems
         .penaltyLog()
         .build());
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
         .detectLeakedSqlLiteObjects()
         .detectLeakedClosableObjects()
         .penaltyLog()
         .penaltyDeath()
         .build());

	}



	public void onClickConnexion(View v) {
		//Toast.makeText(this,"bouton yes marche", Toast.LENGTH_LONG).show();
		Intent intent= new Intent(this, GetWebSession.class);
		
		startActivity(intent)	;
		}
	public void onClickTakePicture(View v) {
		//Toast.makeText(this,"bouton no marche", Toast.LENGTH_LONG).show();
		Intent intent= new Intent(this, TakePictureActivity.class);
		
		startActivity(intent)	;
		}
	public void onClickAccesCompte(View v)
	{
		
	}
	
	public void onClickInscription(View v)
	{
		Intent intent= new Intent(this, SignUPActivity.class);
		startActivity(intent)	;	
	}
	
	public void onClickAccesGalerie(View v)
	{
		Intent intent= new Intent(this, ListTagActivity.class);
		startActivity(intent)	;	
	}
	
	public void onClickLearnMore(View v)
	{
		Intent intent= new Intent(this, AboutTuyatag.class);
		startActivity(intent)	;	
	}
	
}
