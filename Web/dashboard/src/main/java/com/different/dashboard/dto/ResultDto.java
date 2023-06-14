package com.different.dashboard.dto;

public class ResultDto {

	String timeStamp;
	int round;
	String totalTime;
	float score;
	
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timestamp) {
		this.timeStamp=timestamp;
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
