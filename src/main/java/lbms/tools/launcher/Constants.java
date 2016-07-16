package lbms.tools.launcher;

public class Constants {

	public static final String  OSName = System.getProperty("os.name");

	public static final boolean isOSX				= OSName.equalsIgnoreCase("Mac OS X");
	public static final boolean isLinux			= OSName.equalsIgnoreCase("Linux");
	public static final boolean isSolaris			= OSName.equalsIgnoreCase("SunOS");
	public static final boolean isWindowsXP		= OSName.equalsIgnoreCase("Windows XP");
	public static final boolean isWindowsVista		= OSName.equalsIgnoreCase("Windows Vista");
	public static final boolean isWindows95		= OSName.equalsIgnoreCase("Windows 95");
	public static final boolean isWindows98		= OSName.equalsIgnoreCase("Windows 98");
	public static final boolean isWindowsME		= OSName.equalsIgnoreCase("Windows ME");
	public static final boolean isWindows9598ME	= isWindows95 || isWindows98 || isWindowsME;

	public static final boolean isWindows	= !(isOSX || isLinux || isSolaris);

}
