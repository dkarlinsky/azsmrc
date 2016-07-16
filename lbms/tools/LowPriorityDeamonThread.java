package lbms.tools;

import java.util.concurrent.ThreadFactory;

public class LowPriorityDeamonThread implements ThreadFactory {

	public LowPriorityDeamonThread () {
		super();
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread (r);
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		return t;
	}
}