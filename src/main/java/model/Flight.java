package model;

public class Flight {
	private String from;
	private String to;
	private String dateDeparture;
	private String dateArrival;
	private int adult;
	private int child;
	private float priceDep;
	private float priceRet;
	
	public Flight() {
	}
	
	public Flight(String from, String to, String dateDeparture, String dateArrival, int adult, int child) {
		this.setFrom(from);
		this.setTo(to);
		this.setDateDeparture(dateDeparture);
		this.setDateArrival(dateArrival);
		this.setAdult(adult);
		this.setChild(child);
	}
	
	public Flight(String from, String to, String dateDeparture, String dateArrival, int adult, int child, float priceDep, float priceRet) {
		this.setFrom(from);
		this.setTo(to);
		this.setDateDeparture(dateDeparture);
		this.setDateArrival(dateArrival);
		this.setAdult(adult);
		this.setChild(child);
		this.setPriceDep(priceDep);
		this.setPriceRet(priceRet);
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
	public String getDateDeparture() {
		return dateDeparture;
	}
	public void setDateDeparture(String dateDeparture) {
		this.dateDeparture = dateDeparture;
	}
	public String getDateArrival() {
		return dateArrival;
	}
	public void setDateArrival(String dateArrival) {
		this.dateArrival = dateArrival;
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

	public float getPriceDep() {
		return priceDep;
	}

	public void setPriceDep(float priceDep) {
		this.priceDep = priceDep;
	}

	public float getPriceRet() {
		return priceRet;
	}

	public void setPriceRet(float priceRet) {
		this.priceRet = priceRet;
	}
	
	public float getPriceTotal() {
		return priceDep + priceRet;
	}

	@Override
	public String toString() {
		return getFrom() + ", " + getTo() + ", " + getDateDeparture() + ", " + getDateArrival() + ", "
				+ getAdult() + ", " + getChild() + ", " + getPriceDep() + ", " + getPriceRet() + ", " + getPriceTotal();
	}
	
}