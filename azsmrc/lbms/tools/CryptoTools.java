package lbms.tools;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

	public static byte[] messageDigest( byte[] bytes, String algo ) throws NoSuchAlgorithmException   {
		MessageDigest messagedigest = MessageDigest.getInstance( algo );
		messagedigest.update(bytes);
		return messagedigest.digest();
	  }

	public static String formatByte (byte[] digest) {
		String hash = "";
		for ( byte d : digest )
			hash += Integer.toHexString( d & 0xFF);
		return hash;
	}
}
