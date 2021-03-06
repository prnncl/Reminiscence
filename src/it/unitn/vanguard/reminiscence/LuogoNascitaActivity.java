package it.unitn.vanguard.reminiscence;

import it.unitn.vanguard.reminiscence.asynctasks.GetSuggLuogoNascita;
import it.unitn.vanguard.reminiscence.asynctasks.LuogoNascitaTask;
import it.unitn.vanguard.reminiscence.interfaces.OnTaskFinished;
import it.unitn.vanguard.reminiscence.utils.Constants;
import it.unitn.vanguard.reminiscence.utils.FinalFunctionsUtilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

public class LuogoNascitaActivity extends Activity implements OnTaskFinished {

	private Context context;

	protected ProgressDialog p;
	private Button btnLuogoNascitaConfirm;
	private AutoCompleteTextView txtLuogoNascita;
	private boolean placeOk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = LuogoNascitaActivity.this;

		String language = FinalFunctionsUtilities
				.getSharedPreferences(Constants.LANGUAGE_KEY, context);
		FinalFunctionsUtilities.switchLanguage(new Locale(language), context);
		setContentView(R.layout.activity_luogo_nascita);
		
		initializeButtons();
		initializeListeners();
	}

	private void initializeButtons() {
		btnLuogoNascitaConfirm = (Button) findViewById(R.id.btnLuogoNascita);
		txtLuogoNascita = (AutoCompleteTextView) findViewById(R.id.txtLuogoNascita);
		txtLuogoNascita.setTextColor(getResources().getColor(android.R.color.black));
	}

	private void initializeListeners() {

		txtLuogoNascita.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

			@Override
			public void afterTextChanged(Editable arg0) {

				String place = txtLuogoNascita.getText().toString();
				placeOk = !place.trim().equals("");
				if (!placeOk) {
					Toast.makeText(context,
							getResources().getText(R.string.birthplace_empty),
							Toast.LENGTH_SHORT).show();
				} else if (!(placeOk = placeOk && !place.startsWith(" ")
						&& !place.endsWith(" "))) {
					Toast.makeText(
							context,
							getResources().getText(
									R.string.birthplace_contains_spaces),
							Toast.LENGTH_SHORT).show();
				} else if (FinalFunctionsUtilities.isDeviceConnected(context)) {
					new GetSuggLuogoNascita(LuogoNascitaActivity.this)
							.execute(place.replace(" ", "+"));
				}

				if (!placeOk) {
					txtLuogoNascita
							.setBackgroundResource(R.drawable.txt_input_bordered_error);
				} else {
					txtLuogoNascita
							.setBackgroundResource(R.drawable.txt_input_bordered);
				}
			}
		});

		btnLuogoNascitaConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String place = txtLuogoNascita.getText().toString();
				placeOk = !place.trim().equals("");
				if (!placeOk) {
					Toast.makeText(context,
							getResources().getText(R.string.birthplace_empty),
							Toast.LENGTH_SHORT).show();
				} else if (!(placeOk = placeOk && !place.startsWith(" ")
						&& !place.endsWith(" "))) {
					Toast.makeText(
							context,
							getResources().getText(
									R.string.birthplace_contains_spaces),
							Toast.LENGTH_SHORT).show();
				} else if (FinalFunctionsUtilities.isDeviceConnected(context)) {

					FinalFunctionsUtilities.setSharedPreferences(
							Constants.LOUGO_DI_NASCITA_PREFERENCES_KEY,
							txtLuogoNascita.getText().toString(),
							LuogoNascitaActivity.this);
					
					try {
						new LuogoNascitaTask(LuogoNascitaActivity.this).execute(place);
					} catch (Exception e) {
						Log.e(LuogoNascitaActivity.class.getName(), e.toString());
						e.printStackTrace();
					}
				} else {
					Toast.makeText(context,
							getResources().getString(R.string.connection_fail),
							Toast.LENGTH_LONG).show();
				}

				if (!placeOk) {
					txtLuogoNascita
							.setBackgroundResource(R.drawable.txt_input_bordered_error);
				} else {
					txtLuogoNascita
							.setBackgroundResource(R.drawable.txt_input_bordered);
				}
			}
		});
	}

	private List<String> removeDuplicate(List<String> sourceList) {
		Set<String> setPmpListArticle = new HashSet<String>(sourceList);
		return new ArrayList<String>(setPmpListArticle);
	}

	@Override
	public void onTaskFinished(JSONObject res) {

			try {
				
				if (res.getString("success").equals("true")
						&& res.getString("Operation").equals("setLuogoNascita") ) {
						
						Intent loginIntent = new Intent(context, ViewStoriesActivity.class);
						loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(loginIntent, 0);
						finish();
						
				} else {
					Toast.makeText(
							context,
							getResources().getString(
									R.string.connection_fail),
							Toast.LENGTH_LONG).show();
				}
			} catch (JSONException je) {
				
				ArrayList<String> sugg = new ArrayList<String>();
				try {
					sugg.add(res.getString("mun0"));
					sugg.add(res.getString("mun1"));
					sugg.add(res.getString("mun2"));
					sugg.add(res.getString("mun3"));
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							context, R.layout.my_item_view, removeDuplicate(sugg));
					
					txtLuogoNascita.setThreshold(2);
					txtLuogoNascita.setAdapter(adapter);
					txtLuogoNascita.showDropDown();
					
				} catch (JSONException e) {
					Log.e(LuogoNascitaActivity.class.getName(), e.toString());
					e.printStackTrace();
					Toast.makeText(
							context,
							getResources().getString(R.string.registration_failed),
							Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Log.e(LuogoNascitaActivity.class.getName(), e.toString());
				e.printStackTrace();
				Toast.makeText(
						context,
						getResources().getString(R.string.registration_failed),
						Toast.LENGTH_LONG).show();
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
