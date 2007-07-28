package lbms.azsmrc.plugin.web;

import java.util.Map;
import java.util.TreeMap;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.shared.EncodingUtil;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadStats;
import org.gudy.azureus2.plugins.peers.PeerManagerStats;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public class DownloadContainer {

	private static TorrentAttribute[] TA_LIST;

	private Download dl;

	private boolean _new;

	private String name;
	private String hash;
	private boolean forceStart, checking, complete;
	private int position;
	private long downloaded, uploaded;
	private long downloadAVG, uploadAVG, totalAVG;
	private int state;
	private String status;
	private String elapsedTime;
	private String eta;
	private String savePath;
	private float availability;
	private int completition;
	private int health;
	private int shareRatio;
	private String tracker;
	private int downloadLimit, uploadLimit;
	private int seeds, leecher;
	private int total_seeds, total_leecher;
	private long discarded;
	private long last_scrape, next_scrape;
	private long announceTTW, size;
	private Map<String, String> attributes = new TreeMap<String, String>();

	public DownloadContainer (Download dl) throws Exception {
		if (TA_LIST == null) TA_LIST = Plugin.getPluginInterface().getTorrentManager().getDefinedAttributes();
		this.dl = dl;
		if (dl.getTorrent() == null) throw new Exception("Torrent not here");
		_new = true;
		setDlStats();
	}
	public Element updateAndGetDiff () {
		if (_new) {
			_new = false;
			return fullUpdate();
		}
		DownloadStats ds = dl.getStats();
		Element dle = new Element ("Transfer");

		dle.setAttribute("hash", hash);

		if (!name.equals(dl.getName())) {
			name = dl.getName();
			dle.setAttribute("name", name);
		}


		if (forceStart != dl.isForceStart()) {
			forceStart = dl.isForceStart();
			dle.setAttribute("forceStart", Boolean.toString(forceStart));
		}
		if (checking != dl.isChecking()) {
			checking = dl.isChecking();
			dle.setAttribute("checking", Boolean.toString(checking));
		}
		if (complete != dl.isComplete()) {
			complete = dl.isComplete();
			dle.setAttribute("complete", Boolean.toString(complete));
		}
		if (position != dl.getPosition()) {
			position = dl.getPosition();
			dle.setAttribute("position", Integer.toString(position));
		}

		if (downloaded != ds.getDownloaded()) {
			downloaded = ds.getDownloaded();
			dle.setAttribute("downloaded", Long.toString(downloaded));
		}

		if (uploaded != ds.getUploaded()) {
			uploaded = ds.getUploaded();
			dle.setAttribute("uploaded", Long.toString(uploaded));
		}

		if (downloadAVG != ds.getDownloadAverage()) {
			downloadAVG = ds.getDownloadAverage();
			dle.setAttribute("downloadAVG", Long.toString(downloadAVG));
		}

		if (uploadAVG != ds.getUploadAverage()) {
			uploadAVG = ds.getUploadAverage();
			dle.setAttribute("uploadAVG", Long.toString(uploadAVG));
		}

		if (totalAVG != ds.getTotalAverage()) {
			totalAVG = ds.getTotalAverage();
			dle.setAttribute("totalAVG", Long.toString(totalAVG));
		}

		if (state != dl.getState()) {
			state = dl.getState();
			dle.setAttribute("state", Integer.toString(state));
		}

		if (!savePath.equals(dl.getSavePath())) {
			savePath = dl.getSavePath();
			dle.setAttribute("savePath", savePath);
		}

		if (!status.equals(ds.getStatus())) {
			status = ds.getStatus();
			dle.setAttribute("status", status);
		}

		if (!elapsedTime.equals(ds.getElapsedTime())) {
			elapsedTime = ds.getElapsedTime();
			dle.setAttribute("elapsedTime", elapsedTime);
		}

		if (!eta.equals(ds.getETA())) {
			eta = ds.getETA();
			dle.setAttribute("eta", eta);
		}

		if (availability != ds.getAvailability()) {
			availability = ds.getAvailability();
			dle.setAttribute("availability", Float.toString(availability));
		}

		if (completition != ds.getCompleted()) {
			completition = ds.getCompleted();
			dle.setAttribute("completition", Integer.toString(completition));
		}

		if (health != ds.getHealth()) {
			health = ds.getHealth();
			dle.setAttribute("health", Integer.toString(health));
		}

		if (shareRatio != ds.getShareRatio()) {
			shareRatio = ds.getShareRatio();
			dle.setAttribute("shareRatio", Integer.toString(shareRatio));
		}

		if (!tracker.equals(ds.getTrackerStatus())){
			tracker = ds.getTrackerStatus();
			dle.setAttribute("tracker",tracker);
		}

		if (downloadLimit != dl.getMaximumDownloadKBPerSecond()) {
			downloadLimit = dl.getMaximumDownloadKBPerSecond();
			dle.setAttribute("downloadLimit",Integer.toString(downloadLimit));
		}

		if (uploadLimit != dl.getUploadRateLimitBytesPerSecond()) {
			uploadLimit = dl.getUploadRateLimitBytesPerSecond();
			dle.setAttribute("uploadLimit",Integer.toString(uploadLimit));
		}

		if (dl.getState() == Download.ST_DOWNLOADING || dl.getState() == Download.ST_SEEDING) { //Stopped DLs don't have a PeerManager
			PeerManagerStats pms = dl.getPeerManager().getStats();

			if (seeds != pms.getConnectedSeeds()) {
				seeds = pms.getConnectedSeeds();
				dle.setAttribute("seeds",Integer.toString(seeds));
			}

			if (leecher != pms.getConnectedLeechers()) {
				leecher = pms.getConnectedLeechers();
				dle.setAttribute("leecher",Integer.toString(leecher));
			}
			if (discarded != pms.getDiscarded()) {
				discarded = pms.getDiscarded();
				dle.setAttribute("discarded",Long.toString(discarded));
			}
		}

		if (total_seeds != dl.getLastScrapeResult().getSeedCount()) {
			total_seeds = dl.getLastScrapeResult().getSeedCount();
			dle.setAttribute("total_seeds",Integer.toString(total_seeds));
		}

		if (total_leecher != dl.getLastScrapeResult().getNonSeedCount()) {
			total_leecher = dl.getLastScrapeResult().getNonSeedCount();
			dle.setAttribute("total_leecher",Integer.toString(total_leecher));
		}

		if (last_scrape != dl.getLastScrapeResult().getScrapeStartTime()) {
			last_scrape = dl.getLastScrapeResult().getScrapeStartTime();
			dle.setAttribute("last_scrape",Long.toString(last_scrape));
		}

		if (next_scrape != dl.getLastScrapeResult().getNextScrapeStartTime()) {
			next_scrape = dl.getLastScrapeResult().getNextScrapeStartTime();
			dle.setAttribute("next_scrape",Long.toString(next_scrape));
		}

		if (announceTTW != dl.getLastAnnounceResult().getTimeToWait()) {
			announceTTW = dl.getLastAnnounceResult().getTimeToWait();
			dle.setAttribute("announceTimeToWait",Long.toString(announceTTW));
		}

		for (TorrentAttribute ta : TA_LIST) {
			String dlA = dl.getAttribute(ta);
			if (dlA != null) {
				if (attributes.get(ta.getName()) == null || !attributes.get(ta.getName()).equals(dlA)) {
					Element a = new Element("Attribute");
					a.setAttribute("name",ta.getName());
					a.setAttribute("value", dlA);
					attributes.put(ta.getName(), dlA);
					dle.addContent(a);
				}
			}
		}

		return dle;
	}

	public Element fullUpdate () {
		setDlStats();
		return toElement();
	}

	public Element toElement () {
		Element dle = new Element ("Transfer");
		dle.setAttribute("name", name);
		dle.setAttribute("hash", hash);
		dle.setAttribute("forceStart", Boolean.toString(forceStart));
		dle.setAttribute("checking", Boolean.toString(checking));
		dle.setAttribute("complete", Boolean.toString(complete));
		dle.setAttribute("position", Integer.toString(position));
		dle.setAttribute("downloaded", Long.toString(downloaded));
		dle.setAttribute("uploaded", Long.toString(uploaded));
		dle.setAttribute("downloadAVG", Long.toString(downloadAVG));
		dle.setAttribute("uploadAVG", Long.toString(uploadAVG));
		dle.setAttribute("totalAVG", Long.toString(totalAVG));
		dle.setAttribute("state", Integer.toString(state));
		dle.setAttribute("status", status);
		dle.setAttribute("elapsedTime", elapsedTime);
		dle.setAttribute("eta", eta);
		dle.setAttribute("savePath", savePath);
		dle.setAttribute("availability", Float.toString(availability));
		dle.setAttribute("completition", Integer.toString(completition));
		dle.setAttribute("health", Integer.toString(health));
		dle.setAttribute("shareRatio", Integer.toString(shareRatio));
		dle.setAttribute("tracker",tracker);
		dle.setAttribute("downloadLimit",Integer.toString(downloadLimit));
		dle.setAttribute("uploadLimit",Integer.toString(uploadLimit));
		dle.setAttribute("seeds",Integer.toString(seeds));
		dle.setAttribute("leecher",Integer.toString(leecher));
		dle.setAttribute("discarded",Long.toString(discarded));
		dle.setAttribute("total_seeds",Integer.toString(total_seeds));
		dle.setAttribute("total_leecher",Integer.toString(total_leecher));
		dle.setAttribute("last_scrape",Long.toString(last_scrape));
		dle.setAttribute("next_scrape",Long.toString(next_scrape));
		dle.setAttribute("announceTimeToWait",Long.toString(announceTTW));
		dle.setAttribute("size",Long.toString(size));
		for (TorrentAttribute ta : TA_LIST) {
			String dlA = dl.getAttribute(ta);
			if (dlA != null) {
				Element a = new Element("Attribute");
				a.setAttribute("name",ta.getName());
				a.setAttribute("value", dlA);
				dle.addContent(a);
			}
		}
		return dle;
	}

	private void setDlStats () {
		DownloadStats ds = dl.getStats();
		name = dl.getName();
		hash = EncodingUtil.encode(dl.getTorrent().getHash());
		forceStart = dl.isForceStart();
		checking = dl.isChecking();
		complete = dl.isComplete();
		position = dl.getPosition();
		savePath = dl.getSavePath();
		downloaded = ds.getDownloaded();
		uploaded = ds.getUploaded();
		downloadAVG = ds.getDownloadAverage();
		uploadAVG = ds.getUploadAverage();
		totalAVG = ds.getTotalAverage();
		state = dl.getState();
		status = ds.getStatus();
		elapsedTime = ds.getElapsedTime();
		eta = ds.getETA();
		availability = ds.getAvailability();
		completition = ds.getDownloadCompleted(false);
		health = ds.getHealth();
		shareRatio = ds.getShareRatio();
		tracker = ds.getTrackerStatus();
		downloadLimit = dl.getMaximumDownloadKBPerSecond();
		uploadLimit = dl.getUploadRateLimitBytesPerSecond();

		if (dl.getState() == Download.ST_DOWNLOADING || dl.getState() == Download.ST_SEEDING) { //Stopped DLs don't have a PeerManager
			PeerManagerStats pms = dl.getPeerManager().getStats();
			seeds = pms.getConnectedSeeds();
			leecher = pms.getConnectedLeechers();
			discarded = pms.getDiscarded();
		}
		total_seeds = dl.getLastScrapeResult().getSeedCount();
		total_leecher = dl.getLastScrapeResult().getNonSeedCount();

		last_scrape = dl.getLastScrapeResult().getScrapeStartTime();
		next_scrape = dl.getLastScrapeResult().getNextScrapeStartTime();

		announceTTW = dl.getLastAnnounceResult().getTimeToWait();
		size = dl.getTorrent().getSize();

		for (TorrentAttribute ta : TA_LIST) {
			attributes.put(ta.getName(), dl.getAttribute(ta));
		}
	}

	public Download getDownload() {
		return dl;
	}
}
