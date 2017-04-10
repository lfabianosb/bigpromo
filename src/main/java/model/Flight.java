package model;

public class Flight {
	private String from;
	private String to;
	private String dateDeparture;
	private String dateArrival;
	private int adult;
	private int child;
	private float price;
	
	public Flight(String from, String to, String dateDeparture, String dateArrival, int adult, int child, float price) {
		this.setFrom(from);
		this.setTo(to);
		this.setDateDeparture(dateDeparture);
		this.setDateArrival(dateArrival);
		this.setAdult(adult);
		this.setChild(child);
		this.setPrice(price);
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

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
	
	@Override
	public String toString() {
		return getTo() + ", " + getFrom() + ", " + getDateDeparture() + ", " + getDateArrival() + ", "
				+ getAdult() + ", " + getChild() + ", " + getPrice();
	}
	
}
