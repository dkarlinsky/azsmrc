package lbms.tests;

import java.awt.Point;
import java.io.InvalidObjectException;

import junit.framework.TestCase;
import lbms.azsmrc.shared.Serializer;

/**
 * @author Damokles
 *
 */
public class SerializerTest extends TestCase {

	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeArray(java.lang.String[])}.
	 */
	public void testSerializeArrayStringArray() {
		String [] x = new String [] {"dsflskdfs","skdasds","jkcvxjue","\"ksda''asdas","cxmlaäü+ä.0ß´@@"};
		try {
			String [] y = Serializer.deserializeStringArray(Serializer.serializeArray(x));

			if (x.length != y.length) fail("Size mismatches");

			for (int i = 0; i<x.length;i++) {
				if (!x[i].equals(y[i])) fail ("Entries mismatch");
			}
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
	}

	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeArray(int[])}.
	 */
	public void testSerializeArrayIntArray() {
		int [] x = new int [] {12,1335,-124,8,95,3,895,31,8,531,46,478,0,-3,4,-646,4523};
		try {
			int [] y = Serializer.deserializeIntArray(Serializer.serializeArray(x));

			if (x.length != y.length) fail("Size mismatches");

			for (int i = 0; i<x.length;i++) {
				if (x[i] != (y[i])) fail ("Entries mismatch");
			}
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
	}

	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeArray(boolean[])}.
	 */
	public void testSerializeArrayBooleanArray() {
		boolean [] x = new boolean [] {false,true,true,false,true,true,false,false,false,true};
		try {
			boolean [] y = Serializer.deserializeBooleanArray(Serializer.serializeArray(x));

			if (x.length != y.length) fail("Size mismatches");

			for (int i = 0; i<x.length;i++) {
				if (x[i] != (y[i])) fail ("Entries mismatch");
			}
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
	}

	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeArray(float[])}.
	 */
	public void testSerializeArrayFloatArray() {
		float [] x = new float [] {-12.3f,1335,124,-8.34534f,95,3,895,31,8,-531,46,478,0,3,4,646,4523};
		try {
			float [] y = Serializer.deserializeFloatArray(Serializer.serializeArray(x));

			if (x.length != y.length) fail("Size mismatches");

			for (int i = 0; i<x.length;i++) {
				if (x[i] != (y[i])) fail ("Entries mismatch");
			}
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
	}

	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeArray(long[])}.
	 */
	public void testSerializeArrayLongArray() {
		long [] x = new long [] {12,1335,-124,8,95,3,-895,31,8,531,46,478,0,3,4,646,4523};
		try {
			long [] y = Serializer.deserializeLongArray(Serializer.serializeArray(x));

			if (x.length != y.length) fail("Size mismatches");

			for (int i = 0; i<x.length;i++) {
				if (x[i] != (y[i])) fail ("Entries mismatch");
			}
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
	}

	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeArray(double[])}.
	 */
	public void testSerializeArrayDoubleArray() {
		double [] x = new double [] {12.2,1335.5645,124.675,8.456,-95,3.65,895,31,8,531,46,478,0,3,4,646,-4523.21214};
		try {
			double [] y = Serializer.deserializeDoubleArray(Serializer.serializeArray(x));

			if (x.length != y.length) fail("Size mismatches");

			for (int i = 0; i<x.length;i++) {
				if (x[i] != (y[i])) fail ("Entries mismatch");
			}
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			fail (e.getMessage());
		}
	}

	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#serializeObject(java.io.Serializable)}.
	 */
	public void testSerializeObject() {
		Point x = new Point (1,2);
		try {
			Point y = (Point)Serializer.deserializeObject(Serializer.serializeObject(x));
			if (x.x != y.x || x.y != y.y) fail ("Object mismatch");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


	/**
	 * Test method for {@link lbms.azsmrc.shared.Serializer#deserialzeArrayObject(org.jdom.Element)}.
	 */
	public void testDeserialzeArrayObject() {
		int[] i 	= new int [] {12,1335,-124,8,95,3,895,31,8,531,46,478,0,-3,4,-646,4523};
		boolean[] b = new boolean [] {false,true,true,false,true,true,false,false,false,true,true,false,false,true,true,true,false};
		float[] f 	= new float [] {-12.3f,1335,124,-8.34534f,95,3,895,31,8,-531,46,478,0,3,4,646,4523};
		long[] l 	= new long [] {12,1335,-124,8,95,3,-895,31,8,531,46,478,0,3,4,646,4523};
		double[] d 	= new double [] {12.2,1335.5645,124.675,8.456,-95,3.65,895,31,8,531,46,478,0,3,4,646,-4523.21214};

		try {
			int[] it 		= (int[])Serializer.deserialzeArrayObject(Serializer.serializeArray(i));
			boolean[] bt 	= (boolean[])Serializer.deserialzeArrayObject(Serializer.serializeArray(b));
			float[] ft 		= (float[])Serializer.deserialzeArrayObject(Serializer.serializeArray(f));
			long[] lt 		= (long[])Serializer.deserialzeArrayObject(Serializer.serializeArray(l));
			double[] dt 	= (double[])Serializer.deserialzeArrayObject(Serializer.serializeArray(d));
			for (int x = 0; x<i.length;x++) {
				if (i[x] != (it[x])) fail ("Int Entries mismatch");
				if (b[x] != (bt[x])) fail ("Boolean Entries mismatch");
				if (f[x] != (ft[x])) fail ("Float Entries mismatch");
				if (l[x] != (lt[x])) fail ("Long Entries mismatch");
				if (d[x] != (dt[x])) fail ("Double Entries mismatch");
			}
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
