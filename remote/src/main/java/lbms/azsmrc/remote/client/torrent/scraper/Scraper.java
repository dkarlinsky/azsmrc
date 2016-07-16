package lbms.azsmrc.remote.client.torrent.scraper;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.torrent.TOTorrent;
import lbms.azsmrc.remote.client.torrent.TOTorrentException;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.HTTPDownload;

public class Scraper implements Runnable {
	private static String peer_id = "Scraper_"+Long.toString(System.currentTimeMillis()%1000000000000l);
	private TOTorrent torrent;
	private String infoHash;
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
	 * And use the normal announce url
	 * 
	 * @return true if scrape is supported, false if not
	 */
	public boolean doScrape() {
		URL announceUrl = torrent.getAnnounceURL();
		if (getInfoHash() == "") return false;
		return scrape (announceUrl.toExternalForm());
	}


	public boolean scrape (String scrapeURL) {
		if (scrapeURL.lastIndexOf('/') != scrapeURL.indexOf("announce")-1 ) return false;
		char first_separator = scrapeURL.indexOf('?') == -1 ? '?' : '&';
		scrapeURL = scrapeURL.replace("announce", "scrape");
		scrapeURL += first_separator+"info_hash="+getInfoHash()+"&peer_id="+peer_id;
//		System.out.println(scrapeURL);
		try {
			URL realScrapeURL = new URL (scrapeURL);
			HTTPDownload dl = new HTTPDownload(realScrapeURL);
			dl.setProxy(RCMain.getRCMain().getProxy());
			dl.run();
//			System.out.println(dl.getBuffer().toString());
			if (dl.hasFailed()) {
				callScrapeFailedListener("Couldn't connect to Server: "+dl.getFailureReason());
			} else {
				byte[] scrapeRes = dl.getBuffer().toString().getBytes(RemoteConstants.BYTE_ENCODING);
				scrapeResult = new ScrapeResult(scrapeURL,scrapeRes);
				callListener(scrapeResult);
			}
		} catch (Exception e) {
			callScrapeFailedListener(e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	private String getInfoHash() {
		if (infoHash != null) return infoHash;
		infoHash = "";
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
		return infoHash;
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

	public void callListener (ScrapeResult sr) {
		for (ScrapeListener l:listeners) {
			if (sr.hasFailed())
				l.scrapeFailed(sr.getFailureReason());
			else
				l.scrapeFinished(sr);
		}
	}
	public void callScrapeFailedListener (String reason) {
		for (ScrapeListener l:listeners) {
			l.scrapeFailed(reason);
		}
	}
}
