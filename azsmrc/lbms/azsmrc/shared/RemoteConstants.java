package lbms.azsmrc.shared;

import java.nio.charset.Charset;


public class RemoteConstants {

	public static final String  OSName = System.getProperty("os.name");

	  public static final boolean isOSX				= OSName.equalsIgnoreCase("Mac OS X");
	  public static final boolean isLinux			= OSName.equalsIgnoreCase("Linux");
	  public static final boolean isSolaris			= OSName.equalsIgnoreCase("SunOS");
	  public static final boolean isWindowsXP		= OSName.equalsIgnoreCase("Windows XP");
	  public static final boolean isWindows95		= OSName.equalsIgnoreCase("Windows 95");
	  public static final boolean isWindows98		= OSName.equalsIgnoreCase("Windows 98");
	  public static final boolean isWindowsME		= OSName.equalsIgnoreCase("Windows ME");
	  public static final boolean isWindows9598ME	= isWindows95 || isWindows98 || isWindowsME;

	  public static final boolean isWindows	= !(isOSX || isLinux || isSolaris);

	public static final double CURRENT_VERSION = 1;

	//Errors in xml request
	public static final int E_INVALID_REQUEST = 1;
	public static final int E_INVALID_PROTOCOL = 2;

	//Stats
	public static final int ST_ALL 			= Integer.MAX_VALUE;
	public static final int ST_NAME 		= 1;
	public static final int ST_POSITION		= 1<<1;
	public static final int ST_DOWNLOAD_AVG = 1<<2;
	public static final int ST_UPLOAD_AVG 	= 1<<3;
	public static final int ST_DOWNLOADED 	= 1<<4;
	public static final int ST_UPLOADED 	= 1<<5;
	public static final int ST_HEALTH 		= 1<<6;
	public static final int ST_COMPLETITION = 1<<7;
	public static final int ST_AVAILABILITY = 1<<8;
	public static final int ST_ETA 			= 1<<9;
	public static final int ST_STATE		= 1<<10;
	public static final int ST_STATUS		= 1<<11;
	public static final int ST_SHARE		= 1<<12;
	public static final int ST_TRACKER		= 1<<13;
	public static final int ST_LIMIT_DOWN	= 1<<14;
	public static final int ST_LIMIT_UP		= 1<<15;
	public static final int ST_SEEDS		= 1<<16;
	public static final int ST_LEECHER		= 1<<17;
	public static final int ST_TOTAL_SEEDS	= 1<<18;
	public static final int ST_TOTAL_LEECHER= 1<<19;
	public static final int ST_DISCARDED	= 1<<20;
	public static final int ST_SIZE			= 1<<21;
	public static final int ST_ELAPSED_TIME	= 1<<22;
	public static final int ST_TOTAL_AVG	= 1<<23;
	public static final int ST_SCRAPE_TIMES	= 1<<24;
	public static final int ST_ALL_SEEDS	= ST_SEEDS | ST_TOTAL_SEEDS;
	public static final int ST_ALL_LEECHER	= ST_LEECHER | ST_TOTAL_LEECHER;

	//Events
	public static final int EV_DL_FINISHED	= 1;
	public static final int EV_DL_REMOVED	= 2;
	public static final int EV_DL_EXCEPTION	= 3;
	public static final int EV_EXCEPTION	= 4;
	public static final int EV_UPDATE_AVAILABLE	= 5;

	//Parameter types
	public static final int PARAMETER_NOT_FOUND = -1;
	public static final int PARAMETER_STRING 	= 1;
	public static final int PARAMETER_INT		= 2;
	public static final int PARAMETER_BOOLEAN	= 3;
	public static final int PARAMETER_FLOAT		= 4;

	//User Righs
	public static final int RIGHTS_ADMIN = 1;
	public static final int RIGHTS_FORCESTART = 1<<1;

	//Az Core
	public static final String CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC			= "Max Upload Speed KBs";
	public static final String CORE_PARAM_INT_MAX_UPLOAD_SPEED_SEEDING_KBYTES_PER_SEC 	= "Max Upload Speed When Only Seeding KBs";
 	public static final String CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC			= "Max Download Speed KBs";
 	public static final String CORE_PARAM_INT_MAX_CONNECTIONS_PER_TORRENT				= "Max Connections Per Torrent";
 	public static final String CORE_PARAM_INT_MAX_CONNECTIONS_GLOBAL					= "Max Connections Global";
 	public static final String CORE_PARAM_INT_MAX_DOWNLOADS								= "Max Downloads";
 	public static final String CORE_PARAM_INT_MAX_ACTIVE								= "Max Active Torrents";
 	public static final String CORE_PARAM_INT_MAX_ACTIVE_SEEDING						= "Max Active Torrents When Only Seeding";

 	public static final String CORE_PARAM_BOOLEAN_MAX_UPLOAD_SPEED_SEEDING 				= "Max Upload Speed When Only Seeding Enabled";
 	public static final String CORE_PARAM_BOOLEAN_MAX_ACTIVE_SEEDING 					= "Max Active Torrents When Only Seeding Enabled";
	public static final String CORE_PARAM_BOOLEAN_SOCKS_PROXY_NO_INWARD_CONNECTION		= "SOCKS Proxy No Inward Connection";
	public static final String CORE_PARAM_BOOLEAN_NEW_SEEDS_START_AT_TOP				= "Newly Seeding Torrents Get First Priority";

	public static final String CORE_PARAM_STRING_LOCAL_BIND_IP							= "CORE_PARAM_STRING_LOCAL_BIND_IP";
	public static final String CORE_PARAM_BOOLEAN_FRIENDLY_HASH_CHECKING				= "CORE_PARAM_BOOLEAN_FRIENDLY_HASH_CHECKING";



	//Update URL
	public static final String UPDATE_URL = "http://azsmrc.sourceforge.net/AzSMRCupdate.xml.gz";

	//Encoder stuff
	public static final String DEFAULT_ENCODING 	= "UTF8";
	public static final String BYTE_ENCODING 		= "ISO-8859-1";
	public static Charset	DEFAULT_CHARSET;
	public static Charset	BYTE_CHARSET;

	static{
		try{
			BYTE_CHARSET 	= Charset.forName( RemoteConstants.BYTE_ENCODING );
			DEFAULT_CHARSET = Charset.forName( RemoteConstants.DEFAULT_ENCODING );

  	}catch( Throwable e ){

  		e.printStackTrace();
  	}
	}

}
