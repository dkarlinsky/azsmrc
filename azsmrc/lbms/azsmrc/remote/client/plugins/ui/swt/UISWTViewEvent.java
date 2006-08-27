package lbms.azsmrc.remote.client.plugins.ui.swt;

public interface UISWTViewEvent {
	/**
	 * Triggered before view is initialize in order to allow any set up before
	 * initialization
	 * <p>
	 * This is the only time that setting {@link UISWTView#setControlType(int)}
	 * has any effect.
	 * <p>
	 * return true from {@link UISWTViewEventListener#eventOccurred(UISWTViewEvent)}
	 * if creation was successfull.  If you want only one instance of your view,
	 * or if there's any reason you can't create, return false, and an existing
	 * view will be used, if one is present.
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_CREATE = 0;

	/**
	 * Triggered when the datasource related to this view change.
	 * <p>
	 * Usually called after TYPE_CREATE, but before TYPE_INITIALIZE
	 * <p>
	 * getData() will return an Object[] array, or null
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_DATASOURCE_CHANGED = 1;

	/**
	 * Initialize your view.
	 * <p>
	 * getData() will return a SWT Composite or AWT Container for you to place
	 * object in.
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_INITIALIZE = 2;

	/**
	 * Focus Gained
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_FOCUSGAINED = 3;

	/**
	 * Focus Lost
	 * <p>
	 * TYPE_FOCUSLOST may not be called before TYPE_DESTROY
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_FOCUSLOST = 4;

	/** Triggered on user-specified intervals.  Plugins should update any
	 * live information at this time.
	 * <p>
	 * Caller is the GUI thread
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_REFRESH = 5;

	/** Language has changed.  Plugins should update their text to the new
	 * language.  To determine the new language, use Locale.getDefault()
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_LANGUAGEUPDATE = 6;

	/**
	 * Triggered when the parent view is about to be destroyed
	 * <p>
	 * TYPE_FOCUSLOST may not be called before TYPE_DESTROY
	 *
	 * @since 2.3.0.6
	 */
	public static final int TYPE_DESTROY = 7;

	/**
	 * Get the type.
	 *
	 * @return The TYPE_* constant for this event
	 *
	 * @since 2.3.0.6
	 */
	public int getType();

	/**
	 * Get the data
	 *
	 * @return Any data for this event
	 *
	 * @since 2.3.0.6
	 */
	public Object getData();

	/**
	 * Get the View
	 *
	 * @return Information and control over the view
	 *
	 * @since 2.3.0.6
	 */
	public UISWTView getView();
}
