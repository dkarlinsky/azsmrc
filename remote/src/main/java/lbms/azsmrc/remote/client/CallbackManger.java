/**
 * 
 */
package lbms.azsmrc.remote.client;

import lbms.azsmrc.remote.client.callbacks.GenericCallback;

/**
 * @author Leonard
 *
 */
public interface CallbackManger {

	public abstract String addCallback (GenericCallback cb);

	public abstract GenericCallback peek (String cbID);

	public abstract GenericCallback removeCallback (String cbID);

}