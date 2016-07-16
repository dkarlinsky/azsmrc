package lbms.azsmrc.remote.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;



public class SESecurityManager {
	
	protected static SESecurityManager	singleton = new SESecurityManager();

	public static final String SSL_PASSWORD 	= "changeit";
	
	protected String	truststore_name;
	
	private List<SESecurityManagerListener> listener = new ArrayList<SESecurityManagerListener>();
	
	protected static String	KEYSTORE_TYPE;

	private SESecurityManager () {
		{
			String[]	types = { "JKS", "GKR" };
			
			for (int i=0;i<types.length;i++){
				try{
					KeyStore.getInstance( types[i] );
					
					KEYSTORE_TYPE	= types[i];
					
					break;
					
				}catch( Throwable e ){
				}
			}
			
			if ( KEYSTORE_TYPE == null ){
				
					// it'll fail later but we need to use something here
				
				KEYSTORE_TYPE	= "JKS";
			}
		}
		truststore_name = new File(System.getProperty("user.dir"), ".certs").getAbsolutePath();
		ensureStoreExists(truststore_name);
	}
	
	public static SESecurityManager getSingleton()
	{
		return( singleton );
	}
	
	protected boolean
	ensureStoreExists(
		String	name )
	{
		try{
			KeyStore keystore = KeyStore.getInstance( KEYSTORE_TYPE );
			
			if ( !new File(name).exists()){
		
				keystore.load(null,null);
			
				FileOutputStream	out = null;
				
				try{
					out = new FileOutputStream(name);
			
					keystore.store(out, SSL_PASSWORD.toCharArray());
			
				}finally{
					
					if ( out != null ){
						
						out.close();
					}						
				}
				
				return( true );
				
			}else{
				
				return( false );
			}
		}catch( Throwable e ){
			
			e.printStackTrace();
			
			return( false );
			
		}
	}
	
	public KeyStore
	getTrustStore()
	
		throws Exception
	{
		KeyStore keystore = KeyStore.getInstance( KEYSTORE_TYPE );
		
		if ( !new File(truststore_name).exists()){
	
			keystore.load(null,null);
			
		}else{
		
			FileInputStream in 	= null;

			try{
				in = new FileInputStream(truststore_name);
		
				keystore.load(in, SSL_PASSWORD.toCharArray());
				
			}finally{
				
				if ( in != null ){
					
					in.close();
				}
			}
		}
		
		return( keystore );
	}
	
	
	public SSLSocketFactory
	installServerCertificates(
		URL		https_url )
	{
		try{
		
			String	host	= https_url.getHost();
			int		port	= https_url.getPort();
			
			if ( port == -1 ){
				port = 443;
			}
			
			SSLSocket	socket = null;
			
			try{
		
					// to get the server certs we have to use an "all trusting" trust manager
				
				TrustManager[] trustAllCerts = new TrustManager[]{
							new X509TrustManager() {
								public java.security.cert.X509Certificate[] getAcceptedIssuers() {
									return null;
								}
								public void checkClientTrusted(
										java.security.cert.X509Certificate[] certs, String authType) {
								}
								public void checkServerTrusted(
										java.security.cert.X509Certificate[] certs, String authType) {
								}
							}
						};
				
				SSLContext sc = SSLContext.getInstance("SSL");
				
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				
				SSLSocketFactory factory = sc.getSocketFactory();
						
				socket = (SSLSocket)factory.createSocket(host, port);
			
				socket.startHandshake();
				
				java.security.cert.Certificate[] serverCerts = socket.getSession().getPeerCertificates();
				
				if ( serverCerts.length == 0 ){
									
					return( null );
				}
				
				java.security.cert.Certificate	cert = serverCerts[0];
							
				java.security.cert.X509Certificate x509_cert;
				
				if ( cert instanceof java.security.cert.X509Certificate ){
					
					x509_cert = (java.security.cert.X509Certificate)cert;
					
				}else{
					
					java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
					
					x509_cert = (java.security.cert.X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
				}
					
				String	resource = https_url.toString();
				
				int	param_pos = resource.indexOf("?");
				
				if ( param_pos != -1 ){
					
					resource = resource.substring(0,param_pos);
				}
				
				for (SESecurityManagerListener l:listener){
					
					if (l.trustCertificate( resource, x509_cert )){
						
						String	alias = host.concat(":").concat(String.valueOf(port));
				
						return( addCertToTrustStore( alias, cert ));
					}
				}
				
				return( null );
				
			}catch( Throwable e ){
				
				e.printStackTrace(  );
				
				return( null );
				
			}finally{
				
				if ( socket != null ){
					
					try{
						socket.close();
						
					}catch( Throwable e ){
						
						e.printStackTrace(  );
					}
				}
			}
		}finally{
			
		}
	}
	
	protected SSLSocketFactory
	addCertToTrustStore(
		String							alias,
		java.security.cert.Certificate	cert )
	
		throws Exception
	{
		try{
		
			KeyStore keystore = getTrustStore();
			
			if ( cert != null ){
				
				if ( keystore.containsAlias( alias )){
				
					keystore.deleteEntry( alias );
				}
							
				keystore.setCertificateEntry(alias, cert);
	
				FileOutputStream	out = null;
				
				try{
					out = new FileOutputStream(truststore_name);
			
					keystore.store(out, SSL_PASSWORD.toCharArray());
			
				}finally{
					
					if ( out != null ){
						
						out.close();
					}						
				}
			}
			
				// pick up the changed trust store
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			
			tmf.init(keystore);
			
			SSLContext ctx = SSLContext.getInstance("SSL");
			
			ctx.init(null, tmf.getTrustManagers(), null);
						
			SSLSocketFactory	factory = ctx.getSocketFactory();
			
			HttpsURLConnection.setDefaultSSLSocketFactory( factory );
			
			return( factory );
		}finally{
			
		}
	}
	
	public void addSecurityListner(SESecurityManagerListener l) {
		listener.add(l);
	}
	
	public void removeSecurityListner(SESecurityManagerListener l) {
		listener.remove(l);
	}
}
