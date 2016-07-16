package lbms.azsmrc.remote.client.util;

import java.util.concurrent.ThreadFactory;

public class TimerDeamonThreadFactory implements ThreadFactory {

	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}

}
