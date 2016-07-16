package lbms.azsmrc.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class EncodingUtil {
	public static String encode (byte[] b) {
		return new String(new Base64().encode(b));
	}

	public static byte[] decode (String s) {
		return new Base64().decode(s.getBytes());
	}

	public static String IntArrayToString (int[] ar) {
		return Arrays.toString(ar);
	}

	public static String ObjectArrayToString (Object[] ar) {
		return Arrays.toString(ar);
	}

	public static int[] StringToIntArray (String s) {
			s = s.substring(1, s.length()-1);
			String[] parts = s.split(",");
			int[] result = new int[parts.length];
			for (int i=0;i<parts.length;i++) {
				try{
					result[i] = Integer.parseInt(parts[i].trim());
				}catch (Exception e){
					result[i] = 0;
				}
			}
			return result;
	}

	public static int[] StringToIntArray (String s, int def) {
		s = s.substring(1, s.length()-1);
		String[] parts = s.split(",");
		int[] result = new int[parts.length];
		for (int i=0;i<parts.length;i++) {
			try{
				result[i] = Integer.parseInt(parts[i].trim());
			}catch (Exception e){
				result[i] = def;
			}
		}
		return result;
}

	public static String IntListToString (List<Integer> list) {
		return ObjectArrayToString(list.toArray(new Integer[] {}));
	}

	public static List<Integer> StringToIntegerList (String s){
		int[] x = StringToIntArray(s);
		List<Integer> result = new ArrayList<Integer>();
		for (int e:x) {
			result.add(e);
		}
		return result;
	}



	  public static String
		nicePrint(
			byte[] data,
		boolean tight)
		{
		  if(data == null){
			return "";
		  }

		  String out = "";

		  for (int i = 0; i < data.length; i++) {

			  if ((!tight) && i > 0 && (i % 4 == 0)){

				  out = out + " ";
			  }

			  out = out + nicePrint(data[i]);
		  }

		  return( out );
		}

		public static String nicePrint(byte b) {
			byte b1 = (byte) ((b >> 4) & 0x0000000F);
			byte b2 = (byte) (b & 0x0000000F);
			return nicePrint2(b1) + nicePrint2(b2);
		  }

		public static String nicePrint2(byte b) {
			String out = "";
			switch (b) {
			  case 0 :
				out = "0";
				break;
			  case 1 :
				out = "1";
				break;
			  case 2 :
				out = "2";
				break;
			  case 3 :
				out = "3";
				break;
			  case 4 :
				out = "4";
				break;
			  case 5 :
				out = "5";
				break;
			  case 6 :
				out = "6";
				break;
			  case 7 :
				out = "7";
				break;
			  case 8 :
				out = "8";
				break;
			  case 9 :
				out = "9";
				break;
			  case 10 :
				out = "A";
				break;
			  case 11 :
				out = "B";
				break;
			  case 12 :
				out = "C";
				break;
			  case 13 :
				out = "D";
				break;
			  case 14 :
				out = "E";
				break;
			  case 15 :
				out = "F";
				break;
			}
			return out;
		  }

}
