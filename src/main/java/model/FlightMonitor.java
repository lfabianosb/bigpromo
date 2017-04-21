package model;

public class FlightMonitor {
	private String from;
	private String to;
	private String dtDep;
	private String dtRet;
	private int adult;
	private int child;
	private double alertPrice;

	public FlightMonitor() {
	}

	public FlightMonitor(String from, String to, String dtDep, String dtRet, int adult, int child,
			double alertPrice) {
		this.setFrom(from);
		this.setTo(to);
		this.setDtDep(dtDep);
		this.setDtRet(dtRet);
		this.setAdult(adult);
		this.setChild(child);
		this.setAlertPrice(alertPrice);
	}

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

	public String getDtDep() {
		return dtDep;
	}

	public void setDtDep(String dtDep) {
		this.dtDep = dtDep;
	}

	public String getDtRet() {
		return dtRet;
	}

	public void setDtRet(String dtRet) {
		this.dtRet = dtRet;
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

	public double getAlertPrice() {
		return alertPrice;
	}

	public void setAlertPrice(double alertPrice) {
		this.alertPrice = alertPrice;
	}

	@Override
	public String toString() {
		return getFrom() + ", " + getTo() + ", " + getDtDep() + ", " + getDtRet() + ", " + getAdult() + ", "
				+ getChild() + ", " + getAlertPrice();
	}

}