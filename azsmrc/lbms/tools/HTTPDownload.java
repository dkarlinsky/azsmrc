package lbms.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

public class HTTPDownload extends Download  {

	private StringBuffer buffer;
	private boolean finished = false;
	private boolean failed = false;

	public HTTPDownload(URL source, File target) {
		super(source, target);
		// TODO Auto-generated constructor stub
	}

	public HTTPDownload(URL source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public void run() {
		try {
			call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Download call() throws Exception {
		InputStream is = null;
		try {
			HttpURLConnection conn = (HttpURLConnection)source.openConnection();
			conn.setDoInput(true);
			conn.addRequestProperty("Accept-Encoding","gzip");
			conn.connect();
			is =  conn.getInputStream();
			String encoding = conn.getHeaderField( "content-encoding");

		  	boolean	gzip = encoding != null && encoding.equalsIgnoreCase("gzip");		

		  	if ( gzip ){		  		
		  		is = new GZIPInputStream( is );
		  	}
			if (target != null) {
				target.createNewFile();
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(target);
					int r = is.read();
					while (r!=-1) {
						os.write((char)r);
						r = is.read();
					}
				} finally {
					if (os != null)
						os.close();
				}
			} else {
				buffer = new StringBuffer((conn.getContentLength()>0)?conn.getContentLength():1024);
				int r = is.read();
				while (r!=-1) {
					buffer.append((char)r);
					r = is.read();
				}
			}
			finished = true;
		} catch (IOException e) {
			failed = true;
			e.printStackTrace();
			throw e;
		} finally {
			if (is!= null)
				try {
					is.close();
				} catch (IOException e) {}
		}
		return this;
	}

	/**
	 * @return Returns the buffer.
	 */
	public StringBuffer getBuffer() {
		return buffer;
	}

	/**
	 * @return Returns the failed.
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * @return Returns the finished.
	 */
	public boolean isFinished() {
		return finished;
	}
}
