package lbms.tools.anidb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import lbms.tools.anidb.AniDBService.STATE;

/**
 * @author Damokles
 *
 */
public class AniDBClient extends Thread {

	private static int FLOOD_DELAY = 2000;
	private static int PROTOCOL_VERSION = 3;
	private static int CLIENT_VERSION = 1;
	private static String CLIENT_NAME = "janidblib"; //JAniDBLib

	private AniDBService main;
	private InetAddress aniDBaddress;
	private final int ANIDB_PORT = 9000;
	private long lastConnection;

	private int tagCount;
	private Cipher cipher;


	private LinkedBlockingQueue<DatagramPacket> packetQueue = new LinkedBlockingQueue<DatagramPacket>();

	protected AniDBClient (AniDBService main) {
		this.main = main;
		init();
		try {
			cipher = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
		setDaemon(true);
		setPriority(MIN_PRIORITY);
		start();
	}

	@Override
	public void run() {
		DatagramPacket p;
		while (true) {
			try {
				p = packetQueue.take();
				if (System.currentTimeMillis()-lastConnection < FLOOD_DELAY) {
					sleep(FLOOD_DELAY-(System.currentTimeMillis()-lastConnection));
				}
				if (p != null)
					send(p);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void init () {
		try {
			aniDBaddress = InetAddress.getByName("api.anidb.info");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void enqueue (String command, String params) {
		try {
			String s = command+" s="+main.getSessionKey()+((params==null)? "" : "+&"+params);
			byte[] raw = s.getBytes("UTF8");
			if (main.getEncryptionKey() != null) {
				cipher.init(Cipher.ENCRYPT_MODE, main.getEncryptionKey());
				raw = cipher.doFinal(raw);
			}
			DatagramPacket p = new DatagramPacket(raw,raw.length);
			packetQueue.add(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void send (DatagramPacket packet) {
		try {
			packet.setAddress(aniDBaddress);
			packet.setPort(ANIDB_PORT);
			main.getSocket().send(packet);
			lastConnection = System.currentTimeMillis();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getTag(AniDBResponseHandler listener) {
		if (listener != null) {
			String tag;
			//tags start allways with t and have 3 digits: tddd
			if (++tagCount>999) {
				tagCount = 1; // reset tag if it gets to large
			}

			if (tagCount > 99) { //pad
				tag = "t"+(tagCount);
			} else if (tagCount > 9) {
				tag = "t0"+(tagCount);
			} else {
				tag = "t00"+(tagCount);
			}
			main.addTagResponseListener(tag, listener);
			return "&tag="+tag;
		}
		return "";
	}

	//	---------------------------

	protected void sendAuth () {
		//AUTH user={str username}&pass={str password}&protover={int4 apiversion}&client={str clientname}&clientver={int4 clientversion}[&nat=1&comp=1&enc={str encoding}&mtu{int4 mtu value}]

		String username = main.getUsername();
		String pass = main.getPassword();

		if (!(main.getState().equals(STATE.INITIALISATION) || main.getState().equals(STATE.INIT_ENCRYPTION))) {
			throw new IllegalStateException("Authentication can only be in init or after encryption start.");
		}
		main.setState(STATE.AUTHENTICATING);

		try {
			String s = "AUTH user="+username+"&pass="+pass+"&protover="+PROTOCOL_VERSION+"&client="+CLIENT_NAME+"&clientver="+CLIENT_VERSION+"&comp=1&enc=UTF8";
			byte[] raw = s.getBytes("ASCII");
			DatagramPacket p = new DatagramPacket(raw,raw.length);
			packetQueue.add(p);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	protected void sendEncrypt () {
		//ENCRYPT user={str name}&type={int2 type}

		String username = main.getUsername();

		if (!main.getState().equals(STATE.INITIALISATION)) {
			throw new IllegalStateException("Encryption can only be done as first thing");
		}
		main.setState(STATE.INIT_ENCRYPTION);
		try {
			String s = "ENCRYPT user="+username+"&type=1";
			byte[] raw = s.getBytes("ASCII");
			DatagramPacket p = new DatagramPacket(raw,raw.length);
			packetQueue.add(p);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void sendPing () {
		try {
			String s = "PING";
			byte[] raw = s.getBytes("ASCII");
			DatagramPacket p = new DatagramPacket(raw,raw.length);
			packetQueue.add(p);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void sendPing (AniDBResponseHandler listener) {
		try {
			String s = "PING "+getTag(listener);
			byte[] raw = s.getBytes("ASCII");
			DatagramPacket p = new DatagramPacket(raw,raw.length);
			packetQueue.add(p);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	protected void sendLogout() {
		if (main.getState().equals(STATE.READY))
			enqueue("LOGOUT", null);
	}

	//---------------------------
}
