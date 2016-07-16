package lbms.azsmrc.remote.client.torrent.scraper;

public interface ScrapeListener {
	public void scrapeFinished(ScrapeResult sr);
	public void scrapeFailed(String reason);
}
