package me.veereshbeeram.veereshmukotestapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rdio.android.api.OAuth1WebViewActivity;
import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioApiCallback;
import com.rdio.android.api.RdioListener;

import android.support.v7.app.ActionBarActivity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class RdioPlayback extends ActionBarActivity implements RdioListener, OnClickListener {
	private static final String TAG = "RdioPlayback";
	private static MediaPlayer player;
	private static Rdio rdio;


	private static final String appKey = "your-app-key";
	private static final String appSecret = "your-secret-key";

	private static String accessToken = null;
	private static String accessTokenSecret = null;

	private static final String PREF_ACCESSTOKEN = "prefs.accesstoken";
	private static final String PREF_ACCESSTOKENSECRET = "prefs.accesstokensecret";

	private static String searchQuery;
	private static ImageButton play_pause;
	private static ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent searchIntent = getIntent();
		searchQuery = searchIntent
				.getStringExtra("me.veereshbeeram.veereshmukotestapp.searchstring");
		Log.e(TAG, searchQuery);
		setContentView(R.layout.activity_rdio_playback);
		progressBar = (ProgressBar) findViewById(R.id.song_loading);
		play_pause = (ImageButton) findViewById(R.id.play_pause_image);
		play_pause.setOnClickListener(this);
		if (rdio == null) {
			SharedPreferences settings = getPreferences(MODE_PRIVATE);
			accessToken = settings.getString(PREF_ACCESSTOKEN, null);
			accessTokenSecret = settings
					.getString(PREF_ACCESSTOKENSECRET, null);

			rdio = new Rdio(appKey, appSecret, accessToken, accessTokenSecret,
					this, this);

			if (accessToken == null || accessTokenSecret == null) {
				// If either one is null, reset both of them
				accessToken = accessTokenSecret = null;
				Intent myIntent = new Intent(RdioPlayback.this,
						OAuth1WebViewActivity.class);
				myIntent.putExtra(OAuth1WebViewActivity.EXTRA_CONSUMER_KEY,
						appKey);
				myIntent.putExtra(OAuth1WebViewActivity.EXTRA_CONSUMER_SECRET,
						appSecret);
				RdioPlayback.this.startActivityForResult(myIntent, 1);

			} else {
				Log.d(TAG, "Found cached credentials:");
				Log.d(TAG, "Access token: " + accessToken);
				Log.d(TAG, "Access token secret: " + accessTokenSecret);
				rdio.prepareForPlayback();
			}
		}else{
			rdio.prepareForPlayback();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rdio_playback, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		/*
		if(player == null){
			Log.d(TAG,"in pause player null");
		}
		if(!player.isPlaying()){
			Log.d(TAG,"in pause player not playing");
		}
		*/
		if(player != null && player.isPlaying()){
			//Log.d(TAG,"in pause");
			player.pause();
			play_pause.setImageResource(R.drawable.ic_action_play_over_video);
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		/*
		if(player == null){
			Log.d(TAG,"in stop player null");
		}
		if(!player.isPlaying()){
			Log.d(TAG,"in stop player not playing");
		}
		*/
		if(player != null && player.isPlaying()){
			// Log.d(TAG,"in stop");
			player.pause();
			play_pause.setImageResource(R.drawable.ic_action_play_over_video);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		/*
		if(player == null){
			Log.d(TAG,"in destroy player null");
		}
		if(!player.isPlaying()){
			Log.d(TAG,"in destroy player not playing");
		}
		*/
		if(player != null && player.isPlaying()){
			//Log.d(TAG,"in destroy");
			player.stop();
		}
		if(player != null){
			player.reset();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(player !=null && !player.isPlaying()){
			player.start();
			play_pause.setImageResource(R.drawable.ic_action_pause_over_video);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rdio.android.api.RdioListener#onRdioAuthorised(java.lang.String,
	 * java.lang.String) RdioListener Interface methods.
	 */
	@Override
	public void onRdioAuthorised(String arg0, String arg1) {
		//Log.d(TAG, "RDIO authorized");
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString(PREF_ACCESSTOKEN, accessToken);
		editor.putString(PREF_ACCESSTOKENSECRET, accessTokenSecret);
		editor.commit();

	}

	@Override
	public void onRdioReadyForPlayback() {
		if (accessToken != null && accessTokenSecret != null) {
			//Log.d(TAG, "ready for playback with app");

			AsyncTask<String, Void, Boolean> searchPlay = new AsyncTask<String, Void, Boolean>() {
				
				@Override
				protected Boolean doInBackground(String... params) {
					String localSearchQuery = params[0];
					List<NameValuePair> args = new LinkedList<NameValuePair>();
					args.add(new BasicNameValuePair("query", localSearchQuery));
					args.add(new BasicNameValuePair("types", "Track"));
					args.add(new BasicNameValuePair("count", "2"));
					rdio.apiCall("search", args, new RdioApiCallback() {
						@Override
						public void onApiSuccess(JSONObject result) {
							Log.v(TAG, result.toString());
							try {
								JSONArray tracks = result.getJSONObject("result")
										.getJSONArray("results");
								if(tracks.length() > 0){
									JSONObject track = tracks.getJSONObject(0);
									player = rdio.getPlayerForTrack(track.getString("key"),
										null, true);
									player.prepare();
									player.start();
								
									progressBar.setVisibility(View.GONE);
									play_pause.setVisibility(View.VISIBLE);
								}else{
									progressBar.setVisibility(View.GONE);
									Toast.makeText(getApplicationContext(), "Sorry! We could not find any matches. Head back & try again!", Toast.LENGTH_LONG).show();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onApiFailure(String arg0, Exception arg1) {
							Log.e(TAG, "Failed API call" + arg1.toString());
						}
					});							
					return true;
				}
			};
			String[] queries = new String[] {searchQuery};
			searchPlay.execute(queries);
		} else {
		}
	}

	@Override
	public void onRdioUserPlayingElsewhere() {
		Log.w(TAG, "Tell the user that playback is stopping.");
		player.stop();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Log.v(TAG, "Login success");
				if (data != null) {
					accessToken = data.getStringExtra("token");
					accessTokenSecret = data.getStringExtra("tokenSecret");
					onRdioAuthorised(accessToken, accessTokenSecret);
					rdio.setTokenAndSecret(accessToken, accessTokenSecret);
				}
			} else if (resultCode == RESULT_CANCELED) {
				if (data != null) {
					String errorCode = data
							.getStringExtra(OAuth1WebViewActivity.EXTRA_ERROR_CODE);
					String errorDescription = data
							.getStringExtra(OAuth1WebViewActivity.EXTRA_ERROR_DESCRIPTION);
					Log.v(TAG, "ERROR: " + errorCode + " - " + errorDescription);
				}
				accessToken = null;
				accessTokenSecret = null;
			}
			rdio.prepareForPlayback();
		}
	}

	@Override
	public void onClick(View v) {
		if(player != null && player.isPlaying()){
			player.pause();
			play_pause.setImageResource(R.drawable.ic_action_play_over_video);
		}else if(player != null){
			player.start();
			play_pause.setImageResource(R.drawable.ic_action_pause_over_video);
		}
		
	}
}
