package lbms.tools;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;

public abstract class Download implements Runnable, Callable<Download> {
	protected URL source;
	protected File target;

	public Download(URL source) {
		this.source = source;
	}

	public Download(URL source, File target) {
		this.source = source;
		this.target = target;
	}
	public abstract boolean isFailed();
	public abstract boolean isFinished();

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
}
