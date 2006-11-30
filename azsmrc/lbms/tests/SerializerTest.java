package lbms.tests;

import java.awt.Point;

import junit.framework.TestCase;
import lbms.azsmrc.shared.Serializer;

/**
 * @author Damokles
 *
 */
public class SerializerTest extends TestCase {


	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeObject(java.io.Serializable)}.
	 */
	public void testSerializePoint() {
		Point x = new Point (1,2);
		try {
			Point y = (Point)Serializer.deserializeObject(Serializer.serializeObject(x));
			if (x.x != y.x || x.y != y.y) fail ("Object mismatch");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testSerializePointArray() {
		Point[] p = new Point[] {new Point (1,2),new Point (6,98),new Point (11,6),new Point (61,23),new Point (12,2)};
		try {
			Point[] q = (Point[])Serializer.deserializeObject(Serializer.serializeObject(p));
			for (int i = 0; i<p.length;i++)
				if (p[i].x != q[i].x || p[i].y != q[i].y) fail ("Object mismatch");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
