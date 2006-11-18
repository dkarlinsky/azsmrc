package lbms.azsmrc.plugin.web;

import java.io.IOException;

import lbms.azsmrc.plugin.main.User;

import org.jdom.Element;

/**
 * @author Leonard
 *
 */
public interface RequestHandler {

	/**
	 * This function will handle xmlRequest and generate a response if
	 * necessary.
	 * 
	 * @param xmlRequest the XMLElment request
	 * @param response XMLElment representing the response root
	 * @param user the current user
	 * @return true if response should be sent, false if not
	 * @throws IOException
	 */
	public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException;
}
