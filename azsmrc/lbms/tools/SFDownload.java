package lbms.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SFDownload extends Download {

	private static Pattern mirrorPattern = Pattern.compile("<td><a href=\"[\\w/:.]+?\\?use_mirror=(\\w+)\"><b>Download</b></a></td>");
	private List<URL> mirrors = new ArrayList<URL>();

	public SFDownload(URL source, File target) {
		super(source, target);
		// TODO Auto-generated constructor stub
	}

	public SFDownload(URL source) {
		throw new UnsupportedOperationException("This Download needs a target.");
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
			callStateChanged(STATE_INITIALIZING);
			sfContent.call();
		} catch (Exception e1) {
			e1.printStackTrace();
			callStateChanged(STATE_FAILURE);
			failureReason = e1.getMessage();
			failed = true;
			throw e1;
		}
		if (sfContent.hasFailed() || !sfContent.hasFinished()) {
			callStateChanged(STATE_FAILURE);
			failureReason = "Couldn't load mirrors";
			failed = true;
			System.out.println(failureReason);
			throw new Exception ("SFDownload Error occured:"+failureReason);
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
				if (file.hasFailed() || !file.hasFinished()) continue;
				else {
					callStateChanged(STATE_FINISHED);
					return this;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//I hope we are not getting here
		callStateChanged(STATE_FAILURE);
		failureReason = "Couldn't Download file";
		failed = true;
		System.out.println(failureReason);
		throw new Exception (failureReason);
	}
}
