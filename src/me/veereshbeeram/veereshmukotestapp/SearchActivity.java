package me.veereshbeeram.veereshmukotestapp;

import java.util.ArrayList;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SearchActivity extends ActionBarActivity implements
		android.view.View.OnClickListener {
	public static String TAG = "VeereshMukoTest";
	private boolean isConnected = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		ImageButton micButton = (ImageButton) findViewById(R.id.mic_image_button);
		final SpeechRecognizer sr = SpeechRecognizer
				.createSpeechRecognizer(this.getApplicationContext());
		Button searchButton = (Button) findViewById(R.id.search_button);
		
		boolean isSpeech = SpeechRecognizer.isRecognitionAvailable(this.getApplicationContext());
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo currentNetworkInfo = connectivityManager.getActiveNetworkInfo();
		isConnected = currentNetworkInfo != null && currentNetworkInfo.isConnected();
		searchButton.setOnClickListener(this);
		if (isSpeech) {
			sr.setRecognitionListener(new RecognitionListener() {

				@Override
				public void onRmsChanged(float rmsdB) {
				}

				@Override
				public void onResults(Bundle results) {
					ArrayList<String> sResults = results
							.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
					float[] confidenceResults = results
							.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
					for (int i = 0; i < sResults.size(); i++) {
						Log.d(TAG, sResults.get(i));
						Log.d(TAG, String.valueOf(confidenceResults[i]));
					}
					EditText et = (EditText) findViewById(R.id.search_input);
					et.setText(sResults.get(0));

				}

				@Override
				public void onReadyForSpeech(Bundle params) {
				}

				@Override
				public void onPartialResults(Bundle partialResults) {
				}

				@Override
				public void onEvent(int eventType, Bundle params) {
				}

				@Override
				public void onError(int error) {
				}

				@Override
				public void onEndOfSpeech() {
				}

				@Override
				public void onBufferReceived(byte[] buffer) {
				}

				@Override
				public void onBeginningOfSpeech() {
				}
			});
			micButton.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
						Log.d(TAG, "DOWN EVENT");
						sr.startListening(getIntent());
					} else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
						Log.d(TAG, "UP EVENT");
						sr.stopListening();
					}
					return true;
				}
			});
		}
		if(!isConnected){
			Toast.makeText(getApplicationContext(), R.string.no_connection_toast, Toast.LENGTH_SHORT).show();
		}else if(!isSpeech) {
			Toast.makeText(getApplicationContext(), R.string.no_recognition_toast, Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
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
	public void onClick(View v) {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo currentNetworkInfo = connectivityManager.getActiveNetworkInfo();
		isConnected = currentNetworkInfo != null && currentNetworkInfo.isConnected();
		Intent resultIntent = new Intent(this, RdioPlayback.class);
		EditText et = (EditText) findViewById(R.id.search_input);
		String inputData = et.getText().toString();
		Log.e(TAG, inputData);
		if(inputData == null || inputData.isEmpty()){
			Toast.makeText(getApplicationContext(), R.string.empty_input_query_error, Toast.LENGTH_SHORT).show();
		}else if(!isConnected){
			Toast.makeText(getApplicationContext(), R.string.no_connection_toast, Toast.LENGTH_SHORT).show();
		}else{
		resultIntent.putExtra(
				"me.veereshbeeram.veereshmukotestapp.searchstring", inputData);
		startActivity(resultIntent);
		}
	}
}
