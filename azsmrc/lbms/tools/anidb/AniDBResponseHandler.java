package lbms.tools.anidb;


/**
 * @author Damokles
 *
 */
public interface AniDBResponseHandler {
	public void handleResponse (MessageCode code, String data);
}
