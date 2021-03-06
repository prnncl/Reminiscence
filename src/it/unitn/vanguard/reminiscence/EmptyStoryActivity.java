package it.unitn.vanguard.reminiscence;

import it.unitn.vanguard.reminiscence.asynctasks.AddStoryTask;
import it.unitn.vanguard.reminiscence.interfaces.OnTaskFinished;
import it.unitn.vanguard.reminiscence.utils.Constants;
import it.unitn.vanguard.reminiscence.utils.FinalFunctionsUtilities;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import eu.giovannidefrancesco.DroidTimeline.widget.HorizontalListView;

public class EmptyStoryActivity extends Activity implements OnTaskFinished {

	private Context context;

	public static final String YEAR_PASSED_KEY = "emptyStoryYear";
	public static final String TITLE_PASSED_KEY = "newstorytitle";
	public static final String DESC_PASSED_KEY = "newstorydesc";
	public static final String ID_PASSED_KEY = "newstoryid";
	public static final String IMG_PASSED_KEY = "newstoryimg";

	private EditText mTitleEt;
	private EditText mDescriptionEt;
	private EditText mYearEt;
	private Button mAddBtn;
	private ImageView mMediaButton;
	private HorizontalListView mMedias;
	private ArrayList<ImageView> imgs;
	private ArrayList<String> toUpload;
	private ImageViewAdapter mAdapter;
	private int idStoria;
	private Uri coverUri = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		context = EmptyStoryActivity.this;

		String language = FinalFunctionsUtilities.getSharedPreferences(
				Constants.LANGUAGE_KEY, context);
		FinalFunctionsUtilities.switchLanguage(new Locale(language), context);
		setContentView(R.layout.fragment_emptystory);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		initializeComponent();
		toUpload = new ArrayList<String>();
		initializeListeners();
	}

	private void initializeComponent() {

		mTitleEt = (EditText) findViewById(R.id.emptystory_title_et);
		mDescriptionEt = (EditText) findViewById(R.id.emptystory_desc_et);
		mAddBtn = (Button) findViewById(R.id.emptystory_add_btn);
		mMediaButton = (ImageView) findViewById(R.id.emptystory_addmedia_imv);
		mYearEt = (EditText) findViewById(R.id.emptystory_year_et);
		mYearEt.setText("" + getIntent().getExtras().getInt(YEAR_PASSED_KEY));

		mMedias = (HorizontalListView) findViewById(R.id.emptystories_imgs_hlv);
		imgs = new ArrayList<ImageView>();
		mAdapter = new ImageViewAdapter();
		mMedias.setAdapter(mAdapter);
	}

	private void initializeListeners() {

		mYearEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (!checkYearInput(s.toString())) {
					mYearEt.setBackgroundResource(R.drawable.txt_input_bordered_error);
				} else {
					mYearEt.setBackgroundResource(R.drawable.txt_input_bordered);
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

		mAddBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String year = mYearEt.getText().toString();
				if (FinalFunctionsUtilities.isDeviceConnected(context)) {
					if (checkYearInput(year)) {
						try {
							new AddStoryTask(EmptyStoryActivity.this).execute(
									year, mDescriptionEt.getText().toString(),
									mTitleEt.getText().toString());
						} catch (Exception e) {
							Log.e(EmptyStoryActivity.class.getName(),
									e.toString());
						}
					} else {
						showToast(R.string.story_year_broken);
					}
				} else {
					showToast(R.string.connection_fail);
				}
			}
		});

		mMediaButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, 1);
			}
		});
	}

	@Override
	public void onTaskFinished(JSONObject res) {

		try {
			Intent out = new Intent();
			if (res.getString("success").equals("true")) {
				if (res.getString("Operation").equals("addStory")) {
					idStoria = res.getInt("idadded");
					if (toUpload.size() > 0)
						sendPhotos();
					out.putExtra(TITLE_PASSED_KEY, mTitleEt.getText()
							.toString());
					out.putExtra(DESC_PASSED_KEY, mDescriptionEt.getText()
							.toString());
					out.putExtra(YEAR_PASSED_KEY, mYearEt.getText().toString());
					out.putExtra(ID_PASSED_KEY, idStoria + "");
					if (coverUri != null) {
						out.putExtra(IMG_PASSED_KEY, coverUri.toString());
					} else {
						out.putExtra(IMG_PASSED_KEY, "");
					}
				}
				this.setResult(RESULT_OK, out);
				finish();
			} else {
				this.setResult(RESULT_CANCELED, out);
			}
		} catch (JSONException e) {
			showToast(false);
		}
	}

	private void sendPhotos() {
		Intent i = new Intent(this, PhotoUploaderService.class);
		i.putExtra(PhotoUploaderService.ID_STORY_KEY, idStoria + "");
		String[] base = new String[toUpload.size()];
		toUpload.toArray(base);
		i.putExtra(PhotoUploaderService.PATHS_KEY, base);
		startService(i);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Uri chosenImageUri = data.getData();
			Bitmap mBitmap = null;
			try {

				mBitmap = Media.getBitmap(getContentResolver(), chosenImageUri);
				Bitmap bm = mBitmap.copy(Config.RGB_565, false);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
				bm.recycle();
				byte[] b = baos.toByteArray();

				baos.close();
				baos = null;

				String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs
						.add(new BasicNameValuePair("image", encodedImage));

				try {
					if (coverUri == null) {
						coverUri = chosenImageUri;
					}
					ImageView img = new ImageView(context);
					img.setAdjustViewBounds(true);
					img.setImageBitmap(mBitmap);
					imgs.add(img);
					toUpload.add(encodedImage);
					mAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					Log.e(EmptyStoryActivity.class.getName(), e.toString());
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				Log.e(EmptyStoryActivity.class.getName(), e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(EmptyStoryActivity.class.getName(), e.toString());
				e.printStackTrace();
			}
		}
	}

	private boolean checkYearInput(String year) {
		int bornyear = Integer.parseInt(FinalFunctionsUtilities
				.getSharedPreferences(Constants.YEAR_KEY, context));
		int actualyear = Calendar.getInstance().get(Calendar.YEAR);

		try {
			int iyear = Integer.parseInt(year);
			return (iyear >= bornyear && iyear <= actualyear);
		} catch (Exception e) {
			Log.e(EmptyStoryActivity.class.getName(), e.toString());
			return false;
		}
	}

	private void showToast(boolean success) {
		Toast.makeText(
				context,
				getResources().getString(
						success ? R.string.story_add_ok
								: R.string.story_add_fail), Toast.LENGTH_SHORT)
				.show();
	}

	private void showToast(int resource) {
		Toast.makeText(this.getApplicationContext(),
				getResources().getString(resource), Toast.LENGTH_SHORT).show();
	}

	private class ImageViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return imgs.size();
		}

		@Override
		public Object getItem(int arg0) {
			return getView(arg0, null, null);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return imgs.get(position);
		}
	}
}