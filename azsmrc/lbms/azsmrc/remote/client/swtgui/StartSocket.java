package lbms.azsmrc.remote.client.swtgui;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import lbms.azsmrc.shared.RemoteConstants;

/**
 * @author Damokles, original: Olivier
 * 
 */

public class StartSocket {
	private final String[] args;

	public StartSocket(String _args[]) {
		this.args = _args;
	}


	/**
	 * Attempt to send args via socket connection.
	 * @return true if successful, false if connection attempt failed
	 */
	public boolean sendArgs() {
		Socket sck = null;
		PrintWriter pw = null;
		try {
			String msg = "StartSocket: passing startup args to already-running AzSMRC java process listening on [127.0.0.1: 49008]";
			System.out.println( msg );

			sck = new Socket("127.0.0.1", 49008);

			pw = new PrintWriter(new OutputStreamWriter(sck.getOutputStream(),RemoteConstants.DEFAULT_ENCODING));

			StringBuffer buffer = new StringBuffer(StartServer.ACCESS_STRING + ";args;");

			for(int i = 0 ; i < args.length ; i++) {
				String arg = args[i].replaceAll("&","&&").replaceAll(";","&;");
				buffer.append(arg);
				buffer.append(';');
			}

			System.out.println("Main::startSocket: sending '"
					+ buffer.toString() + "'");

			pw.println(buffer.toString());
			pw.flush();

			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;  //there was a problem connecting to the socket
		}
		finally {
			try {
				if (pw != null)  pw.close();
			}
			catch (Exception e) {}

			try {
				if (sck != null) 	sck.close();
			}
			catch (Exception e) {}
		}
	}


	public static void main(String args[]) {
		new StartSocket(args);
	}
}