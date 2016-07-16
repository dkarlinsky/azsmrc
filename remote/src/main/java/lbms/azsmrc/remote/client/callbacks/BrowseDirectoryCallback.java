/**
 * 
 */
package lbms.azsmrc.remote.client.callbacks;

/**
 * @author Leonard
 * 
 */
public interface BrowseDirectoryCallback extends GenericCallback {
	public void subdirList (String parentDir, String[] subdirs);

	public void errorOccured (String msg);
}
