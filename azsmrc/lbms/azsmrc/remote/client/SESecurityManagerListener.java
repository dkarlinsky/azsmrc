package lbms.azsmrc.remote.client;

import java.security.cert.X509Certificate;

public interface SESecurityManagerListener {
	public boolean trustCertificate(String ressource, X509Certificate x509_cert);
}
