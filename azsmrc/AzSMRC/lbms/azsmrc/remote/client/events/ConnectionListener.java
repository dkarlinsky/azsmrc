package lbms.azsmrc.remote.client.events;

public interface ConnectionListener {
	public final static int ST_DISCONNECTED = 0;
	public final static int ST_CONNECTING = 1;
	public final static int ST_CONNECTED = 2;
	public void connectionState (int state);
}
