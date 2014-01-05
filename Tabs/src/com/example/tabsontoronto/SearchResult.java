package com.example.tabsontoronto;

public class SearchResult {

	private String title;
	private String meetineDate;
	private String committee;
	private String itemUrl;
	private String itemNum;

	public SearchResult(String title, String meeting_date, String committee,
			String item_url, String item_num) {
		this.setTitle(title);
		this.setMeetingDate(meeting_date);
		this.setCommittee(committee);
		this.setItemUrl(item_url);
		this.setItemNum(item_num);

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMeetingDate() {
		return meetineDate;
	}

	public void setMeetingDate(String meeting_date) {
		this.meetineDate = meeting_date;
	}

	public String getCommittee() {
		return committee;
	}

	public void setCommittee(String committee) {
		this.committee = committee;
	}

	public String getItemUrl() {
		return itemUrl;
	}

	public void setItemUrl(String item_url) {
		this.itemUrl = item_url;
	}

	public String getItemNum() {
		return itemNum;
	}

	public void setItemNum(String item_num) {
		this.itemNum = item_num;
	}

}
