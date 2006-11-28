package lbms.azsmrc.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public class Serializer {

	public static Element serializeArray(String[] array) {
		Element e = new Element ("Array");
		e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_ARRAY_STRING));
		for (String s:array) {
			Element item = new Element ("Item");
			item.setText(s);
			e.addContent(item);
		}
		return e;
	}

	public static Element serializeArray(int[] array) {
		Element e = new Element ("Array");
		e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_ARRAY_INT));
		e.setText(Arrays.toString(array));
		return e;
	}

	public static Element serializeArray(boolean[] array) {
		Element e = new Element ("Array");
		e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_ARRAY_BOOLEAN));
		e.setText(Arrays.toString(array));
		return e;
	}

	public static Element serializeArray(float[] array) {
		Element e = new Element ("Array");
		e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_ARRAY_FLOAT));
		e.setText(Arrays.toString(array));
		return e;
	}

	public static Element serializeArray(long[] array) {
		Element e = new Element ("Array");
		e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_ARRAY_LONG));
		e.setText(Arrays.toString(array));
		return e;
	}

	public static Element serializeArray(double[] array) {
		Element e = new Element ("Array");
		e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_ARRAY_DOUBLE));
		e.setText(Arrays.toString(array));
		return e;
	}

	public static Element serializeObject (Serializable o) throws NotSerializableException {
		ObjectOutputStream oos = null;
		Element e = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos  =   new ObjectOutputStream( baos );
			oos.writeObject( o );

			e = new Element ("Object");
			e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_SERIALZED_OBJECT));
			e.setText(new String(new Base64().encode(baos.toByteArray())));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException ex) {}
		}

		return e;
	}

	public static Object deserializeObject (Element e) throws InvalidObjectException, ClassNotFoundException {
		if (e.getName().equalsIgnoreCase("Object")) {
			ObjectInputStream ois = null;
			Object o = null;
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(new Base64().decode(e.getTextTrim().getBytes()));
				ois = new ObjectInputStream (bis);
				o = ois.readObject();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e1) {}
				}
			}
			return o;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Object) was ("+e.getName()+")");
		}
	}

	public static String[] deserializeStringArray (Element e) throws InvalidObjectException {
		if (e.getName().equalsIgnoreCase("Array")) {
			List<Element> elems = e.getChildren("Item");
			String[] result = new String[elems.size()];
			for (int i=0;i < result.length;i++) {
				result[i] = elems.get(i).getTextTrim();
			}
			return result;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Array) was ("+e.getName()+")");
		}
	}

	public static int[] deserializeIntArray (Element e) throws InvalidObjectException {
		if (e.getName().equalsIgnoreCase("Array")) {
			String s = e.getTextTrim();
			s = s.substring(1, s.length()-1);
			String[] parts = s.split(",");
			int[] result = new int[parts.length];
			for (int i=0;i<parts.length;i++) {
				try{
					result[i] = Integer.parseInt(parts[i].trim());
				}catch (Exception ex){
					result[i] = 0;
				}
			}
			return result;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Array) was ("+e.getName()+")");
		}
	}

	public static boolean[] deserializeBooleanArray (Element e) throws InvalidObjectException {
		if (e.getName().equalsIgnoreCase("Array")) {
			String s = e.getTextTrim();
			s = s.substring(1, s.length()-1);
			String[] parts = s.split(",");
			boolean[] result = new boolean[parts.length];
			for (int i=0;i<parts.length;i++) {
				try{
					result[i] = Boolean.parseBoolean(parts[i].trim());
				}catch (Exception ex){
					result[i] = false;
				}
			}
			return result;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Array) was ("+e.getName()+")");
		}
	}

	public static float[] deserializeFloatArray (Element e) throws InvalidObjectException {
		if (e.getName().equalsIgnoreCase("Array")) {
			String s = e.getTextTrim();
			s = s.substring(1, s.length()-1);
			String[] parts = s.split(",");
			float[] result = new float[parts.length];
			for (int i=0;i<parts.length;i++) {
				try{
					result[i] = Float.parseFloat(parts[i].trim());
				}catch (Exception ex){
					result[i] = 0;
				}
			}
			return result;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Array) was ("+e.getName()+")");
		}
	}

	public static long[] deserializeLongArray (Element e) throws InvalidObjectException {
		if (e.getName().equalsIgnoreCase("Array")) {
			String s = e.getTextTrim();
			s = s.substring(1, s.length()-1);
			String[] parts = s.split(",");
			long[] result = new long[parts.length];
			for (int i=0;i<parts.length;i++) {
				try{
					result[i] = Long.parseLong(parts[i].trim());
				}catch (Exception ex){
					result[i] = 0;
				}
			}
			return result;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Array) was ("+e.getName()+")");
		}
	}

	public static double[] deserializeDoubleArray (Element e) throws InvalidObjectException {
		if (e.getName().equalsIgnoreCase("Array")) {
			String s = e.getTextTrim();
			s = s.substring(1, s.length()-1);
			String[] parts = s.split(",");
			double[] result = new double[parts.length];
			for (int i=0;i<parts.length;i++) {
				try{
					result[i] = Double.parseDouble(parts[i].trim());
				}catch (Exception ex){
					result[i] = 0;
				}
			}
			return result;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Array) was ("+e.getName()+")");
		}
	}

	public static Object deserialzeArrayObject (Element e) throws InvalidObjectException {
		if (e.getName().equalsIgnoreCase("Array")) {
			Object o = null;
			switch (Integer.parseInt(e.getAttributeValue("type"))) {
			case RemoteConstants.PARAMETER_ARRAY_STRING:
				o = deserializeStringArray(e);
				break;
			case RemoteConstants.PARAMETER_ARRAY_INT:
				o = deserializeIntArray(e);
				break;
			case RemoteConstants.PARAMETER_ARRAY_BOOLEAN:
				o = deserializeBooleanArray(e);
				break;
			case RemoteConstants.PARAMETER_ARRAY_FLOAT:
				o = deserializeFloatArray(e);
				break;
			case RemoteConstants.PARAMETER_ARRAY_LONG:
				o = deserializeLongArray(e);
				break;
			case RemoteConstants.PARAMETER_ARRAY_DOUBLE:
				o = deserializeDoubleArray(e);
				break;
			default:
				break;
			}
			return o;
		} else {
			throw new InvalidObjectException("Element had not correct type: expected (Array) was ("+e.getName()+")");
		}
	}

}
