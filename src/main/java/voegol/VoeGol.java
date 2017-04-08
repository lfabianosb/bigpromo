package voegol;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import voegol.exception.FlightNotFoundException;
import voegol.exception.SearchException;

public class VoeGol {
	private static final DateTimeFormatter US_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd");
	private static final String CHARSET = "UTF-8";
	private static final String URL_GET = "http://compre2.voegol.com.br/Select2.aspx";
	private static final String URL_POST = "http://compre2.voegol.com.br/CSearch.aspx?culture=pt-br";
	private static final int TIMER_PERIOD = 60000;
	private static final long THRESHOLD_PRICE = 900;

	private String from;
	private String to;
	private LocalDate ddep;
	private LocalDate dret;
	private int adult;
	private int child;

	public VoeGol(String from, String to, LocalDate ddep, LocalDate dret, int adult, int child) {
		this.from = from;
		this.to = to;
		this.ddep = ddep;
		this.dret = dret;
		this.adult = adult;
		this.child = child;

		System.out.println("De " + from + " Para " + to + " ::: " + ddep
				+ " - " + dret + " ::: adultos: " + adult + ", crianças: " + child);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Timer timer = new Timer();
		TimerTask myTask = new TimerTask() {
		    @Override
		    public void run() {
		    	try {
			    	LocalDate ida = LocalDate.of(2017, 3, 25);
					LocalDate volta = LocalDate.of(2017, 3, 29);
					
					VoeGol gol = new VoeGol("JPA", "AEP", ida, volta, 2, 0);
					HttpPost post = gol.configSearch();
					Document document = gol.search(post);
					BigDecimal price = gol.findMinorPrice(document);
					if (gol.isGoodPrice(price)) {
						System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
						System.out.println(":::::::::::::::::::::::: COMPRAR ::::::::::::::::::::::::");
						System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n");
					}
			    } catch (Exception e) {
					System.err.println("Erro: " + e.getMessage());
					e.printStackTrace();
				}
		    }
		};

		timer.schedule(myTask, 0, TIMER_PERIOD);
	}
	
	//Configurar a pesquisa
	public HttpPost configSearch() throws SearchException {
		HttpPost post = new HttpPost(URL_POST);
		addHeaders(post);
		try {
			addParameters(post);
		} catch (UnsupportedEncodingException e) {
			throw new SearchException(e);
		}
		
		return post;
	}
	
	//Pesquisar
	public Document search(HttpPost post) throws SearchException {
		CloseableHttpClient client = buildHttpClient();
		Document document = null;
		try {
			// Faz esta requisição para setar os parâmetros na sessão
			client.execute(post);
			
			HttpResponse r = client.execute(new HttpGet(URL_GET));
			InputStream content = r.getEntity().getContent();
			document = Jsoup.parse(content, CHARSET, URL_GET);
			content.close();
			client.close();
		} catch (Exception e) {
			throw new SearchException(e);
		}
		
		return document;
	}
	
	//Encontrar o menor preço de ida e volta
	public BigDecimal findMinorPrice(Document document) throws FlightNotFoundException {
		BigDecimal total = new BigDecimal(0);
		Elements prices = document.select("li.active span span:last-child");
		
		if (prices.size() != 2) {
			throw new FlightNotFoundException("Flight not found");
		}
		
		for (Element price : prices) {
			Double dblPrice = new Double(0);

			if (price.text() != null && price.text().length() > 0) {
				//TODO ajustar essa conversão
				dblPrice = new Double(price.text().replace(".", "").replace(",", "."));
				System.out.println(dblPrice);
				BigDecimal bgdPrice = new BigDecimal(dblPrice);
				total = total.add(bgdPrice);
			}
		}
	
		System.out.println(total.doubleValue() + "\n");
		
		return total;
	}
	
	//Verificar se a soma dos preços de ida e volta aciona a compra 
	public boolean isGoodPrice(BigDecimal price) {
		if (price.compareTo(new BigDecimal(THRESHOLD_PRICE)) < 0) {
			return true;
		}
		
		return false;
	}

	private CloseableHttpClient buildHttpClient() {
		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
		CookieStore cookieStore = new BasicCookieStore();
		
	    return HttpClients.custom()
				.setDefaultRequestConfig(globalConfig)
				.setDefaultCookieStore(cookieStore)
				.build();
	}

	private static void addHeaders(HttpRequestBase req) {
		req.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		req.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		req.addHeader("Accept-Language", "en-US,en;q=0.8,pt-BR;q=0.6,pt;q=0.4,es;q=0.2");
		req.addHeader("Origin", "http://www.voegol.com.br");
		req.addHeader("Cache-Control", "max-age=0");
		req.addHeader("Connection", "keep-alive");
		req.addHeader("Host", "compre2.voegol.com.br");
		req.addHeader("Referer", "http://www.voegol.com.br/pt-br/Paginas/default.aspx");
		req.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
	}

	private void addParameters(HttpPost post)
			throws UnsupportedEncodingException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

		urlParameters.add(new BasicNameValuePair("header-chosen-origin", ""));
		urlParameters.add(new BasicNameValuePair("destiny-hidden", "false"));
		urlParameters.add(new BasicNameValuePair("header-chosen-destiny", ""));
		urlParameters.add(new BasicNameValuePair("goBack", "goAndBack"));
		urlParameters.add(new BasicNameValuePair("promotional-code", ""));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$TextBoxMarketOrigin1", from));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$TextBoxMarketDestination1", to));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketDay1", DAY.format(ddep)));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketMonth1", US_MONTH.format(ddep)));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketDay2", DAY.format(dret)));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketMonth2", US_MONTH.format(dret)));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_ADT", "" + adult));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_CHD", "" + child));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_INFT", "0"));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$RadioButtonMarketStructure", "RoundTrip"));
		urlParameters
				.add(new BasicNameValuePair(
						"ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListResidentCountry", "br"));
		urlParameters.add(new BasicNameValuePair("PageFooter_SearchView$DropDownListOriginCountry", "pt"));
		urlParameters.add(new BasicNameValuePair("ControlGroupSearchView$ButtonSubmit", "compre aqui"));
		urlParameters.add(new BasicNameValuePair("size", "small"));
		urlParameters.add(new BasicNameValuePair("color", "default"));
		urlParameters.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		urlParameters.add(new BasicNameValuePair("__EVENTTARGET", ""));
		post.setEntity(new UrlEncodedFormEntity(urlParameters));
	}

}