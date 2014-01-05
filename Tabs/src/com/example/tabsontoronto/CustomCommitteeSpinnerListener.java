package com.example.tabsontoronto;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

//The CustomCommitteeSpinnerListener class listens to user option picks on the
//Committee filter spinner and updates corresponding values based on the selected
//option.
public class CustomCommitteeSpinnerListener implements OnItemSelectedListener {
	 
	  public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
		  SearchResultsActivity.advancedFilter = true;
		  SearchResultsActivity.committee_value = parent.getItemAtPosition(pos).toString();
	  }
	 
	  @Override
	  public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	  }
	 
	}
