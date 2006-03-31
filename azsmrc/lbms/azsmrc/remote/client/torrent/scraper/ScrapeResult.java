package lbms.azsmrc.remote.client.torrent.scraper;

import java.io.IOException;
import java.util.Map;

import lbms.azsmrc.shared.BDecoder;

public class ScrapeResult {
	private long seeds, leechers, downloaded;

	public ScrapeResult (byte[] scrapeData) {
		try {
			Map<String, Object> data = BDecoder.decode(scrapeData);
			System.out.println(data.get("complete"));
			System.out.println(data.get("incomplete"));
			System.out.println(data.get("downloaded"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
