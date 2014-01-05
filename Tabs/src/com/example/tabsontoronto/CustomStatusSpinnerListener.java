package com.example.tabsontoronto;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

//The CustomStatusSpinnerListener class listens to user option picks on the
//Status filter spinner and updates corresponding values based on the selected
//option.
public class CustomStatusSpinnerListener implements OnItemSelectedListener {
	 
	  public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
		//Toast.makeText(parent.getContext(), 
		//	"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
		//	Toast.LENGTH_SHORT).show();
		  SearchResultsActivity.advancedFilter = true;
		  SearchResultsActivity.status_value = parent.getItemAtPosition(pos).toString();		  
	  }
	 
	  @Override
	  public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	  }
	 
	}
