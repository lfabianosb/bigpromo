package job;

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

public class FlightSearchJob implements Runnable {

	private static final String GET = "GET";
	private static final String ZONE_ID = "GMT-03:00";
	private static final String CHARSET = "UTF-8";
	private static final int CONNECTION_RESET_EXCEPTION = 600;
	private static final int SOCKET_TIMEOUT_EXCEPTION = 601;

	private String company;

	public FlightSearchJob(String company) {
		this.company = company;
	}

	@Override
	public void run() {

		int counter = 1;
		while (true) {
			List<FlightMonitor> flights = checkFlights();

			for (FlightMonitor fltm : flights) {
				HttpURLConnection connection = null;
				InputStream is = null;
				try {
					String strUrl = getURL(fltm, company);
					URL url = new URL(strUrl);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod(GET);
					connection.setConnectTimeout(Integer.parseInt(System.getenv("GET_CONNECTION_TIMEOUT")));
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
					String now = getCurrentDateTime();

					if (isError) {
						if (codeResponse != CONNECTION_RESET_EXCEPTION && codeResponse != SOCKET_TIMEOUT_EXCEPTION) {
							new Slack().sendMessage("[" + now + "] Ocorreu o seguinte erro: " + codeResponse + " - "
									+ msgResponse + "\nURL: " + strUrl, Slack.ERROR);
							System.err.println("Ocorreu o seguinte erro: " + response + "\nResponse: " + codeResponse
									+ " - " + msgResponse);
						} else {
							System.err.println("code response error = " + codeResponse);
						}
					} else {
						Flight flight = new Gson().fromJson(response.toString(), Flight.class);

						System.out.println(" [" + now + "] (" + company.toUpperCase() + "): " + flight);

						if (flight.getPriceTotal() < fltm.getAlertPrice()) {
							Slack slack = new Slack();
							String resp = slack.sendMessage(
									"[" + now + "] Comprar voo (" + company.toUpperCase() + ") " + flight, Slack.ALERT);

							System.out.println("Resposta da mensagem enviada para o Slack: " + resp);
						}
					}
				} catch (Exception e) {
					Slack slack = new Slack();
					slack.sendMessage("Erro: " + e.getMessage(), Slack.ERROR);
					System.err.println("Erro: " + e.getMessage());
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
						}
					}
					if (connection != null) {
						connection.disconnect();
					}
				}

				// Esperar entre cada voo que será pesquisado
				try {
					Thread.sleep(Long.parseLong(System.getenv("SLEEP_TIME_BETWEEN_REQUESTS")));
				} catch (InterruptedException e) {
					String now = getCurrentDateTime();
					new Slack().sendMessage("[" + now + "] Erro: " + e.getMessage(), Slack.ERROR);
					System.err.println("[" + now + "] Erro: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if ((counter % Integer.parseInt(System.getenv("MSG_INFO_AFTER_N_TIMES"))) == 0) {
				new Slack().sendMessage("[" + getCurrentDateTime() + "] I'm Working!", Slack.INFO);
			}

			// Reset counter
			if (counter++ > 1000)
				counter = 1;

			// Aguardar um pouco antes de reiniciar o ciclo de pesquisas
			try {
				Thread.sleep(Long.parseLong(System.getenv("SLEEP_TIME")));
			} catch (InterruptedException e) {
				String now = getCurrentDateTime();
				new Slack().sendMessage("[" + now + "] Erro: " + e.getMessage(), Slack.ERROR);
				System.err.println("[" + now + "] Erro: " + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/**
	 * Listar os voos que devem ser monitorados
	 * 
	 * @return Lista de voos
	 */
	private List<FlightMonitor> checkFlights() {
		List<FlightMonitor> retorno = new ArrayList<FlightMonitor>();
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			URL url = new URL(System.getenv("MONITOR_FLIGHTS") + "?auth=" + System.getenv("FIREBASE_AUTH"));
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(GET);
			connection.setConnectTimeout(Integer.parseInt(System.getenv("GET_CONNECTION_TIMEOUT")));
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
				for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
					FlightMonitor flight = gson.fromJson(entry.getValue(), FlightMonitor.class);
					retorno.add(flight);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}

		return retorno;
	}

	/**
	 * Obter a URL do serviço
	 * 
	 * @param flight
	 * @param company
	 * @return URL do serviço
	 */
	private String getURL(FlightMonitor flight, String company) {
		return System.getenv("FLIGHT_SERVICE") + "/flight/" + company + "?from=" + flight.getFrom() + "&to="
				+ flight.getTo() + "&dep=" + flight.getDtDep() + "&ret=" + flight.getDtRet() + "&adult="
				+ flight.getAdult() + "&child=" + flight.getChild();
	}

	/**
	 * Obter data e hora corrente
	 * 
	 * @return Data e hora corrente no formato dd/MM/yyyy HH:mm:ss
	 */
	private String getCurrentDateTime() {
		ZoneId zoneId = ZoneId.of(ZONE_ID);
		LocalDateTime now = LocalDateTime.now(zoneId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		return now.format(formatter);
	}

}
