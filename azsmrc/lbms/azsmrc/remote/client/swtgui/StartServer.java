package lbms.azsmrc.remote.client.swtgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByFileDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByURLDialog;
import lbms.azsmrc.shared.RemoteConstants;


/**
 * @author Damokles, original: Olivier
 * 
 */
public class StartServer {

	public static final String ACCESS_STRING = "AzSMRC Start Server Access";
	private ServerSocket socket;
	private int state;

	private boolean bContinue;
	public static final int STATE_FAULTY = 0;
	public static final int STATE_LISTENING = 1;

	private List		queuedTorrents = new ArrayList();
	private boolean		coreStarted	= false;

	public StartServer() {
		try {
			socket = new ServerSocket(49008, 50, InetAddress.getByName("127.0.0.1"));

			state = STATE_LISTENING;

			System.out.println("StartServer: listening on "
					+ "127.0.0.1:49008 for passed torrent info");

		}catch (Throwable t) {
			state = STATE_FAULTY;
			String reason = t.getMessage() == null ? "<>" : t.getMessage();
			System.out.println("StartServer ERROR: unable" + " to bind to 127.0.0.1:49008 listening"
					+ " for passed torrent info: " + reason);
		}
	}

	public void	pollForConnections () {
		if ( socket != null ){
			Thread t = new Thread()	{
				public void
				run()
				{
					pollForConnectionsSupport();
				}
			};
			t.setDaemon(true);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	private void pollForConnectionsSupport () {
		bContinue = true;
		while (bContinue) {
			BufferedReader br = null;
			try {
				Socket sck = socket.accept();
				String address = sck.getInetAddress().getHostAddress();
				if (address.equals("localhost") || address.equals("127.0.0.1")) {
					br = new BufferedReader(new InputStreamReader(sck.getInputStream(),RemoteConstants.DEFAULT_ENCODING));
					String line = br.readLine();

					System.out.println("Main::startServer: received '"
							+ line + "'");

					if (line != null) {
						StringTokenizer st = new StringTokenizer(line, ";");
						int i = 0;
						if(st.countTokens() > 1) {
							String args[] = new String[st.countTokens() - 1];
							String checker = st.nextToken();
							if(checker.equals(ACCESS_STRING)) {

								String debug_str = "";

								while (st.hasMoreElements()) {
									String bit = st.nextToken().replaceAll("&;", ";").replaceAll("&&", "&");

									debug_str += (debug_str.length()==0?"":" ; ") + bit;

									args[i++] = bit;
								}

								System.out.println("Main::startServer: decoded to '" + debug_str + "'");

								processArgs(args);
							}
						}
					}
				}
				sck.close();

			}
			catch (Exception e) {
				if(!(e instanceof SocketException))
					e.printStackTrace();
				//bContinue = false;
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (Exception e) { /*ignore */
				}
			}
		}
	}


	private void processArgs(String args[]) {
		if (args.length < 1 || !args[0].equals( "args" )){
			return;
		}

		boolean	open	= true;

		for (int i = 1; i < args.length; i++) {

			String	arg = args[i];

			if ( i == 1 ){

				if ( arg.equalsIgnoreCase( "--closedown" )){

					RCMain.getRCMain().close();

					return;

				}else if ( arg.equalsIgnoreCase( "--open" )){

					continue;

				}
			}

			String file_name = arg;

			if( file_name.toUpperCase().startsWith( "HTTP:" ) || file_name.toUpperCase().startsWith( "MAGNET:" ) ) {

				System.out.println("StartServer: args[" + i
						+ "] handling as a URI: " + file_name);


			} else {

				try {
					File file = new File(file_name);

					if (!file.exists()) {

						throw (new Exception("File not found"));
					}

					file_name = file.getCanonicalPath();

					System.out.println("StartServer: file = " + file_name);

				} catch (Throwable e) {

					System.out.println(
							"Failed to access torrent file '" + file_name
							+ "'. Ensure sufficient temporary file space "
							+ "available (check browser cache usage).");
				}
			}

			boolean	queued = false;

			try {

				if (!coreStarted) {

					queuedTorrents.add( new Object[]{ file_name, new Boolean( open )});

					queued = true;
				}
			} finally {
			}

			if ( !queued ){

				handleFile( file_name, open );
			}
		}
	}

	public void openQueuedTorrents () {
		for (int i=0;i<queuedTorrents.size();i++){

			Object[]	entry = (Object[])queuedTorrents.get(i);

			String	file_name 	= (String)entry[0];
			boolean	open		= ((Boolean)entry[1]).booleanValue();

			handleFile(file_name, open);
		}
	}

	private void handleFile(String file_name, boolean open) {
		try {
			if ( open ){
				if( file_name.toUpperCase().startsWith( "HTTP:" ) || file_name.toUpperCase().startsWith( "MAGNET:" ) )
					OpenByURLDialog.openWithURL(file_name);
				else
					OpenByFileDialog.open(new String[] {file_name});

			} else {
				Client client = RCMain.getRCMain().getClient();
				if (client != null) {
					if( file_name.toUpperCase().startsWith( "HTTP:" ) || file_name.toUpperCase().startsWith( "MAGNET:" ) )
						client.getDownloadManager().addDownload(file_name);
					else
						client.getDownloadManager().addDownload(new File(file_name));
				}
			}
		} catch (Throwable e) {

			e.printStackTrace();
		}
	}

	/**
	 * @param core_started the core_started to set
	 */
	public void setCoreStarted(boolean core_started) {
		this.coreStarted = core_started;
	}

	/**
	 * @return
	 */
	public int getState() {
		return state;
	}
}
