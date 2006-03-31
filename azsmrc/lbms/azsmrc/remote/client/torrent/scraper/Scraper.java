package lbms.azsmrc.remote.client.torrent.scraper;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.torrent.TOTorrent;
import lbms.azsmrc.remote.client.torrent.TOTorrentException;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.HTTPDownload;

public class Scraper implements Runnable {
	private static String peer_id = "Scraper_"+Long.toString(System.currentTimeMillis()%1000000000000l);
	private TOTorrent torrent;
	private ScrapeResult scrapeResult;
	private List<ScrapeListener> listeners = new ArrayList<ScrapeListener>();

	public Scraper (TOTorrent torrent ) {
		this.torrent = torrent;
	}

	public void run() {
		doScrape();
	}

	/**
	 * This will scrape the server.
	 * 
	 * @return true if scrape is supported, false if not
	 */
	public boolean doScrape() {
		URL announceUrl = torrent.getAnnounceURL();
		String infoHash = "";
		try {
			infoHash = URLEncoder.encode(
					new String(torrent.getHash(), RemoteConstants.BYTE_ENCODING),
					RemoteConstants.BYTE_ENCODING).replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TOTorrentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (infoHash == "") return false;
		return scrape (announceUrl.toExternalForm(),infoHash);
	}

	private boolean scrape (String scrapeURL,String infoHash) {
		if (scrapeURL.lastIndexOf('/') != scrapeURL.indexOf("announce")-1 ) return false;
		char first_separator = scrapeURL.indexOf('?') == -1 ? '?' : '&';
		scrapeURL = scrapeURL.replace("announce", "scrape");
		scrapeURL += first_separator+"info_hash="+infoHash+"&peer_id="+peer_id;
//		System.out.println(scrapeURL);
		try {
			URL realScrapeURL = new URL (scrapeURL);
			HTTPDownload dl = new HTTPDownload(realScrapeURL);
			dl.run();
//			System.out.println(dl.getBuffer().toString());
			byte[] scrapeRes = dl.getBuffer().toString().getBytes(RemoteConstants.BYTE_ENCODING);
			scrapeResult = new ScrapeResult(scrapeRes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @return Returns the scrapeResult.
	 */
	public ScrapeResult getScrapeResult() {
		return scrapeResult;
	}

	public void addListener (ScrapeListener l) {
		listeners.add(l);
	}

	public void removeListener (ScrapeListener l) {
		listeners.remove(l);
	}
}
