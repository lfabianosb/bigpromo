package model;

public class FlightMonitor {
	private String from;
	private String to;
	private String dtStart;
	private String dtEnd;
	private int minDays;
	private int maxDays;
	private int adult;
	private int child;
	private boolean nonStop = false;
	private double alertPrice;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getDtStart() {
		return dtStart;
	}

	public void setDtStart(String dtStart) {
		this.dtStart = dtStart;
	}

	public String getDtEnd() {
		return dtEnd;
	}

	public void setDtEnd(String dtEnd) {
		this.dtEnd = dtEnd;
	}

	public int getMinDays() {
		return minDays;
	}

	public void setMinDays(int minDays) {
		this.minDays = minDays;
	}

	public int getMaxDays() {
		return maxDays;
	}

	public void setMaxDays(int maxDays) {
		this.maxDays = maxDays;
	}

	public int getAdult() {
		return adult;
	}

	public void setAdult(int adult) {
		this.adult = adult;
	}

	public int getChild() {
		return child;
	}

	public void setChild(int child) {
		this.child = child;
	}

	public boolean isNonStop() {
		return nonStop;
	}

	public void setNonStop(boolean nonStop) {
		this.nonStop = nonStop;
	}

	public double getAlertPrice() {
		return alertPrice;
	}

	public void setAlertPrice(double alertPrice) {
		this.alertPrice = alertPrice;
	}

	@Override
	public String toString() {
		return getFrom() + ", " + getTo() + ", " + getDtStart() + ", " + getDtEnd() + ", " + getMinDays() + ", "
				+ getMaxDays() + ", " + getAdult() + ", " + getChild() + ", " + isNonStop() + ", " + getAlertPrice();
	}

}
