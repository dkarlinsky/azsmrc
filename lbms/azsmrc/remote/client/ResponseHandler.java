package lbms.azsmrc.remote.client;

import java.io.IOException;

import lbms.azsmrc.main.User;

import org.jdom.*;

public interface ResponseHandler {
	public long handleRequest(Element xmlRequest) throws IOException;
}
