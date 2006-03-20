package lbms.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SFDownload extends Download {

	private static Pattern mirrorPattern = Pattern.compile("<td><a href=\"[\\w/:.]+?\\?use_mirror=(\\w+)\"><b>Download</b></a></td>");
	private List<URL> mirrors = new ArrayList<URL>();
	private boolean finished = false;
	private boolean failed = false;

	public SFDownload(URL source, File target) {
		super(source, target);
		// TODO Auto-generated constructor stub
	}

	public SFDownload(URL source) {
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
		HTTPDownload sfContent = new HTTPDownload(source);
		try {
			sfContent.call();
		} catch (Exception e1) {
			e1.printStackTrace();
			throw e1;
		}
		if (sfContent.isFailed() || !sfContent.isFinished()) {
			System.out.println("Couldn't load mirrors");
			throw new Exception ("Error occured: Couldn't load mirrors");
		}
		Matcher sfMirror = mirrorPattern.matcher(sfContent.getBuffer());
		String fileLocation = source.getPath();
		//Find mirrors
		while (sfMirror.find()) {
			String mirrorId = sfMirror.group(1);
			try {
				mirrors.add(new URL("http://"+mirrorId+".dl.sourceforge.net/sourceforge"+fileLocation));
				//System.out.println("SF.net Mirror: http://"+mirrorId+".dl.sourceforge.net/sourceforge"+fileLocation);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		//Downloading file
		for (URL x:mirrors) {
			try {
				//System.out.println("Trying: "+x.toExternalForm());
				HTTPDownload file = new HTTPDownload(x,target);
				file.call();
				if (file.isFailed() || !file.isFinished()) continue;
				else return this;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//I hope we are not getting here
		System.out.println("Couldn't Download file");
		throw new Exception ("Couldn't Download file");
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
