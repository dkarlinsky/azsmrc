/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui;

/**
 * @author Damokles
 *
 */
public interface ErrorReporterListener {
	public void errorSubmitted (boolean submitted);
	public void redirectTo (String url);
	public void showText (String text);
}
