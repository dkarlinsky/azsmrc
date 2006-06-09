/**
 *
 */
package lbms.azsmrc.remote.client.swtgui.sound;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Damokles
 *
 */
public class SoundManager {

	private Map<Sound, Clip> soundBank = new HashMap<Sound, Clip>();
	private ArrayBlockingQueue<Clip> soundQueue = new ArrayBlockingQueue<Clip>(3);
	private Thread soundPlayerThread;
	private static SoundManager instance = new SoundManager();
	private boolean silentMode = false;

	private SoundManager () {
		soundPlayerThread = new Thread(new Runnable() {
			public void run() {
				Clip c = null;
				while (true) {
					try {
						if (c != null && c.isRunning() ) {
							Thread.sleep(100);
							continue;
						}
						c = soundQueue.take();
						c.setFramePosition(0);
						c.start();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		soundPlayerThread.setPriority(Thread.MIN_PRIORITY);
		soundPlayerThread.setDaemon(true);
		soundPlayerThread.start();
	}


	public static void playSound (Sound snd) {

		if (!instance.silentMode && instance.soundBank.containsKey(snd)) {
			if (instance.soundQueue.offer(instance.soundBank.get(snd))) {
				System.out.println("Added to Play Queue: "+snd);
			}
		}
	}

	public static void load(Sound key, File audioFile) throws SoundException {
		try {
			Clip c = instance.loadClip(audioFile);
			instance.soundBank.put(key, c);
		} catch (Exception e) {
			throw new SoundException(e);
		}
	}

	private Clip loadClip (File audioFile) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		Clip c = null;
		AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
		AudioFormat	format = ais.getFormat();
		DataLine.Info	info = new DataLine.Info(Clip.class, format);
		c = (Clip) AudioSystem.getLine(info);
		c.open(ais);
		return c;
	}

	public static void unLoad (Sound key) {
		if (instance.soundBank.containsKey(key)) {
			Clip c = instance.soundBank.get(key);
			instance.soundBank.remove(key);
			c.stop();
			c.close();
		}
	}

	public static void setSilentMode(boolean silent) {
		instance.silentMode = silent;
	}

	public static boolean isSilent() {
		return instance.silentMode;
	}
}
