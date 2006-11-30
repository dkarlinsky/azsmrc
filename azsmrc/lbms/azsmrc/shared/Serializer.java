package lbms.azsmrc.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public class Serializer {

	public static Element serializeObject (Serializable o) throws NotSerializableException {
		ObjectOutputStream oos = null;
		Element e = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos  =   new ObjectOutputStream( baos );
			oos.writeObject( o );

			e = new Element ("Object");
			e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_SERIALZED_OBJECT));
			e.setAttribute("class", o.getClass().getCanonicalName());
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

	public static String getClassname (Element e) {
		return e.getAttributeValue("class");
	}
}
