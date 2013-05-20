package it.unitn.vanguard.reminiscence;

import it.unitn.vanguard.reminiscence.QuestionPopUpHandler.QuestionPopUp;
import it.unitn.vanguard.reminiscence.asynctasks.DeleteStoryTask;
import it.unitn.vanguard.reminiscence.asynctasks.GetPublicStoriesTask;
import it.unitn.vanguard.reminiscence.asynctasks.GetStoryCoverTask;
import it.unitn.vanguard.reminiscence.asynctasks.LogoutTask;
import it.unitn.vanguard.reminiscence.frags.StoryFragment;
import it.unitn.vanguard.reminiscence.interfaces.OnGetStoryTask;
import it.unitn.vanguard.reminiscence.interfaces.OnTaskFinished;
import it.unitn.vanguard.reminiscence.utils.Constants;
import it.unitn.vanguard.reminiscence.utils.FinalFunctionsUtilities;
import it.unitn.vanguard.reminiscence.utils.Story;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import eu.giovannidefrancesco.DroidTimeline.view.TimeLineView;
import eu.giovannidefrancesco.DroidTimeline.view.YearView;

public class ViewStoriesActivity extends BaseActivity implements
		OnTaskFinished, QuestionPopUp, OnGetStoryTask {

	private static final String SAVE_INDEX = "saved_index";
	public static final int ADD_STORY_CODE = 100;

	private Context context;

	// private ViewPager mViewPager;
	private GridView mCards;
	private TimeLineView mTimeLine;
	private ProgressDialog dialog;
	private ActionBar actionBar;

	private TextView mQuestionTv;
	private ImageView mCloseQuestionImgV;
	private StoriesAdapter mStoriesAdapter;
	private int selectedIndex;
	// private YearView selected;
	private int startYear;
	private int requestYear;

	private YearView lastSelected;
	
	private Bundle bundle;

	@Override
	public void onCreate(Bundle arg0) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(arg0);
		setContentView(R.layout.activity_viewstories);
		context = ViewStoriesActivity.this;
		bundle = arg0;

		String language = FinalFunctionsUtilities.getSharedPreferences(
				"language", context);
		FinalFunctionsUtilities.switchLanguage(new Locale(language), context);

		mCards = (GridView) findViewById(R.id.viewstroies_cards_gw);
		mTimeLine = (TimeLineView) findViewById(R.id.viewstories_tlv);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getApplicationContext(), R.array.stories_dropdown,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		mStoriesAdapter = new StoriesAdapter();
		mCards.setAdapter(mStoriesAdapter);

		// E' per avere lo 0 alla fine degli anni.(per avere l'intera decade, praticamente)
		String year = FinalFunctionsUtilities.getSharedPreferences(Constants.YEAR_KEY, this);
		year = year.substring(0, year.length() - 1);
		
		// HOTFIX
		try {
			requestYear = Integer.parseInt(year + '0');
		} catch (Exception ex) {
			requestYear = 1920;
		}
		
		startYear = requestYear;
		mTimeLine.setStartYear(startYear);
		
		if (bundle != null) {
			selectedIndex = bundle.getInt(SAVE_INDEX);
			FinalFunctionsUtilities.stories.clear();
			mStoriesAdapter.notifyDataSetChanged();
		} else {
			selectedIndex = 0;
		}

		
		Log.e("setting the selected year", "oh yeah");
		YearView selected = (YearView) mTimeLine.getAdapter().getView(selectedIndex, null, mTimeLine);
		selected.setBackgroundColor(getResources().getColor(R.color.pomegranate));
		
		requestYear = selected.getYear();
		lastSelected = selected;
		
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(adapter,
				new ActionBar.OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						switch (itemPosition) {
						case 0:
							if (FinalFunctionsUtilities.isDeviceConnected(context)) {
								FinalFunctionsUtilities.stories.clear();
								Log.e("asdasasd","caso 0");
								
								YearView selected = (YearView) mTimeLine.getAdapter().getView(selectedIndex, null, mTimeLine);
								selected.setBackgroundColor(getResources().getColor(R.color.pomegranate));
								
								new GetPublicStoriesTask(ViewStoriesActivity.this, /* GetStoriesTask */
										requestYear).execute();
								
								FinalFunctionsUtilities.setSharedPreferences(Constants.ACTIVE_STORIES, Constants.PRIVATE_STORIES, context);
								
							} else {
								Toast.makeText(context,
										R.string.connection_fail,
										Toast.LENGTH_LONG).show();
							}
							break;
						case 1:
							if (FinalFunctionsUtilities
									.isDeviceConnected(context)) {
								FinalFunctionsUtilities.stories.clear();
								Log.e("asdasasd","caso 1");
								
								YearView selected = (YearView) mTimeLine.getAdapter().getView(selectedIndex, null, mTimeLine);
								selected.setBackgroundColor(getResources().getColor(R.color.pomegranate));
								
								new GetPublicStoriesTask(
										ViewStoriesActivity.this, requestYear)
										.execute();
								
								FinalFunctionsUtilities.setSharedPreferences(Constants.ACTIVE_STORIES, Constants.PUBLIC_STORIES, context);
								
							} else {
								Toast.makeText(context,
										R.string.connection_fail,
										Toast.LENGTH_LONG).show();
							}
							break;
						}
						return true;
					}
				});
		
		setListeners();
		initializePopUps();
	}

	private void initializeStoryList() {

		/*
		 * doppie storie perche' doppia richiesta, richiesta che avviene gia'
		 * con l'inizializzazione della barra privato/pubblico
		 * 
		 * // Comincia a chiedere al server le storie if
		 * (FinalFunctionsUtilities.isDeviceConnected(context)) { new
		 * GetStoriesTask(this, requestYear).execute(); } else {
		 * Toast.makeText(context, R.string.connection_fail, Toast.LENGTH_LONG)
		 * .show(); }
		 */
	}

	private void initializePopUps() {
		Bundle b = new Bundle();
		b.putString(QuestionPopUpHandler.QUESTION_PASSED_KEY,
				"Sei mai andato in crociera?");
		Message msg = new Message();
		msg.setData(b);
		new QuestionPopUpHandler(this).sendMessageDelayed(msg,
				Constants.QUESTION_INTERVAL);
	}

	private void setListeners() {
		
		mTimeLine.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				((YearView) arg1).setBackgroundColor(getResources().getColor(
						R.color.pomegranate));

				lastSelected.setBackgroundColor(getResources().getColor(
						R.color.red_background_dark));
				lastSelected = (YearView) arg1;

				// aggiorno gli indici
				selectedIndex = arg2;
				requestYear = ((YearView) arg1).getYear();
				// tolgo le vecchie e chiedo le nuove
				FinalFunctionsUtilities.stories.clear();
				mStoriesAdapter.notifyDataSetChanged();
				
				if(FinalFunctionsUtilities.isDeviceConnected(context)) {
					Log.e("Request","stories");
					new GetPublicStoriesTask(ViewStoriesActivity.this, requestYear).execute();
				}
				else {
					if(dialog!=null && dialog.isShowing()) { 	dialog.dismiss(); }
					Toast.makeText(context, getResources().getString(R.string.connection_fail), Toast.LENGTH_LONG).show();
				}
			}
		});

		mQuestionTv = (TextView) findViewById(R.id.viewtories_addstory_hint);
		mQuestionTv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				OnHide();
			}
		});

		mCloseQuestionImgV = (ImageView) findViewById(R.id.viewstories_addstory_hint_close);
		mCloseQuestionImgV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				OnHide();
			}
		});
		mLogout = (TextView) findViewById(R.id.hiddebmenu_logout_tv);
		mLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						ViewStoriesActivity.this);

				builder.setMessage(R.string.exit_message)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialogInterface,
											int id) {

										if (FinalFunctionsUtilities
												.isDeviceConnected(context)) {
											dialog = new ProgressDialog(
													ViewStoriesActivity.this);
											dialog.setTitle(getResources()
													.getString(R.string.please));
											dialog.setMessage(getResources()
													.getString(R.string.wait));
											dialog.setCancelable(false);
											dialog.show();

											String email = FinalFunctionsUtilities
													.getSharedPreferences(
															Constants.MAIL_KEY,
															ViewStoriesActivity.this);
											String password = FinalFunctionsUtilities
													.getSharedPreferences(
															Constants.PASSWORD_KEY,
															ViewStoriesActivity.this);

											new LogoutTask(
													ViewStoriesActivity.this)
													.execute(email, password);
										} else {
											Toast.makeText(
													context,
													getResources()
															.getString(
																	R.string.connection_fail),
													Toast.LENGTH_LONG).show();
										}
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

									}
								});

				AlertDialog alert = builder.create();
				alert.show();
				((TextView) alert.findViewById(android.R.id.message))
						.setGravity(Gravity.CENTER);
				((Button) alert.getButton(AlertDialog.BUTTON_POSITIVE))
						.setBackgroundResource(R.drawable.bottone_logout);
				((Button) alert.getButton(AlertDialog.BUTTON_POSITIVE))
						.setTextColor(Color.WHITE);

			}
		});
	}

	private class StoriesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return FinalFunctionsUtilities.stories.size();
		}

		@Override
		public Object getItem(int arg0) {
			return FinalFunctionsUtilities.stories.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			View v = getLayoutInflater().inflate(R.layout.card_story, arg2,
					false);
			Story story = FinalFunctionsUtilities.stories.get(arg0);
			ImageView back = (ImageView) v.findViewById(R.id.cardstory_img);
			TextView title = (TextView) v.findViewById(R.id.cardstory_title);
			TextView desc = (TextView) v.findViewById(R.id.cardstory_desc);
			TextView year = (TextView) v.findViewById(R.id.yearStoryCard);
			if (story != null) {
				title.setText(story.getTitle());
				desc.setText(story.getDesc());
				year.setText(String.valueOf(story.getAnno()));
				if (story.getBackground() != null) {
					back.setImageBitmap(FinalFunctionsUtilities.stories.get(
							arg0).getBackground());
				}
				else Log.e("Story" + story.getId(),"Story" + story.getId() + " has bitmap null");
			}
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View clicked) {
					Story story = FinalFunctionsUtilities.stories.get(arg0);
					Log.e("Showing story:", "id: " + story.getId());
					StoryFragment sf = StoryFragment.newIstance(
							story.getTitle(), story.getDesc(),
							"" + story.getAnno(), story.getId());
					sf.show(getSupportFragmentManager(), "visualized");
				}
			});
			
			v.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View longClicked) {
					if(FinalFunctionsUtilities.stories.get(arg0).getId().equals("1")){
						final int pos = arg0;
						
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
	
						String delStory = "";
						delStory += getResources().getString(R.string.deleteStoryPopupMessage1);
						delStory += "\n\n\"" + FinalFunctionsUtilities.stories.get(pos).getTitle() + "\"\n\n";
						delStory += getResources().getString(R.string.deleteStoryPopupMessage2);
						
						builder.setMessage(delStory)
								.setPositiveButton(R.string.yes,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialogInterface,
													int id) {
												
												if(FinalFunctionsUtilities.isDeviceConnected(context)) {
													dialog = new ProgressDialog(
															context);
													dialog.setTitle(getResources()
															.getString(R.string.please));
													dialog.setMessage(getResources()
															.getString(R.string.wait));
													dialog.setCancelable(false);
													dialog.show();	
													deleteStory(arg0);																					
													
												}
												else {
													Toast.makeText(context,
															getResources().getString(R.string.connection_fail),
															Toast.LENGTH_LONG).show();
												}
											}
										})
								.setNegativeButton(R.string.no,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog, int id) {
	
											}
										});
	
						AlertDialog alert = builder.create();
						alert.show();
						((TextView) alert.findViewById(android.R.id.message))
								.setGravity(Gravity.CENTER);
						((Button) alert.getButton(AlertDialog.BUTTON_POSITIVE))
								.setBackgroundResource(R.drawable.bottone_logout);
						((Button) alert.getButton(AlertDialog.BUTTON_POSITIVE))
								.setTextColor(Color.WHITE);	
					}
					return true;					
				}
			});
			return v;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.timeline, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_story:
			Intent i = new Intent(this, EmptyStoryActivity.class);
			i.putExtra(EmptyStoryActivity.YEAR_PASSED_KEY, requestYear);
			startActivityForResult(i, ADD_STORY_CODE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTaskFinished(JSONObject res) {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}

		try {
			if (res.getString("success").equals("true")) {
				if(res.getString("Operation").equals("Logout")){
					Toast.makeText(context,
							getResources().getString(R.string.logout_success),
							Toast.LENGTH_LONG).show();
	
					FinalFunctionsUtilities.clearSharedPreferences(context);
					startActivity(new Intent(ViewStoriesActivity.this,
							LoginActivity.class));
					this.finish();
				}
				else if (res.get("Operation").equals("delStory")){
					Toast.makeText(context,
							getResources().getString(R.string.deleteStorySucces),
							Toast.LENGTH_LONG).show();	
					mStoriesAdapter.notifyDataSetChanged();
				}
				else if(res.getString("Operation").equals("GetStoryCover") &&
						!res.getString("numImages").equals("0")) {
					// Inserisco la cover della storia..
					String id = res.getString("story_id");
					Story s = null;
					for(int i = 0; i < FinalFunctionsUtilities.stories.size(); i++) {
						if(id.equals(FinalFunctionsUtilities.stories.get(i).getId())) {
							s = FinalFunctionsUtilities.stories.get(i);
						}
					}
					byte[] decodedString = Base64.decode(res.getString("cover"), Base64.DEFAULT);
					Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
					s.setBackground(bitmap);
					mStoriesAdapter.notifyDataSetChanged();
				}
			} else {
				if(res.getString("Operation").equals("Logout")){
					Toast.makeText(this,
							getResources().getString(R.string.logout_failed),
							Toast.LENGTH_LONG).show();
				}
				else if (res.get("Operation").equals("DelStory")){
					Toast.makeText(context,
							getResources().getString(R.string.deleteStoryFail),
							Toast.LENGTH_LONG).show();					
				}
			}
		} catch (JSONException e) {
			Log.e(LoginActivity.class.getName(), e.toString());
		}
	}

	@Override
	public void OnShow(String question) {
		togglePopup(true);
		mQuestionTv.setText(question);
	}

	@Override
	public void OnHide() {
		togglePopup(false);
	}

	private void togglePopup(boolean visibility) {
		if (!visibility) {
			mQuestionTv.setVisibility(View.GONE);
			mCloseQuestionImgV.setVisibility(View.GONE);
		} else {
			mQuestionTv.setVisibility(View.VISIBLE);
			mCloseQuestionImgV.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void OnStart() {
		setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public void OnProgress() {
		mStoriesAdapter.notifyDataSetChanged();
	}

	@Override
	public void OnFinish(Boolean result) {
		setProgressBarIndeterminateVisibility(false);
		if ((requestYear == startYear) && (FinalFunctionsUtilities.getSharedPreferences(Constants.ACTIVE_STORIES, context).equals(Constants.PRIVATE_STORIES))) {
			addBornStory();
		}
		View no_res = findViewById(R.id.no_result_tv);
		if (FinalFunctionsUtilities.stories.isEmpty()) {
			no_res.setVisibility(View.VISIBLE);
			mCards.setVisibility(View.INVISIBLE);
		} else {
			no_res.setVisibility(View.INVISIBLE);
			mCards.setVisibility(View.VISIBLE);
		}
		// TEST
		String token = FinalFunctionsUtilities.getSharedPreferences(Constants.TOKEN_KEY, context);
		if(FinalFunctionsUtilities.isDeviceConnected(this)) {
			for(int i = 0; i < FinalFunctionsUtilities.stories.size(); i++) {
				try {
					Log.e("GetStoryCover", "task number " + i + "");
					new GetStoryCoverTask(ViewStoriesActivity.this, ViewStoriesActivity.this).execute(token, FinalFunctionsUtilities.stories.get(i).getId() + "");
				}
				catch (Exception e) {
					Log.e("con size= " + FinalFunctionsUtilities.stories.size(), i + ": " +e.toString());
				}
			}
		}
		mStoriesAdapter.notifyDataSetChanged();
		// updateSelected(true);
		OnProgress();
	}

	private void addBornStory() {
		String title = getString(R.string.born_title);
		String desc = String.format(getString(R.string.born),
				FinalFunctionsUtilities.getSharedPreferences(
						Constants.LOUGO_DI_NASCITA_PREFERENCES_KEY,
						ViewStoriesActivity.this));
		Bitmap img = BitmapFactory.decodeResource(getResources(),
				R.drawable.baby);
		Story s = new Story(startYear, title, desc, "");
		s.setBackground(img);
		FinalFunctionsUtilities.stories.addFirst(s);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SAVE_INDEX, selectedIndex);
	}

	private void updateSelected(boolean selected) {
		YearView v = (YearView) mTimeLine.getChildAt(selectedIndex);
		if (v == null)
			v = (YearView) mTimeLine.getAdapter().getView(selectedIndex, null,
					null);
		if (selected)
			v.setBackgroundColor(getResources().getColor(R.color.pomegranate));
		else
			v.setBackgroundColor(getResources().getColor(
					R.color.red_background_dark));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADD_STORY_CODE) {
			if (resultCode == RESULT_OK) {
				if (data.hasExtra(EmptyStoryActivity.YEAR_PASSED_KEY)) {
					int year = Integer
							.parseInt(data
									.getStringExtra(EmptyStoryActivity.YEAR_PASSED_KEY));
					if (year >= requestYear && year <= requestYear + 10) {
						Story s = new Story(
								year,
								data.getStringExtra(EmptyStoryActivity.TITLE_PASSED_KEY),
								data.getStringExtra(EmptyStoryActivity.DESC_PASSED_KEY),
								data.getStringExtra(EmptyStoryActivity.ID_PASSED_KEY));
						FinalFunctionsUtilities.stories.add(s);
						mStoriesAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	}
	
	public void storyDeleted(int position) {
		FinalFunctionsUtilities.stories.remove(position);
	}

	private void deleteStory(int position) {
		Story story = FinalFunctionsUtilities.stories.get(position);
		if(dialog == null) {
			dialog = new ProgressDialog(context);
			dialog.setTitle(getResources().getString(R.string.please));
			dialog.setMessage(getResources().getString(R.string.wait));
			dialog.setCancelable(false);
			dialog.show();
		}
		if (FinalFunctionsUtilities.isDeviceConnected(context)) {
			new DeleteStoryTask(this, position).execute(story.getId());
		}
	}
}
