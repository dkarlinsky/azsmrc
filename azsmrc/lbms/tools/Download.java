package lbms.tools;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;

public abstract class Download implements Runnable, Callable<Download> {

	public static int TIMEOUT = 15000;

	protected URL source;
	protected File target;
	protected String failureReason;
	protected boolean finished = false;
	protected boolean failed = false;

	public Download(URL source) {
		this.source = source;
	}

	public Download(URL source, File target) {
		this.source = source;
		this.target = target;
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
}
