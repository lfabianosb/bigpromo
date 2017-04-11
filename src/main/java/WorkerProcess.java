import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;

import messenger.Slack;
import model.Flight;

public class WorkerProcess {

	private static final String GET = "GET";
	private static final String ZONE_ID = "GMT-03:00";
	private static HashMap<String, Float> flights = new HashMap<String, Float>();

	static {
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=SCL&dayDep=13&monthDep=6&yearDep=2017&dayArr=18&monthArr=6&yearArr=2017&adult=2&child=0",
				1200f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=MVD&dayDep=14&monthDep=6&yearDep=2017&dayArr=18&monthArr=6&yearArr=2017&adult=2&child=0",
				1200f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=SAO&dayDep=6&monthDep=5&yearDep=2017&dayArr=9&monthArr=5&yearArr=2017&adult=2&child=0",
				450f);
	}

	public static void main(String[] args) {
		while (true) {
			Iterator iterator = flights.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry mentry = (Map.Entry) iterator.next();

				HttpURLConnection connection = null;
				try {
					// Create connection
					URL url = new URL(mentry.getKey().toString());
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

					if (flight.getPrice() < (Float) mentry.getValue()) {
						Slack slack = new Slack();
						String resp = slack.sendMessage("[" + getCurrentDateTime() + "] Comprar voo " + flight);
						System.out.println("Resposta da mensagem enviada para o Slack: " + resp);
					}
				} catch (Exception e) {
					Slack slack = new Slack();
					slack.sendMessage("Erro: " + e.getMessage());
					System.err.println("Erro: " + e.getMessage());
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}

				//Esperar 5 segundos entre as requisições
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Slack slack = new Slack();
					slack.sendMessage("Erro: " + e.getMessage());
					System.err.println("Erro: " + e.getMessage());
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(600000); // 10min
			} catch (InterruptedException e) {
				Slack slack = new Slack();
				slack.sendMessage("Erro: " + e.getMessage());
				System.err.println("Erro: " + e.getMessage());
				e.printStackTrace();
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
