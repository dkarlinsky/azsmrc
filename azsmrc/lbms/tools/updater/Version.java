package lbms.tools.updater;

public class Version implements Comparable<Version> {
	private int[] version = new int[] { 0 };

	public Version(String ver) {
		String[] tmp = ver.split("\\.");
		this.version = new int[tmp.length];

		for (int i = 0; i < tmp.length; i++) {
			try {
				this.version[i] = Integer.parseInt(tmp[i]);
			} catch (NumberFormatException e) {
				this.version[i] = 0;
			}
		}

	}

	public int compareTo(Version o) {
		boolean smaller = false;
		int length = 0;
		if (version.length < o.version.length) {
			smaller = true;
			length = version.length;
		} else {
			length = o.version.length;
		}
		for (int i = 0; i < length; i++) {
			if (version[i] == o.version[i]) {
				continue;
			} else {
				return version[i] - o.version[i];
			}
		}
		if (smaller) {
			return -1;
		} else if (version.length == o.version.length) {
			return 0;
		} else {
			return 1;
		}
	}

	public int compareTo(String o) {
		return compareTo(new Version(o));
	}

	@Override
	public String toString() {
		String out = "";
		for (int i = 0; i < version.length - 1; i++) {
			out += version[i] + ".";
		}
		out += version[version.length - 1];
		return out;
	}
}
