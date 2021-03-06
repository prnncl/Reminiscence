package it.unitn.vanguard.reminiscence;

import it.unitn.vanguard.reminiscence.asynctasks.LoginTask;
import it.unitn.vanguard.reminiscence.interfaces.OnTaskFinished;
import it.unitn.vanguard.reminiscence.utils.Constants;
import it.unitn.vanguard.reminiscence.utils.FinalFunctionsUtilities;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnTaskFinished {

	private Context context;
	
	private Button btnLogin;
	private Button btnRegistration;
	private EditText usernameEditText;
	private EditText passwordEditText;

	protected ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = LoginActivity.this;
	
		// Se l'utente aveva gia' affettuato il login in precedenza salta dirett nella timeline
		if(FinalFunctionsUtilities.isLoggedIn(context)) { 
			Intent timeline = new Intent(context, ViewStoriesActivity.class);
			startActivity(timeline);
			finish();
		}
		
		String language = FinalFunctionsUtilities
				.getSharedPreferences(Constants.LANGUAGE_KEY, context);
		FinalFunctionsUtilities.switchLanguage(new Locale(language), context);
		setContentView(R.layout.activity_login);
		initializeButtons();
		initializeListeners();
	}

	private void initializeButtons() {
		// Set the Typeface for logo label..
		TextView logo = (TextView)findViewById(R.id.Logo);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"Pacifico.ttf");
		logo.setTypeface(typeFace);
		
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnRegistration = (Button) findViewById(R.id.btnRegistration);
		usernameEditText = (EditText) findViewById(R.id.edittextUsername);
		passwordEditText = (EditText) findViewById(R.id.edittextPassword);
	}

	private void initializeListeners() {

		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String username = usernameEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				boolean isEmptyUsername = username.trim().equals("");
				boolean isEmptyPassword = password.trim().equals("");
				
				if(isEmptyUsername && isEmptyPassword) {
					Toast.makeText(context, getResources().getString(R.string.login_empty_both), Toast.LENGTH_LONG).show();
				}
				else if(isEmptyUsername) {
					Toast.makeText(context, getResources().getString(R.string.login_empty_username), Toast.LENGTH_LONG).show();
				}
				else if(isEmptyPassword) {
					Toast.makeText(context, getResources().getString(R.string.login_empty_password), Toast.LENGTH_LONG).show();
				}
				else if(FinalFunctionsUtilities.isDeviceConnected(context)) {
					dialog = new ProgressDialog(context);
					dialog.setTitle(getResources().getString(R.string.please));
					dialog.setMessage(getResources().getString(R.string.wait));
					dialog.setCancelable(false);
					dialog.show();
					try {
						new LoginTask(LoginActivity.this).execute(username, password);
					} catch (Exception e) {
						Log.e(LoginActivity.class.getName(), e.toString());
					}
				}
				else {
					if(dialog!=null && dialog.isShowing()) { 	dialog.dismiss(); }
					Toast.makeText(context, getResources().getString(R.string.connection_fail), Toast.LENGTH_LONG).show();
				}
			}
		});
		btnRegistration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent regIntent = new Intent(context, RegistrationActivity.class);
				startActivity(regIntent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		});
		
		usernameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					usernameEditText.setBackgroundResource(R.drawable.txt_input_bordered_ok);
				} else {
					usernameEditText.setBackgroundResource(R.drawable.txt_input_bordered);
				}
			}
		});

		passwordEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					passwordEditText.setBackgroundResource(R.drawable.txt_input_bordered_ok);
				} else {
					passwordEditText.setBackgroundResource(R.drawable.txt_input_bordered);
				}
			}
		});
	}
	
	@Override
	public void onTaskFinished(JSONObject res) {
		
		if(dialog!=null && dialog.isShowing()) dialog.dismiss();
		try {
			if (res.getString("success").equals("true")) {
				
				FinalFunctionsUtilities.setSharedPreferences(Constants.NAME_KEY, res.getString("nome"), context);
				FinalFunctionsUtilities.setSharedPreferences(Constants.SURNAME_KEY, res.getString("cognome"), context);
				FinalFunctionsUtilities.setSharedPreferences(Constants.DAY_KEY, res.getString("day"), context);
				FinalFunctionsUtilities.setSharedPreferences(Constants.MONTH_KEY, res.getString("month"), context);
				FinalFunctionsUtilities.setSharedPreferences(Constants.YEAR_KEY, res.getString("year"), context);
				FinalFunctionsUtilities.setSharedPreferences(Constants.LOUGO_DI_NASCITA_PREFERENCES_KEY, res.getString("luogonascita"), context);
				
				startActivity(new Intent(context, ViewStoriesActivity.class));
				finish();
			} else {
				Toast.makeText(context, getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
			Log.e(LoginActivity.class.getName(), e.toString());
		} catch (Exception e) {
			Log.e(LoginActivity.class.getName(), e.toString());
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		String language = FinalFunctionsUtilities
				.getSharedPreferences(Constants.LANGUAGE_KEY, context);
		Locale locale = new Locale(language);

		if(locale.toString().equals(Locale.ITALIAN.getLanguage()) || locale.toString().equals(Locale.ITALY.getLanguage())) {
			menu.getItem(0).setIcon(R.drawable.it);
		}
		else if(locale.toString().equals(Locale.ENGLISH.getLanguage())) {
			menu.getItem(0).setIcon(R.drawable.en);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Locale locale = null;
		switch (item.getItemId()) {
		    case R.id.action_language_it: { locale = Locale.ITALY; break; }
		    case R.id.action_language_en: { locale = Locale.ENGLISH; break; }
		    case android.R.id.home: this.finish();break;
	    }
		
		// Refresh activity
		if(locale != null && FinalFunctionsUtilities.switchLanguage(locale, context)) {
		    finish();
		    startActivity(getIntent());
	    }
	    return true;
	}
}
