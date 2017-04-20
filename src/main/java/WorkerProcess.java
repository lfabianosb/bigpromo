import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import messenger.Slack;
import model.Flight;

public class WorkerProcess {

	private static final String GET = "GET";
	private static final String ZONE_ID = "GMT-03:00";
	private static final String CHARSET = "UTF-8";
	private static final int CONNECTION_TIMEOUT = 30000; // 30s
	private static HashMap<String, Float> flights = new HashMap<String, Float>();

	static {
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=SCL&dayDep=28&monthDep=10&yearDep=2017&dayArr=4&monthArr=11&yearArr=2017&adult=2&child=0",
				1100f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=SCL&dayDep=29&monthDep=10&yearDep=2017&dayArr=4&monthArr=11&yearArr=2017&adult=2&child=0",
				1100f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=MVD&dayDep=28&monthDep=10&yearDep=2017&dayArr=4&monthArr=11&yearArr=2017&adult=2&child=0",
				1100f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=MVD&dayDep=29&monthDep=10&yearDep=2017&dayArr=4&monthArr=11&yearArr=2017&adult=2&child=0",
				1100f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=SAO&dayDep=6&monthDep=5&yearDep=2017&dayArr=9&monthArr=5&yearArr=2017&adult=2&child=0",
				480f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=SAO&dayDep=15&monthDep=6&yearDep=2017&dayArr=18&monthArr=6&yearArr=2017&adult=2&child=0",
				480f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=CWB&dayDep=15&monthDep=6&yearDep=2017&dayArr=18&monthArr=6&yearArr=2017&adult=2&child=0",
				590f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=VIX&dayDep=23&monthDep=9&yearDep=2017&dayArr=30&monthArr=9&yearArr=2017&adult=2&child=0",
				820f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=VIX&dayDep=24&monthDep=9&yearDep=2017&dayArr=30&monthArr=9&yearArr=2017&adult=2&child=0",
				820f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=VIX&dayDep=23&monthDep=9&yearDep=2017&dayArr=29&monthArr=9&yearArr=2017&adult=2&child=0",
				820f);
		flights.put(
				"https://bigpromoservice.herokuapp.com/flight/voegol?from=JPA&to=VIX&dayDep=24&monthDep=9&yearDep=2017&dayArr=29&monthArr=9&yearArr=2017&adult=2&child=0",
				820f);

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
					connection.setConnectTimeout(CONNECTION_TIMEOUT);
					connection.setUseCaches(false);
					connection.setDoInput(true);
					connection.setDoOutput(true);

					int codeResponse = connection.getResponseCode();
					String msgResponse = connection.getResponseMessage();

					boolean isError = codeResponse >= 400;
					InputStream is = isError ? connection.getErrorStream() : connection.getInputStream();
					String contentEncoding = connection.getContentEncoding() != null ? connection.getContentEncoding()
							: CHARSET;
					String response = IOUtils.toString(is, contentEncoding);
					is.close();

					if (isError) {
						Slack slack = new Slack();
						slack.sendMessage("[" + getCurrentDateTime() + "] Ocorreu o seguinte erro: " + codeResponse
								+ " - " + msgResponse + "\nURL: " + mentry.getKey().toString(), Slack.ERROR);
						System.err.println("Ocorreu o seguinte erro: " + response + "\nResponse: " + codeResponse + " - "
								+ msgResponse);
					} else {
						Flight flight = jsonToFlight(response.toString());

						System.out.println("[" + getCurrentDateTime() + "] " + flight);

						if (flight.getPriceTotal() < (Float) mentry.getValue()) {
							Slack slack = new Slack();
							String resp = slack.sendMessage("[" + getCurrentDateTime() + "] Comprar voo " + flight,
									Slack.INFO);
							System.out.println("Resposta da mensagem enviada para o Slack: " + resp);
						}
					}
				} catch (Exception e) {
					Slack slack = new Slack();
					slack.sendMessage("Erro: " + e.getMessage(), Slack.ERROR);
					System.err.println("Erro: " + e.getMessage());
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}

				// Esperar 30 segundos entre as requisições
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					Slack slack = new Slack();
					slack.sendMessage("Erro: " + e.getMessage(), Slack.ERROR);
					System.err.println("Erro: " + e.getMessage());
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(300000); // 5min
			} catch (InterruptedException e) {
				Slack slack = new Slack();
				slack.sendMessage("Erro: " + e.getMessage(), Slack.ERROR);
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
