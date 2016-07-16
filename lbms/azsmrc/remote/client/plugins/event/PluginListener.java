package lbms.azsmrc.remote.client.plugins.event;

/**
 * @author Damokles
 *
 */
public interface PluginListener {

	/**
	 * Event is fired it AzSMRC has started completly
	 */
	public void startupCompleted();

	/**
	 * Event is fired when AzSMRC is going to shut down
	 */
	public void shutdownInitiated();
}
