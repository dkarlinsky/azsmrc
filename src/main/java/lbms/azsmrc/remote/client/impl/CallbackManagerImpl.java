/**
 * 
 */
package lbms.azsmrc.remote.client.impl;

import java.util.HashMap;
import java.util.Map;

import lbms.azsmrc.remote.client.CallbackManger;
import lbms.azsmrc.remote.client.callbacks.GenericCallback;

/**
 * @author Leonard
 * 
 */
public class CallbackManagerImpl implements CallbackManger {

	private Map<String, GenericCallback>	callbacks	= new HashMap<String, GenericCallback>();
	private int								id			= 1;

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.CallbackManger#addCallback(lbms.azsmrc.remote.client.callbacks.GenericCallback)
	 */
	public synchronized String addCallback (GenericCallback cb) {
		String cbID = Integer.toHexString(id++);
		callbacks.put(cbID, cb);
		return cbID;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.CallbackManger#peek(java.lang.String)
	 */
	public synchronized GenericCallback peek (String cbID) {
		return callbacks.get(cbID);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.CallbackManger#removeCallback(java.lang.String)
	 */
	public synchronized GenericCallback removeCallback (String cbID) {
		return callbacks.remove(cbID);
	}
}
