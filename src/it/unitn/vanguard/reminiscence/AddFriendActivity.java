package it.unitn.vanguard.reminiscence;

import it.unitn.vanguard.reminiscence.asynctasks.AddFriendTask;
import it.unitn.vanguard.reminiscence.interfaces.OnTaskFinished;
import it.unitn.vanguard.reminiscence.utils.Constants;
import it.unitn.vanguard.reminiscence.utils.FinalFunctionsUtilities;

import java.util.Locale;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddFriendActivity extends Activity implements OnTaskFinished {

	private Context context;

	private EditText editTextName;
	private EditText editTextSurname;
	private EditText editTextMail;
	private Button confirmBtn;
	private Button backBtn;
	private String name, surname, mail;
	protected ProgressDialog dialog;
	private boolean nameOk = false, surnameOk = false, mailOk = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = AddFriendActivity.this;

		String language = FinalFunctionsUtilities.getSharedPreferences(
				Constants.LANGUAGE_KEY, context);
		FinalFunctionsUtilities.switchLanguage(new Locale(language), context);
		setContentView(R.layout.activity_add_friend);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		initializeButtons();
		initializeListeners();
	}

	private void initializeButtons() {
		editTextName = (EditText) findViewById(R.id.add_friend_name_et);
		editTextSurname = (EditText) findViewById(R.id.add_friend_surname_et);
		editTextMail = (EditText) findViewById(R.id.add_friend_mail_et);
		confirmBtn = (Button) findViewById(R.id.add_friend_confirm_button);
		backBtn = (Button) findViewById(R.id.add_friend_back_button);

		name = editTextName.getText().toString();
		surname = editTextSurname.getText().toString();
		mail = editTextMail.getText().toString();

		nameOk = !(name.trim().equals(""));
		surnameOk = !(surname.trim().equals(""));
		mailOk = FinalFunctionsUtilities.isValidEmailAddress(mail);
	}

	private void initializeListeners() {
		View.OnClickListener confirmListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (nameOk && surnameOk && mailOk) {
					if (FinalFunctionsUtilities.isDeviceConnected(context)) {
						dialog = new ProgressDialog(context);
						dialog.setTitle(getResources().getString(
								R.string.please));
						dialog.setMessage(getResources().getString(
								R.string.wait));
						dialog.setCancelable(false);
						dialog.show();
						try {
							new AddFriendTask(AddFriendActivity.this).execute(
									name, surname, mail);
						} catch (Exception e) {
							Log.e(AddFriendActivity.class.getName(),
									e.toString());
						}
					} else {
						Toast.makeText(context, R.string.connection_fail,
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(AddFriendActivity.this,
							R.string.add_friend_checkinputs, Toast.LENGTH_LONG)
							.show();
				}
			}
		};

		confirmBtn.setOnClickListener(confirmListener);
		backBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		editTextName.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				name = editTextName.getText().toString();
				nameOk = !name.trim().equals("");
				if (!nameOk) {
					Toast.makeText(
							context,
							getResources().getText(
									R.string.registration_surname_empty),
							Toast.LENGTH_SHORT).show();
				} else if (!(nameOk = nameOk && !name.startsWith(" ")
						&& !name.endsWith(" "))) {
					Toast.makeText(
							context,
							getResources()
									.getText(
											R.string.registration_surname_contains_spaces),
							Toast.LENGTH_SHORT).show();
				}

				if (!nameOk) {
					editTextName
							.setBackgroundResource(R.drawable.txt_input_bordered_error);
				} else {
					editTextName
							.setBackgroundResource(R.drawable.txt_input_bordered);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		editTextSurname.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				surname = editTextSurname.getText().toString();
				surnameOk = !surname.trim().equals("");
				if (!surnameOk) {
					Toast.makeText(
							context,
							getResources().getText(
									R.string.registration_surname_empty),
							Toast.LENGTH_SHORT).show();
				} else if (!(surnameOk = surnameOk && !surname.startsWith(" ")
						&& !surname.endsWith(" "))) {
					Toast.makeText(
							context,
							getResources()
									.getText(
											R.string.registration_surname_contains_spaces),
							Toast.LENGTH_SHORT).show();
				}

				if (!surnameOk) {
					editTextSurname
							.setBackgroundResource(R.drawable.txt_input_bordered_error);
				} else {
					editTextSurname
							.setBackgroundResource(R.drawable.txt_input_bordered);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		editTextMail.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				mail = editTextMail.getText().toString();
				mailOk = FinalFunctionsUtilities.isValidEmailAddress(mail);

				if (!mailOk) {
					editTextMail
							.setBackgroundResource(R.drawable.txt_input_bordered_error);
				} else {
					editTextMail
							.setBackgroundResource(R.drawable.txt_input_bordered);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});
	}

	@Override
	public void onTaskFinished(JSONObject res) {

		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}

		try {
			if (res.getString("success").equals("true")) {
				Intent returnIntent = new Intent();
				returnIntent.putExtra("id",
						Integer.parseInt(res.getString("id")));
				returnIntent.putExtra("name", res.getString("name"));
				returnIntent.putExtra("surname", res.getString("surname"));
				returnIntent.putExtra("mail", res.getString("email"));
				setResult(RESULT_OK, returnIntent);
				finish();
				Toast.makeText(context, R.string.add_friend_successful,
						Toast.LENGTH_LONG).show();
			} else {
				finish();
				Toast.makeText(context, R.string.registration_failed,
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.e(AddFriendActivity.class.getName(), e.toString());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
