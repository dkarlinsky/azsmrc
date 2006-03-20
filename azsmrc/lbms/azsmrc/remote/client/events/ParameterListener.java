package lbms.azsmrc.remote.client.events;

public interface ParameterListener {
	public void azParameter (String key, String value, int type);
	public void pluginParameter (String key, String value, int type);
}
