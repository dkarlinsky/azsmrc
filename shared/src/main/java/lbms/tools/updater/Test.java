package lbms.tools.updater;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Version v1,v2;
		v1 = new Version("2.02.1");
		v2 = new Version("1.5");
		int result = v1.compareTo(v2);
		System.out.println("Ergebnis: "+result);
	}

}
