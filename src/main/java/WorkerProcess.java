import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.http.client.methods.HttpPost;
import org.jsoup.nodes.Document;

import voegol.VoeGol;

public class WorkerProcess {
	public static void main(String[] args) {
		while (true) {
			try {
				LocalDate ida = LocalDate.of(2017, 5, 6);
				LocalDate volta = LocalDate.of(2017, 5, 9);

				VoeGol gol = new VoeGol("JPA", "SAO", ida, volta, 2, 0);
				HttpPost post = gol.configSearch();
				Document document = gol.search(post);
				BigDecimal price = gol.findMinorPrice(document);
				if (gol.isGoodPrice(price)) {
					System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
					System.out.println(":::::::::::::::: COMPRAR POR " + price + " ::::::::::::::::");
					System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n\n\n");
				}

				Thread.sleep(30000);
			} catch (Exception e) {
				System.out.println("ERRO: " + e.getMessage());
				e.printStackTrace();
			}
			System.out.println("Worker process woke up");
		}
	}
}
