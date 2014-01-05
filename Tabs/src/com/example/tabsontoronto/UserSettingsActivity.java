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

import com.example.tabsontoronto.MainActivity.RequestTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class UserSettingsActivity extends Activity {
	
	final Context context = this;
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	String tempNewEmail = null;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_settings_view);
		
		TextView emailDisplay = (TextView) findViewById(R.id.show_email);
		emailDisplay.setText(MainActivity.account);
		
		//Generate Listview of subscribed terms by the user
		//Along with unsubscsribe buttons for each term
	}
	
	class ChangeEmailTask extends AsyncTask <String, String, String> {

	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	        	response = httpclient.execute(new HttpGet("http://tabs-server.cloudapp.net/changeEmail/?oldEmail="+
	        									uri[0]+"&newEmail="+uri[1]));
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
	        if (result.equals("Success")) {
	        	//TODO Handle Email Change Success
	        	MainActivity.account = tempNewEmail;
	        	Editor editor = prefs.edit();
	        	editor.putString("EMAILACCOUNT", tempNewEmail);
				editor.commit();
	        } else {
	        	//TODO Handle Email Change Failure
	        }
	    }
	}
	
	public void clickHandler(View v) {
		
		switch (v.getId()) {
			
			case R.id.edit_email_button:
				LayoutInflater li = LayoutInflater.from(context);
				View firstRunPromptView = li.inflate(R.layout.first_run_prompt, null);
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				alertDialogBuilder.setView(firstRunPromptView);
				final EditText userInput = (EditText) firstRunPromptView.
						findViewById(R.id.editTextSignupEmail);
				
				// set dialog message
				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
					    	// get user input and set it to account
					    	tempNewEmail = userInput.getText().toString();
					    	new ChangeEmailTask().execute(MainActivity.account, tempNewEmail);
					    }
					  });
				
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
				break;
				
			default:
				break;
		
		}
		
	}
	
}