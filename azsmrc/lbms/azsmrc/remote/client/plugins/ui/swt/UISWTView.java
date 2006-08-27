package lbms.azsmrc.remote.client.plugins.ui.swt;



public interface UISWTView {

	/**
	 * Retrieve the data sources related to this view.
	 *
	 * @return dependent upon subclasses implementation
	 */
	public Object getDataSource();

	/**
	 * ID of the view
	 *
	 * @return ID of the view
	 */
	public String getViewID();

	/**
	 * Closes the view
	 *
	 */
	public void closeView();


	/**
	 * Trigger an event for this view
	 *
	 * @param eventType  Event to trigger
	 * @param data data to send with trigger
	 *
	 */
	public void triggerEvent(int eventType, Object data);



	/**
	 * Override the default title with a new one.
	 *
	 * @param title new Title
	 *
	 */
	public void setTitle(String title);

}
