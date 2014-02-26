package project.tuyatag;

import project.tuyatag.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class FormulaireConnexion extends Activity {
	public EditText pseudo;
	public EditText mdp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ecran_co);
	}
	
	
	public void confirmInscription(View v)
	{
		pseudo = (EditText) findViewById(R.id.editTextUserName);
		mdp = (EditText) findViewById(R.id.editTextPassword);

		boolean valide=true;
		//check si les champs sont pas vide
		if(pseudo.getText().toString().equals("") || mdp.getText().toString().equals(""))
		{
			//Inscription.afficheErreur(this, "les champs ne doivent pas �tre vide");
			valide= false;
		}
		
		if (valide)
		{
			//envoi requete HTTP CONNEXION
			
		}

		
	}
}
