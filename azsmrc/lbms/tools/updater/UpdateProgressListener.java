/**
 * 
 */
package lbms.tools.updater;

/**
 * @author Damokles
 *
 */
public interface UpdateProgressListener {

	public static final int STATE_INITIALIZING	= 0;
	public static final int STATE_DOWNLOADING	= 1;
	public static final int STATE_INSTALLING	= 2;
	public static final int STATE_FINISHED		= 3;
	public static final int STATE_ERROR			= -1;
	public static final int STATE_ABORTED		= -2;

	public void stateChanged (int state);
}
