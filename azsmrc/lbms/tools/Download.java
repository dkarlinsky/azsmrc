package lbms.tools;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

public abstract class Download implements Runnable, Callable<Download> {

	public static final int TIMEOUT = 30000;

	public static final int STATE_ABORTED 		= -2;
	public static final int STATE_FAILURE 		= -1;
	public static final int STATE_WAITING 		= 0;
	public static final int STATE_INITIALIZING 	= 1;
	public static final int STATE_CONNECTING 	= 2;
	public static final int STATE_DOWNLOADING 	= 3;
	public static final int STATE_FINISHED 		= 4;

	protected URL source;
	protected File target;
	protected String failureReason;
	protected boolean finished = false;
	protected boolean failed = false;
	protected List<DownloadListener> dlListener = new Vector<DownloadListener>();
	protected int state = 0;
	protected boolean abort = false;

	protected Download() {}

	public Download(URL source) {
		this.source = source;
	}

	public Download(URL source, File target) {
		this.source = source;
		this.target = target;
	}

	/**
	 * Tries to abort the Download
	 */
	public void abortDownload () {
		abort = true;
	}

	public String  getFailureReason() {
		return failureReason;
	}

	/**
	 * @return Returns the source.
	 */
	public URL getSource() {
		return source;
	}

	/**
	 * @param source The source to set.
	 */
	public void setSource(URL source) {
		this.source = source;
	}

	/**
	 * @return Returns the target.
	 */
	public File getTarget() {
		return target;
	}

	/**
	 * @param target The target to set.
	 */
	public void setTarget(File target) {
		this.target = target;
	}

	/**
	 * @return Returns the failed.
	 */
	public boolean hasFailed() {
		return failed;
	}

	/**
	 * @return Returns the finished.
	 */
	public boolean hasFinished() {
		return finished;
	}

	public void addDownloadListener (DownloadListener l) {
		dlListener.add(l);
	}

	public void removeDownloadListener (DownloadListener l) {
		dlListener.remove(l);
	}

	protected void callProgress (long bytesRead, long bytesTotal) {
		for (DownloadListener l:dlListener) {
			l.progress(bytesRead, bytesTotal);
		}
	}

	protected void callStateChanged (int newS) {
		for (DownloadListener l:dlListener) {
			l.stateChanged(state, newS);
		}
		state = newS;
	}
}
