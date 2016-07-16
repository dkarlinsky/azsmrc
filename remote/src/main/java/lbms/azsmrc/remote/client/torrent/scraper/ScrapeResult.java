package lbms.azsmrc.remote.client.torrent.scraper;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import lbms.azsmrc.shared.BDecoder;
import lbms.azsmrc.shared.RemoteConstants;

public class ScrapeResult {
	private String scrapeUrl;
	private long seeds, leechers, downloaded;

	private boolean failure = false;

	private String failureReason = "";

	public ScrapeResult(byte[] scrapeData) {
		try {
			Map data = BDecoder.decode(scrapeData);
			Iterator iter;
			/*iter = data.keySet().iterator();
			while (iter.hasNext()) {
				System.out.println(iter.next());
			}*/
			if (data.containsKey("failure reason")) {
				failure = true;
				failureReason = new String((byte[]) data.get("failure reason"),
						RemoteConstants.DEFAULT_ENCODING);
				// System.out.println(failureReason);
			} else {
				Map files = (Map) data.get("files");
				iter = files.keySet().iterator();
				while (iter.hasNext()) {
					Map stats = (Map) files.get(iter.next());
					seeds = (Long) stats.get("complete");
					leechers = (Long) stats.get("incomplete");
					downloaded = (Long) stats.get("downloaded");
				}
				System.out.println("Seeds = " + seeds + "\nLeechers = "
						+ leechers + "\nDownloaded = " + downloaded);
			}
		} catch (IOException e) {
			failure = true;
			failureReason = e.getMessage();
			e.printStackTrace();
		}
	}

	public ScrapeResult (String scrapeURL, byte[] scrapeData) {
		this(scrapeData);
		this.scrapeUrl = scrapeURL;
	}

	/**
	 * @return Returns the downloaded.
	 */
	public long getDownloaded() {
		return downloaded;
	}

	/**
	 * @return Returns the leechers.
	 */
	public long getLeechers() {
		return leechers;
	}

	/**
	 * @return Returns the seeds.
	 */
	public long getSeeds() {
		return seeds;
	}

	/**
	 * @return Returns the failure.
	 */
	public boolean hasFailed() {
		return failure;
	}

	/**
	 * @return Returns the failureReason.
	 */
	public String getFailureReason() {
		return failureReason;
	}

	/**
	 * @return Returns the scrapeUrl.
	 */
	public String getScrapeUrl() {
		return scrapeUrl;
	}


}
