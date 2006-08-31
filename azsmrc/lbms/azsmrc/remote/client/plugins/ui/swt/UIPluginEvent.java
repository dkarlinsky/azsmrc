package lbms.azsmrc.remote.client.plugins.ui.swt;

/**
 * @author Damokles
 *
 */
public interface UIPluginEvent {

	/**
	 * Triggered before view is initialize in order to allow any set up before
	 * initialization
	 * <p>
	 * return true from {@link UISWTViewEventListener#eventOccurred(UISWTViewEvent)}
	 * if creation was successfull.  If you want only one instance of your view,
	 * or if there's any reason you can't create, return false, and an existing
	 * view will be used, if one is present.
	 * 
	 */
	public static final int TYPE_CREATE = 0;

	/**
	 * Initialize your view.
	 * <p>
	 * getData() will return a SWT Composite
	 */
	public static final int TYPE_INITIALIZE = 2;

	/**
	 * Focus Gained
	 * 
	 */
	public static final int TYPE_FOCUSGAINED = 3;

	/**
	 * Focus Lost
	 * <p>
	 * TYPE_FOCUSLOST may not be called before TYPE_DESTROY
	 * 
	 */
	public static final int TYPE_FOCUSLOST = 4;

	/**
	 * Triggered on user-specified intervals.  Plugins should update any
	 * live information at this time.
	 * <p>
	 * Caller is the GUI thread
	 * 
	 */
	public static final int TYPE_REFRESH = 5;

	public static final int TYPE_DATASOURCE_CHANGED = 6;

	/**
	 * Triggered when the parent view is about to be destroyed
	 * <p>
	 * TYPE_FOCUSLOST may not be called before TYPE_DESTROY
	 * 
	 */
	public static final int TYPE_DESTROY = 7;

	public int getType();

	public Object getData();

	public Object getDataSource();
}
