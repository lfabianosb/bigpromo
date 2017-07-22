import job.FlightSearchJob;

public class WorkerProcess {

	private static final String GOL = "gol";
	private static final String TAM = "latam";

	public static void main(String[] args) {
		Thread job1 = new Thread(new FlightSearchJob(GOL), GOL);
		job1.start();

//		Thread job2 = new Thread(new FlightSearchJob(TAM), TAM);
//		job2.start();
	}
}