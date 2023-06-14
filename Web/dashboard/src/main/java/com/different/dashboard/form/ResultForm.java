package com.different.dashboard.form;

public class ResultForm {

	String timestamp;
	int round;
	String totalTime;
	float score;

	
	public String getTimeStamp() {
		return timestamp;
	}
	public void setTimeStamp(String timestamp) {
		this.timestamp=timestamp;
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round=round;
	}
	public String getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(String totalTime) {
		this.totalTime=totalTime;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score=score;
	}
}
