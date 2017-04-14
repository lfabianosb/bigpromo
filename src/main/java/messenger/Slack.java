package messenger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;

public class Slack {

	private static final String POST = "POST";
	private static final String PAYLOAD = "payload=";
	private static final String UTF_8 = "UTF-8";
	private static final String URL = System.getenv("SLACK_WEBHOOK");
	public static final int INFO = 0;
	public static final int ERROR = 1;

	public String sendMessage(String message, int type) {
		HttpURLConnection connection = null;

		try {
			// Message
			JsonObject jsonMsg = new JsonObject();
			jsonMsg.addProperty("channel", "#general");
			jsonMsg.addProperty("username", "webhookbot");
			jsonMsg.addProperty("text", message);
			if (type == Slack.ERROR) // ERROR
				jsonMsg.addProperty("icon_emoji", ":bug:");
			else // INFO
				jsonMsg.addProperty("icon_emoji", ":airplane:");

			// Create connection
			URL url = new URL(URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(POST);
			connection.setConnectTimeout(20000); // 20s
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			String payload = PAYLOAD + URLEncoder.encode(jsonMsg.toString(), UTF_8);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(payload);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
