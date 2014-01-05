package com.example.tabsontoronto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	//Value of this constant has to be updated based on the URL of the API.
	private static final String SEARCH_API_URL = "http://tabs-server.cloudapp.net/androidSearch/?query=%s&advancedsearch=Basic&fromDate=&toDate=&decisionBodyId=0&itemStatus=";
	
	public static final String SEARCH_JSON = "searchJson";
	public static final String SEARCH_INPUT = "searchInput";
	public static String SEARCH_TERM;

	final Context context = this;
	static String account = null;
	ProgressDialog searchProgress;
	Fragment currFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The home page's layout
		setContentView(R.layout.activity_main);
		initializeAutoComplete();

		// Initializing the FragmentManager
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();

		// Adding the MainFragment as the current fragment on display
		MainFragment mainfrag = new MainFragment();
		fragmentTransaction.add(R.id.fragment_container, mainfrag).commit();
		this.currFragment = mainfrag;

		// Check if this is the first time
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		boolean isFirstRun = prefs.getBoolean("FIRSTRUN", true);
		if (isFirstRun) {
			// Pop-up Dialog prompting user for an email.
			// The email will be used as an account for subscribing to keywords.

			LayoutInflater li = LayoutInflater.from(context);
			View firstRunPromptView = li.inflate(R.layout.first_run_prompt,
					null);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			alertDialogBuilder.setView(firstRunPromptView);
			final EditText userInput = (EditText) firstRunPromptView
					.findViewById(R.id.editTextSignupEmail);

			// set dialog message
			alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// get user input and set it to account
							account = userInput.getText().toString();
							Editor editor = prefs.edit();
							editor.putBoolean("FIRSTRUN", false);
							editor.putString("EMAILACCOUNT", account);
							editor.commit();
						}
					});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
			
		} else {
			account = prefs.getString("EMAILACCOUNT", "No Email Provided");
			Toast.makeText(MainActivity.this, 
					"Your signup email is " + account + ".",
		        	Toast.LENGTH_SHORT).show();
		}
	}

	private void initializeAutoComplete() {
		String[] dictionary = getResources().getStringArray(R.array.accepted_terms);
		AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search_input);
		actv.setThreshold(2);
		actv.setAdapter(new ArrayAdapter<String>(this, R.layout.search_input_suggestion, dictionary));
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		// Determines appropriate behaviour based on the UI element originating
		// the touch event.

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		
		switch (item.getItemId()) {
		
		case R.id.action_about_us:
			AboutUsFragment aboutusFrag = new AboutUsFragment();
			fragmentTransaction.remove(currFragment);
			fragmentTransaction.add(R.id.fragment_container, aboutusFrag)
					.commit();
			this.currFragment = aboutusFrag;
			return true;
			
		case R.id.action_contact:
			ContactFormFragment contactFrag = new ContactFormFragment();
			fragmentTransaction.remove(currFragment);
			fragmentTransaction.add(R.id.fragment_container, contactFrag)
					.commit();
			this.currFragment = contactFrag;
			return true;
			
		case R.id.action_privacy:
			PrivacyPolicyFragment privacyFrag = new PrivacyPolicyFragment();
			fragmentTransaction.remove(currFragment);
			fragmentTransaction.add(R.id.fragment_container, privacyFrag)
					.commit();
			this.currFragment = privacyFrag;
			return true;
			
		case R.id.action_settings:
			// OPEN SETTINGS
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	class RequestTask extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			searchProgress = new ProgressDialog(MainActivity.this);
			searchProgress.setTitle("Just one second...");
			searchProgress.setMessage("We're processing your search!");
			searchProgress.setCancelable(false);
			searchProgress.show();
			ImageButton searchSubmit = (ImageButton) findViewById(R.id.search_submit);
			searchSubmit.setEnabled(false);
		}

		@Override
		protected String doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// TODO Handle problems..
			} catch (IOException e) {
				// TODO Handle problems..
			}
			return responseString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ImageButton searchSubmit = (ImageButton) findViewById(R.id.search_submit);
			searchSubmit.setEnabled(true);
			searchProgress.dismiss();
			Intent intent = new Intent(MainActivity.this,
					SearchResultsActivity.class);
			intent.putExtra(SEARCH_JSON, result);
			intent.putExtra(SEARCH_INPUT, MainActivity.SEARCH_TERM);
			startActivity(intent);
		}
	}

	public void clickHandler(View v) {


		switch (v.getId()) {

		case R.id.search_submit:
			if (!hasInternetAccess()) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("Sorry...");
				dialog.setMessage("Your device is not connected to the internet. Please connect first and try again later.");
				dialog.setCancelable(true);
				dialog.setPositiveButton("Okay", null);
				dialog.show();
				break;
			}
			AutoCompleteTextView searchInput = (AutoCompleteTextView) findViewById(R.id.search_input);
			String input = searchInput.getText().toString();
			SEARCH_TERM = input;
			new RequestTask().execute(String.format(SEARCH_API_URL, stripQuery(input)));
			break;

		default:
			break;
		}
	}

	private String stripQuery(String input) {
		return input.replaceAll("\\s+","%20");
	}

	// Checks if the device is connected to the internet.
	private boolean hasInternetAccess() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}

		return false;
	}

}
