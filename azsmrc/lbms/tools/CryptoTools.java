package lbms.tools;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class CryptoTools {

	public static byte[] messageDigestFile( String file, String algo ) throws Exception  {
		MessageDigest messagedigest = MessageDigest.getInstance( algo );
		byte[] md = new byte[8192];
		FileInputStream in = null;
		try {
			in = new FileInputStream( file );
			for ( int n = 0; (n = in.read( md )) > -1; )
			  messagedigest.update( md, 0, n );
			return messagedigest.digest();
		} finally {
			if (in!=null) in.close();
		}
	  }

	public static String formatByte (byte[] digest) {
		String hash = "";
		for ( byte d : digest )
			hash += Integer.toHexString( d & 0xFF);
		return hash;
	}
}
