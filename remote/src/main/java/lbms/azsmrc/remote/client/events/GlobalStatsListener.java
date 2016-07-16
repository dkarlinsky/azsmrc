package lbms.azsmrc.remote.client.events;

public interface GlobalStatsListener {
	public void updateStats(int d, int u, int seeding, int seedqueue, int downloading, int downloadqueue);
}
