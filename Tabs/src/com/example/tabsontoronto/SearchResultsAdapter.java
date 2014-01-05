package com.example.tabsontoronto;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SearchResultsAdapter extends ArrayAdapter<SearchResult> {

	Context context;
	int resourceId;
	ArrayList<SearchResult> results;

	public SearchResultsAdapter(Context context, int resource,
			List<SearchResult> objects) {
		super(context, resource, objects);

		this.context = context;
		this.resourceId = resource;
		this.results = (ArrayList<SearchResult>) objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = ((Activity) this.context)
					.getLayoutInflater();
			convertView = inflater.inflate(this.resourceId, parent, false);
		}

		SearchResult result = results.get(position);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView meeting_date = (TextView) convertView
				.findViewById(R.id.meeting_date);
		TextView committee = (TextView) convertView
				.findViewById(R.id.committee);
		TextView item_num = (TextView) convertView.findViewById(R.id.item_num);

		title.setText(result.getTitle());
		meeting_date.setText(result.getMeetingDate());
		committee.setText(result.getCommittee());
		item_num.setText(result.getItemNum());

		return convertView;
	}

}
