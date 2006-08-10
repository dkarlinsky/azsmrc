/**
 * 
 */
package lbms.azsmrc.remote.client.plugins;

import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public interface PluginClient {

	public Element getSendElement();

	public void sendCustomRequest (Element e);
}
