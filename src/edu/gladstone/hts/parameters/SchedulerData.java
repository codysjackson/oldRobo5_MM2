package edu.gladstone.hts.parameters;

public class SchedulerData {

	private String date;
	private String timepoint;
	private String time;
	private String hour;
	private String estimatedDuration;
	private String mode; /* mode can be a ";" delimited string */
	
	public SchedulerData(){}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTimepoint() {
		return timepoint;
	}
	public void setTimepoint(String timepoint) {
		this.timepoint = timepoint;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getEstimatedDuration() {
		return estimatedDuration;
	}
	public void setEstimatedDuration(String estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
}
