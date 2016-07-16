package lbms.azsmrc.remote.client.events;

public interface ExceptionListener {
	/**
	 * @param e the Exception
	 * @param serious wheter the Exception is matters or not
	 */
	public void exceptionOccured (Exception e, boolean serious);	
}