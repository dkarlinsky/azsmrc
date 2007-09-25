package lbms.azsmrc.remote.client.history;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import lbms.azsmrc.remote.client.swtgui.RCMain;

import org.jdom.Element;

/**
 * @author Damokles
 * 
 */
public class DownloadHistory {

	private static DownloadHistory		instance;
	private Queue<ListenerWrapper>		listeners;

	private Set<DownloadHistoryEntry>	entries		= new TreeSet<DownloadHistoryEntry>();

	private long						startDate	= Long.MAX_VALUE;
	private long						endDate		= 0;

	private DownloadHistory() {
		listeners = new LinkedList<ListenerWrapper>();
	}

	public static DownloadHistory getInstance() {
		if (instance == null) {
			instance = new DownloadHistory();
		}
		return instance;
	}

	/**
	 * @param start timestamp in seconds
	 * @param end timestamp in seconds
	 * @param listener callback listener that will receive the result
	 */
	public void getEntries(long start, long end,
			DownloadHistoryListener listener) {
		boolean ecoMode = RCMain.getRCMain().getProperties().getPropertyAsBoolean(
				"downloadHistory.ecoMode");
		if (start >= startDate
				&& (end <= endDate || (ecoMode && end - 300 <= endDate))) {
			List<DownloadHistoryEntry> resultList = new ArrayList<DownloadHistoryEntry>();
			if (entries != null) {
				for (DownloadHistoryEntry dhe : entries) {
					if (start <= dhe.getTimestamp()
							&& end >= dhe.getTimestamp()) {
						resultList.add(dhe);
					}
				}
			}
			listener.updatedEntries(resultList.toArray(new DownloadHistoryEntry[resultList.size()]));
		} else {
			long newStart;
			long newEnd;
			if (startDate < start) {
				newStart = endDate;
			} else {
				startDate = start;
				newStart = start;
			}
			if (endDate > end) {
				newEnd = startDate;
			} else {
				endDate = end;
				newEnd = end;
			}
			listeners.add(new ListenerWrapper(start, end, listener));
			RCMain.getRCMain().getClient().sendGetDownloadHistory(newStart,
					newEnd);
		}
	}

	/**
	 * Explicitly forces an update on the Data
	 * 
	 * @param start timestamp in seconds
	 * @param end timestamp in seconds
	 * @param listener callback listener that will receive the result
	 */
	public void getEntriesForce(long start, long end,
			DownloadHistoryListener listener) {
		long newStart;
		long newEnd;
		if (startDate < start) {
			newStart = endDate;
		} else {
			startDate = start;
			newStart = start;
		}
		if (endDate > end) {
			newEnd = startDate;
		} else {
			endDate = end;
			newEnd = end;
		}
		listeners.add(new ListenerWrapper(start, end, listener));
		RCMain.getRCMain().getClient().sendGetDownloadHistory(newStart, newEnd);
	}

	/**
	 * Should only be invoked from ResponseManager
	 * 
	 * @param elements
	 */
	public void addEntries(List<Element> elements) {
		for (Element e : elements) {
			entries.add(new DownloadHistoryEntry(e));
		}
		ListenerWrapper repeatedElement = null;
		while (!listeners.isEmpty()) {
			ListenerWrapper listener = listeners.poll();
			if (listener == repeatedElement) {
				listeners.add(listener);
				break;
			}

			if (listener.start >= startDate && listener.end <= endDate) {
				List<DownloadHistoryEntry> resultList = new ArrayList<DownloadHistoryEntry>();
				if (entries != null) {
					for (DownloadHistoryEntry dhe : entries) {
						if (listener.start <= dhe.getTimestamp()
								&& listener.end >= dhe.getTimestamp()) {
							resultList.add(dhe);
						}
					}
				}
				listener.listener.updatedEntries(resultList.toArray(new DownloadHistoryEntry[resultList.size()]));

				listeners.remove(listener);
			} else {
				if (repeatedElement == null) {
					repeatedElement = listener;
				}
				listeners.add(listener);
			}
		}
	}

	private static class ListenerWrapper {
		long					start;
		long					end;
		DownloadHistoryListener	listener;

		public ListenerWrapper(long start, long end,
				DownloadHistoryListener listener) {
			super();
			this.start = start;
			this.end = end;
			this.listener = listener;
		}

	}
}
