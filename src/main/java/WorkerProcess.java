import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import messenger.Slack;
import model.Flight;
import model.FlightMonitor;

public class WorkerProcess {

	private static final long SLEEP_TIME = Long.parseLong(System.getenv("SLEEP_TIME"));
	private static final long SLEEP_TIME_BET_REQUESTS = Long.parseLong(System.getenv("SLEEP_TIME_BETWEEN_REQUESTS"));
	private static final int GET_CONNECTION_TIMEOUT = Integer.parseInt(System.getenv("GET_CONNECTION_TIMEOUT"));
	private static final String FIREBASE_AUTH = System.getenv("FIREBASE_AUTH");
	private static final String URL_FLIGHT_SERVICE = System.getenv("FLIGHT_SERVICE");
	private static final String URL_MONITOR_FLIGHTS = System.getenv("MONITOR_FLIGHTS");
	private static final String GET = "GET";
	private static final String ZONE_ID = "GMT-03:00";
	private static final String CHARSET = "UTF-8";

	private static String getURL(FlightMonitor flight) {
		String retorno = null;

		String dayDep = flight.getDtDep().substring(0, 2);
		String monthDep = flight.getDtDep().substring(3, 5);
		String yearDep = flight.getDtDep().substring(6);
		String dayRet = flight.getDtRet().substring(0, 2);
		String monthRet = flight.getDtRet().substring(3, 5);
		String yearRet = flight.getDtRet().substring(6);

		retorno = URL_FLIGHT_SERVICE + "/flight/voegol?from=" + flight.getFrom() + "&to=" + flight.getTo() + "&dayDep="
				+ dayDep + "&monthDep=" + monthDep + "&yearDep=" + yearDep + "&dayArr=" + dayRet + "&monthArr="
				+ monthRet + "&yearArr=" + yearRet + "&adult=" + flight.getAdult() + "&child=" + flight.getChild();

		return retorno;
	}

	private static List<FlightMonitor> checkFlights() {
		List<FlightMonitor> retorno = new ArrayList<FlightMonitor>();
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			URL url = new URL(URL_MONITOR_FLIGHTS + "?auth=" + FIREBASE_AUTH);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(GET);
			connection.setConnectTimeout(GET_CONNECTION_TIMEOUT);
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			int codeResponse = connection.getResponseCode();

			boolean isError = codeResponse >= 400;
			is = isError ? connection.getErrorStream() : connection.getInputStream();
			String contentEncoding = connection.getContentEncoding() != null ? connection.getContentEncoding()
					: CHARSET;
			String response = IOUtils.toString(is, contentEncoding);

			if (!isError) {
				Gson gson = new Gson();
				JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
				for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
					FlightMonitor flight = gson.fromJson(entry.getValue(), FlightMonitor.class);
					retorno.add(flight);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}

		return retorno;
	}

	public static void main(String[] args) {
		while (true) {
			List<FlightMonitor> flights = checkFlights();

			for (FlightMonitor fltm : flights) {
				HttpURLConnection connection = null;
				InputStream is = null;
				try {
					String strUrl = getURL(fltm);
					URL url = new URL(strUrl);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod(GET);
					connection.setConnectTimeout(GET_CONNECTION_TIMEOUT);
					connection.setUseCaches(false);
					connection.setDoInput(true);
					connection.setDoOutput(true);

					int codeResponse = connection.getResponseCode();
					String msgResponse = connection.getResponseMessage();

					boolean isError = codeResponse >= 400;
					is = isError ? connection.getErrorStream() : connection.getInputStream();
					String contentEncoding = connection.getContentEncoding() != null ? connection.getContentEncoding()
							: CHARSET;
					String response = IOUtils.toString(is, contentEncoding);

					if (isError) {
						Slack slack = new Slack();
						slack.sendMessage("[" + getCurrentDateTime() + "] Ocorreu o seguinte erro: " + codeResponse
								+ " - " + msgResponse + "\nURL: " + strUrl, Slack.ERROR);
						System.err.println("Ocorreu o seguinte erro: " + response + "\nResponse: " + codeResponse
								+ " - " + msgResponse);
					} else {
						Flight flight = jsonToFlight(response.toString());

						System.out.println("[" + getCurrentDateTime() + "] " + flight);

						if (flight.getPriceTotal() < fltm.getAlertPrice()) {
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
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
						}
					}
				}

				try {
					Thread.sleep(SLEEP_TIME_BET_REQUESTS);
				} catch (InterruptedException e) {
					Slack slack = new Slack();
					slack.sendMessage("Erro: " + e.getMessage(), Slack.ERROR);
					System.err.println("Erro: " + e.getMessage());
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(SLEEP_TIME);
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
