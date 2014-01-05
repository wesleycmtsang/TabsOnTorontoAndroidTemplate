package com.example.tabsontoronto;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.content.Intent;
import android.app.Activity;

public class ContactFormFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.contact_form_view, container, false);
	}
	
	public void submitForm(View button) {
		
		switch (button.getId()) {
			
		case R.id.contact_submit_button:
			//Prepare to construct the email message
			Log.i("TAG", "INSIDE CASE STATEMTNT");
			final EditText nameField = (EditText) button.findViewById(R.id.contact_name);  
			String name = nameField.getText().toString();  
			final EditText emailField = (EditText) button.findViewById(R.id.contact_subject);  
			String subject = emailField.getText().toString();  
			final EditText feedbackField = (EditText) button.findViewById(R.id.contact_feedback);  
			String feedback = feedbackField.getText().toString();
			
			String admin_email = "@string/admin_contact";
			
			String message = "From: " + name + "\n" + "\n" + feedback;
			
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setData(Uri.parse("mailto:"));
			emailIntent.setType("text/plain");
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{admin_email});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(Intent.EXTRA_TEXT, message);

			//need this to prompt email client only
			//emailIntent.setType("message/rfc822");
			//emailIntent.setType("text/plain");

			startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));
			break;
		}
	}

}
