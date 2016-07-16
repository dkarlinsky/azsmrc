package lbms.tests;

import java.awt.Point;

import junit.framework.TestCase;
import lbms.azsmrc.shared.Serializer;

/**
 * @author Damokles
 *
 */
public class SerializerTest extends TestCase {

	public void testSerializePoint() {
		Point x = new Point (1,2);
		try {
			Point y = (Point)Serializer.deserializeObject(Serializer.serializeObjectToString(x));
			if (x.x != y.x || x.y != y.y) fail ("Object mismatch");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testSerializePointArray() {
		Point[] p = new Point[] {new Point (1,2),new Point (6,98),new Point (11,6),new Point (61,23),new Point (12,2)};
		try {
			Point[] q = (Point[])Serializer.deserializeObject(Serializer.serializeObjectToString(p));
			for (int i = 0; i<p.length;i++)
				if (p[i].x != q[i].x || p[i].y != q[i].y) fail ("Object mismatch");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeObject(java.io.Serializable)}.
	 */
	public void testSerializePointElement() {
		Point x = new Point (1,2);
		try {
			Point y = (Point)Serializer.deserializeObject(Serializer.serializeObjectToElement(x));
			if (x.x != y.x || x.y != y.y) fail ("Object mismatch");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testSerializePointArrayElement() {
		Point[] p = new Point[] {new Point (1,2),new Point (6,98),new Point (11,6),new Point (61,23),new Point (12,2)};
		try {
			Point[] q = (Point[])Serializer.deserializeObject(Serializer.serializeObjectToElement(p));
			for (int i = 0; i<p.length;i++)
				if (p[i].x != q[i].x || p[i].y != q[i].y) fail ("Object mismatch");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
