package lbms.tools.anidb;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.crypto.Cipher;

import lbms.tools.anidb.AniDBService.STATE;

/**
 * @author Damokles
 *
 */
public class AniDBResponseManager implements Runnable {

	private final int BUFFER_SIZE = 1536;

	private AniDBService main;

	private HashMap<MessageCode, AniDBResponseHandler> handlers = new HashMap<MessageCode, AniDBResponseHandler>();

	private boolean run = true;

	private Cipher cipher;


	private void init () {
		try {
			/*responseSocket = new DatagramSocket();
			responseSocket.setReuseAddress(true);
			InetSocketAddress sa = new InetSocketAddress(main.getConfig().getPropertyAsInt("AniDBService.listenport", 49011));
			responseSocket.bind(sa);*/
			run = true;
			cipher = Cipher.getInstance("AES");
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (run) {
			try {
				byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
				main.getSocket().receive(packet);

				int length = packet.getLength();

				//encryption
				if(main.getEncryptionKey()!=null) try {
					cipher.init(Cipher.DECRYPT_MODE, main.getEncryptionKey());
					buffer = cipher.doFinal(buffer, 0, length);
					length = buffer.length;
				}catch(Exception e) {
					e.printStackTrace();
					//Systen("! Decryption failed: "+e.getMessage());
					//m_key = null;
					//m_cip = null;
				}

				//compressed buffer
				if(buffer.length>1 && buffer[0]==0 && buffer[1]==0) try {
					Inflater dec = new Inflater();
					dec.setInput(buffer, 2, length-2);
					byte[] result = new byte[length*3];
					length = dec.inflate(result);
					dec.end();
					buffer = result;
				}catch(DataFormatException e){
					e.printStackTrace();
				}

				byte[] raw = new byte[length];
				System.arraycopy(buffer, 0, raw, 0, length);
				String rs = new String(raw, "UTF8");
				rs = rs.substring(0, rs.length()-1);
				System.out.println("AniDB RawData: "+rs);
				String[] results = rs.split("<br />");
				for (String s:results) {
					System.out.println("AniDB SplitData: "+s);
					String tag = null;
					if (s.startsWith("t")) {
						tag = s.substring(0, 4);
						s = s.substring(5); //Allways space after tag
						System.out.println("AniDB Tag: "+tag);
					}
					MessageCode code = MessageCode.getTagByCode(Integer.parseInt(s.substring(0, 3)));
					System.out.println("AniDB Code: "+code);
					s = s.substring(4);
					if (tag != null) {
						AniDBResponseHandler handler = main.getTagResponseListener(tag);
						if (handler != null) {
							handler.handleResponse(code, s);
						} else {
							System.out.println("AniDB TagHandler was null for: "+tag+" "+code);
						}
					}
					if (handlers.containsKey(code)) {
						handlers.get(code).handleResponse(code, s);
					} else {
						System.out.println("AniDB No Handler for: "+code);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected AniDBResponseManager (AniDBService mainServ) {
		this.main = mainServ;
		init();
		handlers.put(MessageCode.INVALID_CODE, new AniDBResponseHandler() {
			/* (non-Javadoc)
			 * @see lbms.tools.anidb.AniDBResponseHandler#handleResponse(lbms.tools.anidb.MessageCode, java.lang.String)
			 */
			public void handleResponse(MessageCode code, String data) {
				//custom function to indicate an invalid code
			}
		});
		handlers.put(MessageCode.ENCRYPTION_ENABLED, new AniDBResponseHandler() {
			/* (non-Javadoc)
			 * @see lbms.tools.anidb.AniDBResponseHandler#handleResponse(lbms.tools.anidb.MessageCode, java.lang.String)
			 */
			public void handleResponse(MessageCode code, String data) {
				//209 {str salt} ENCRYPTION ENABLED
				String salt = data.substring(0, data.indexOf(' '));
				System.out.println("Encryption Salt is: "+salt);
				main.setSessionKey(salt);
				main.getClient().sendAuth();
			}
		});
		handlers.put(MessageCode.LOGIN_ACCEPTED, new AniDBResponseHandler() {
			/* (non-Javadoc)
			 * @see lbms.tools.anidb.AniDBResponseHandler#handleResponse(lbms.tools.anidb.MessageCode, java.lang.String)
			 */
			public void handleResponse(MessageCode code, String data) {
				//200 {str session_key} LOGIN ACCEPTED
				String skey = data.substring(0, data.indexOf(' '));
				System.out.println("Session key is: "+skey);
				main.setSessionKey(skey);
				main.setState(STATE.READY);
			}
		});
		handlers.put(MessageCode.LOGIN_ACCEPTED_NEW_VER, new AniDBResponseHandler() {
			/* (non-Javadoc)
			 * @see lbms.tools.anidb.AniDBResponseHandler#handleResponse(lbms.tools.anidb.MessageCode, java.lang.String)
			 */
			public void handleResponse(MessageCode code, String data) {
				//201 {str session_key} LOGIN ACCEPTED - NEW VERSION AVAILABLE
				String skey = data.substring(0, data.indexOf(' '));
				System.out.println("Session key is: "+skey);
				main.setSessionKey(skey);
				main.setState(STATE.READY);
			}
		});
		handlers.put(MessageCode.LOGGED_OUT, new AniDBResponseHandler() {
			/* (non-Javadoc)
			 * @see lbms.tools.anidb.AniDBResponseHandler#handleResponse(lbms.tools.anidb.MessageCode, java.lang.String)
			 */
			public void handleResponse(MessageCode code, String data) {
				//203 LOGGED OUT
				main.setState(STATE.NOT_CONNECTED);
			}
		});
		handlers.put(MessageCode.NOT_LOGGED_IN, new AniDBResponseHandler() {
			/* (non-Javadoc)
			 * @see lbms.tools.anidb.AniDBResponseHandler#handleResponse(lbms.tools.anidb.MessageCode, java.lang.String)
			 */
			public void handleResponse(MessageCode code, String data) {
				//403 NOT LOGGED IN
				main.setState(STATE.NOT_CONNECTED);
			}
		});
		handlers.put(MessageCode.PONG, new AniDBResponseHandler() {
			/* (non-Javadoc)
			 * @see lbms.tools.anidb.AniDBResponseHandler#handleResponse(lbms.tools.anidb.MessageCode, java.lang.String)
			 */
			public void handleResponse(MessageCode code, String data) {
				//300 PONG
				System.out.println("Pong received");
			}
		});
	}
}
