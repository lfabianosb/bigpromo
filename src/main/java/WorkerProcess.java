import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.google.gson.Gson;

import messenger.Slack;
import model.Flight;

public class WorkerProcess {

	private static final String GET = "GET";
	private static final String URL = "https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=SCL&dayDep=13&monthDep=6&yearDep=2017&dayArr=18&monthArr=6&yearArr=2017&adult=2&child=0";
	private static final String ZONE_ID = "GMT-03:00";

	public static void main(String[] args) {
		while (true) {
			HttpURLConnection connection = null;
			try {
				// Create connection
				URL url = new URL(URL);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(GET);
				connection.setConnectTimeout(20000); // 20s
				connection.setUseCaches(false);
				connection.setDoInput(true);
				connection.setDoOutput(true);

				// Get Response
				final InputStream is = connection.getInputStream();
				final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuilder response = new StringBuilder();
				while ((line = rd.readLine()) != null) {
					response.append(line);
					response.append('\n');
				}
				rd.close();

				Flight flight = jsonToFlight(response.toString());
				System.out.println(flight);

				if (flight.getPrice() < 5000) {
					System.out.println("Preco: " + flight.getPrice());
					
					Slack slack = new Slack();
					String resp = slack.sendMessage("[" + getCurrentDateTime() + "] Comprar voo " + flight);
					System.out.println(resp);
				}

				Thread.sleep(600000); // 10min
			} catch (Exception e) {
				Slack slack = new Slack();
				slack.sendMessage("Erro: " + e.getMessage());
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
	}

	private static Flight jsonToFlight(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, Flight.class);
	}
	
	private static String getCurrentDateTime() {
		ZoneId zoneId = ZoneId.of(ZONE_ID);
		LocalDateTime now = LocalDateTime.now(zoneId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		return now.format(formatter);
	}
}
