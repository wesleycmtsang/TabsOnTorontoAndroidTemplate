package com.example.tabsontoronto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchResultsActivity extends Activity {

	// Value of constant should be changed depending on the URL of the server.
	private static final String API_SUBSCRIBE = "http://tabs-server.cloudapp.net/androidRegister/?query=%s&advancedsearch=Basic&fromDate=&toDate=&decisionBodyId=0&itemStatus=&email=%s";
	private static final String API_ADVANCED_SEARCH = "http://tabs-server.cloudapp.net/androidSearch/?query=%s&advancedsearch=Basic&fromDate=&toDate=&decisionBodyId=0&itemStatus=";
	
	final Context context = this;
	public static final String ADV_SEARCH_JSON = "advSearchJson";
	public static final String ADV_SEARCH_INPUT = "advSearchInput";
	
	Menu actionMenu;
	static boolean advancedFilter;
	static String searchTerm;
	static SharedPreferences prefs;
	
	static String status_value;
	static String committee_value;
	
	ProgressDialog filterProgress;
	String json;
	
	Map<String, String> status_map;
	Map<String, String> committee_map;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_results);
		
		status_map = new HashMap<String, String>();
		committee_map = new HashMap<String, String>();
		
		String[] status_list = getResources().getStringArray(R.array.status_filter_list);
		String[] status_val_list = getResources().getStringArray(R.array.status_val_list);
		for (int i = 0; i < status_list.length; i++) {
			status_map.put(status_list[i], status_val_list[i]);
		}
		
		String[] committee_list = getResources().getStringArray(R.array.committee_filter_list);
		String[] committee_val_list = getResources().getStringArray(R.array.committee_val_list);
		for (int i = 0; i < committee_list.length; i++) {
		    committee_map.put(committee_list[i], committee_val_list[i]);
		}
		
		committee_value = "";
		status_value = "";
		
		Intent intent = getIntent();
		
		//Attempt to retrieve value stored as MainActivity.SEARCH_INPUT
		searchTerm = intent.getStringExtra(MainActivity.SEARCH_INPUT);
		
		if (searchTerm == null) {
			//This means this is an Advanced Filters Results page
			searchTerm = intent.getStringExtra(ADV_SEARCH_INPUT);
			getActionBar().setTitle(searchTerm);
			json = intent.getStringExtra(ADV_SEARCH_JSON);
			search(searchTerm);
		} else {
			//This means this is a Basic Search Results page
			String query = intent.getStringExtra(MainActivity.SEARCH_INPUT);
			getActionBar().setTitle(query);
			json = intent.getStringExtra(MainActivity.SEARCH_JSON);
			search(query);
		}
	}

	private void search(String query) {
		
		searchTerm = query;

		final ArrayList<SearchResult> results = resultsAdapterHandler(json);

		if (results.size() == 0) {
			SearchResult noRes = new SearchResult(
					"No results were found for your search.", "", "", "", "");
			results.add(noRes);
		}

		SearchResultsAdapter adapter = new SearchResultsAdapter(this,
				R.layout.search_result_view, results);

		ListView searchResultsList = (ListView) findViewById(R.id.search_results_list);

		searchResultsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				if (results.get(position).getItemUrl() != "") {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(results.get(position).getItemUrl()));
					startActivity(browserIntent);
				}
			}

		});

		searchResultsList.setClickable(true);
		searchResultsList.setAdapter(adapter);		
	}

	// Creates ArrayList of SearchResult objects based on data in the JSON
	// passed to activity.
	private ArrayList<SearchResult> resultsAdapterHandler(String json) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		JSONArray query = null;

		advancedFilter = false;

		try {
			query = new JSONArray(json);
		} catch (JSONException e) {
		}

		for (int i = 0; i < query.length(); i++) {
			JSONObject result = null;
			try {
				result = query.getJSONObject(i);
			} catch (JSONException e) {
			}
			if (result != null) {
				try {
					results.add(new SearchResult(result.getString("title"),
							result.getString("meeting_date"), result
									.getString("committee"), result
									.getString("item_url"), result
									.getString("item_num")));
				} catch (JSONException e) {
				}

			}
		}
		return results;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_menu, menu);
		actionMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_subscribe:
			// subscribe with no advanced filters
			String acctEmail = MainActivity.account;
			new SubscribeTask().execute(searchTerm, acctEmail);
			return true;

		case R.id.action_filters:
			// OPEN FILTERS
			LayoutInflater li = LayoutInflater.from(context);
			View advancedFiltersView = li.inflate(R.layout.advanced_filters_view,
					null);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			
			Spinner temp_stat_spin = (Spinner) advancedFiltersView.findViewById(R.id.status_spinner);
			Spinner temp_comm_spin = (Spinner) advancedFiltersView.findViewById(R.id.committee_spinner);
			
			ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(context, 
					R.array.status_filter_list, android.R.layout.simple_spinner_item);
			ArrayAdapter<CharSequence> committeeAdapter = ArrayAdapter.createFromResource(context, 
					R.array.committee_filter_list, android.R.layout.simple_spinner_item);
			
			statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			committeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			temp_stat_spin.setAdapter(statusAdapter);
			temp_comm_spin.setAdapter(committeeAdapter);
			
			temp_stat_spin.setOnItemSelectedListener(new CustomStatusSpinnerListener());
			temp_comm_spin.setOnItemSelectedListener(new CustomCommitteeSpinnerListener());
			
			alertDialogBuilder.setView(advancedFiltersView);

			// set dialog message
			alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							//new Intent here
							String status_hash_val = status_map.get(status_value);
							String comm_hash_val = committee_map.get(committee_value);
							//String processed_term = stripQuery(searchTerm);
							new FilterTask().execute(searchTerm, comm_hash_val, status_hash_val);
						}
					});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
			return true;
			
		case R.id.action_settings:
			// OPEN SETTINGS
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// The FilterTask class is an AsyncTask that performs the advanced
	// filters action.
	class FilterTask extends AsyncTask <String, String, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			filterProgress = new ProgressDialog(SearchResultsActivity.this);
			filterProgress.setTitle("Just one second...");
			filterProgress.setMessage("We're filtering your search!");
			filterProgress.show();
		}

	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        String processed_term = stripQuery(uri[0]);
	        try {
	        	response = httpclient.execute(new HttpGet("http://tabs-server.cloudapp.net/androidSearch/?query="+
	        									processed_term+"&advancedsearch=Advanced&fromDate=&toDate=&decisionBodyId="+
	        									uri[1]+"&itemStatus="+uri[2]));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
			filterProgress.dismiss();
			Intent intent = new Intent(SearchResultsActivity.this, SearchResultsActivity.class);
			intent.putExtra(ADV_SEARCH_JSON, result);
			intent.putExtra(ADV_SEARCH_INPUT, searchTerm);
			startActivity(intent);
	    }
	}
	
	// The SubscribeTask class is an AsyncTask that performs the keyword
	// subscription action.
	class SubscribeTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			String processed_term = stripQuery(uri[0]);
			try {
				response = httpclient.execute(new HttpGet(String.format(
						API_SUBSCRIBE, processed_term, uri[1])));
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
			//Display message upon successful keyword subscription.
			Toast.makeText(SearchResultsActivity.this, 
					"Subscription to keyword '" + searchTerm + "' successful.",
		        	Toast.LENGTH_SHORT).show();
		}
	}
	
	//The method stripQuery replaces all spaces in strings with "%20" values.
	private String stripQuery(String input) {
		return input.replaceAll("\\s+","%20");
	}

}
