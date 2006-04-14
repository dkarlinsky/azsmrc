/*
 * File    : DisplayFormatters.java
 * Created : 07-Oct-2003
 * By      : gardnerpar
 *
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package lbms.azsmrc.remote.client.util;

/**
 * @author gardnerpar
 *
 */

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;

public class
DisplayFormatters
{
	final public static int UNIT_B  = 0;
	final public static int UNIT_KB = 1;
	final public static int UNIT_MB = 2;
	final public static int UNIT_GB = 3;
	final public static int UNIT_TB = 4;

	final private static int UNITS_PRECISION[] =	 {	 0, // B
														 1, //KB
														 2, //MB
														 2, //GB
														 3 //TB
													  };
	private static String[] units;
	private static String[] units_rate;
	private static int unitsStopAt = UNIT_TB;

	private static String[] units_base10;

	private static String		per_sec;

	private static boolean use_si_units = false;
	private static boolean use_units_rate_bits = false;
	private static boolean not_use_GB_TB;


	// private static String lastDecimalFormat = "";


	static NumberFormat	percentage_format;

	static{
		setUnits();
		percentage_format = NumberFormat.getPercentInstance();
		percentage_format.setMinimumFractionDigits(1);
		percentage_format.setMaximumFractionDigits(1);
	}

  public static void
  setUnits()
  {
	  // (1) http://physics.nist.gov/cuu/Units/binary.html
	  // (2) http://www.isi.edu/isd/LOOM/documentation/unit-definitions.text

	units = new String[unitsStopAt + 1];
	units_rate = new String[unitsStopAt + 1];

	if ( use_si_units ){
	  // fall through intentional
	  switch (unitsStopAt) {
		case UNIT_TB:
		  units[UNIT_TB] = "TiB";
		  units_rate[UNIT_TB] = (use_units_rate_bits) ? "Tibit"  :"TiB";
		case UNIT_GB:
		  units[UNIT_GB]= "GiB";
		  units_rate[UNIT_GB] = (use_units_rate_bits) ? "Gibit"  : "GiB";
		case UNIT_MB:
		  units[UNIT_MB] = "MiB";
		  units_rate[UNIT_MB] = (use_units_rate_bits) ? "Mibit"  : "MiB";
		case UNIT_KB:
		  // can be upper or lower case k
		  units[UNIT_KB] ="KiB";
		  // can be upper or lower case k, upper more consistent
		  units_rate[UNIT_KB] = (use_units_rate_bits) ? "Kibit"  : "KiB";
		case UNIT_B:
		  units[UNIT_B] = "B";
		  units_rate[UNIT_B] = (use_units_rate_bits)  ?   "bit"  :   "B";
	  }
	}else{
	  switch (unitsStopAt) {
		case UNIT_TB:
		  units[UNIT_TB] = "TB";
		  units_rate[UNIT_TB] = (use_units_rate_bits) ? "Tbit"  : "TB";
		case UNIT_GB:
		  units[UNIT_GB]= "GB";
		  units_rate[UNIT_GB] = (use_units_rate_bits) ? "Gbit"  : "GB";
		case UNIT_MB:
		  units[UNIT_MB] ="MB";
		  units_rate[UNIT_MB] = (use_units_rate_bits) ? "Mbit"  : "MB";
		case UNIT_KB:
		  // yes, the k should be lower case
		  units[UNIT_KB] ="kB";
		  units_rate[UNIT_KB] = (use_units_rate_bits) ? "kbit"  : "kB";
		case UNIT_B:
		  units[UNIT_B] = "B";
		  units_rate[UNIT_B] = (use_units_rate_bits)  ? "bit"  :  "B";
	  }
	}


	per_sec = "/s" ;

	units_base10 =
		new String[]{  "B", "KB",  "MB" ,  "GB",  "TB" };

	for (int i = 0; i <= unitsStopAt; i++) {
	  units[i] 		= units[i];
	  units_rate[i] = units_rate[i] + per_sec;
	}

	NumberFormat.getPercentInstance().setMinimumFractionDigits(1);
	NumberFormat.getPercentInstance().setMaximumFractionDigits(1);
   }


	private static String	PeerManager_status_finished;
	private static String	PeerManager_status_finishedin;
	private static String	Formats_units_alot;

	public static String
	getRateUnit(
		int		unit_size )
	{
		return( units_rate[unit_size].substring(1, units_rate[unit_size].length()) );
	}
	public static String
	getUnit(
		int		unit_size )
	{
		return( units[unit_size].substring(1, units[unit_size].length()) );
	}

	public static String
	getRateUnitBase10(int unit_size) {
		return units_base10[unit_size] + per_sec;
	}

	public static String
	getUnitBase10(int unit_size) {
		return units_base10[unit_size];
	}

	public static String
	formatByteCountToKiBEtc(int n)
	{
		return( formatByteCountToKiBEtc((long)n));
	}

	public static
	String formatByteCountToKiBEtc(
		long n )
	{
		return( formatByteCountToKiBEtc( n, false, false ));
	}

	public static
	String formatByteCountToKiBEtc(
		long n, boolean bTruncateZeros )
	{
		return( formatByteCountToKiBEtc( n, false, bTruncateZeros ));
	}

	protected static
	String formatByteCountToKiBEtc(
		long	n,
		boolean	rate,
		boolean bTruncateZeros)
	{
		double dbl = (rate && use_units_rate_bits) ? n * 8 : n;

	  	int unitIndex = UNIT_B;

	  	while (dbl >= 1024 && unitIndex < unitsStopAt){

		  dbl /= 1024L;
		  unitIndex++;
		}

		return( formatDecimal( dbl, UNITS_PRECISION[unitIndex], bTruncateZeros ) +
				( rate ? units_rate[unitIndex] : units[unitIndex]));
	}

	public static String
	formatByteCountToKiBEtcPerSec(
		long		n )
	{
		return( formatByteCountToKiBEtc(n,true,false));
	}

	public static String
	formatByteCountToKiBEtcPerSec(
		long		n,
		boolean bTruncateZeros)
	{
		return( formatByteCountToKiBEtc(n,true, bTruncateZeros));
	}

		// base 10 ones

	public static String
	formatByteCountToBase10KBEtc(
			long n)
	{
		if (n < 1024){

			return n + units_base10[UNIT_B];

		}else if (n < 1024 * 1024){

			return 	(n / 1024) + "." +
					((n % 1024) / 100) +
					units_base10[UNIT_KB];

		}else if ( n < 1024L * 1024L * 1024L  || not_use_GB_TB ){

			return 	(n / (1024L * 1024L)) + "." +
					((n % (1024L * 1024L)) / (1024L * 100L)) +
					units_base10[UNIT_MB];

		}else if (n < 1024L * 1024L * 1024L * 1024L){

			return (n / (1024L * 1024L * 1024L)) + "." +
					((n % (1024L * 1024L * 1024L)) / (1024L * 1024L * 100L))+
					units_base10[UNIT_GB];

		}else if (n < 1024L * 1024L * 1024L * 1024L* 1024L){

			return (n / (1024L * 1024L * 1024L* 1024L)) + "." +
					((n % (1024L * 1024L * 1024L* 1024L)) / (1024L * 1024L * 1024L* 100L))+
					units_base10[UNIT_TB];
		}else{

			return Formats_units_alot;
		}
	}

	
	public static String
	formatKBCountToBase10KBEtc(
			long n)
	{
		if (n < 1000){

			return n + units_base10[UNIT_KB];

		}else if (n < 1000 * 1000){

			return 	(n / 1000) + "." +
					((n % 1000) / 100) +
					units_base10[UNIT_MB];

		}else if ( n < 1000L * 1000L * 1000L  || not_use_GB_TB ){

			return 	(n / (1000L * 1000L)) + "." +
					((n % (1000L * 1000L)) / (1000L * 100L)) +
					units_base10[UNIT_GB];

		}else if (n < 1000L * 1000L * 1000L * 1000L){

			return (n / (1000L * 1000L * 1000L)) + "." +
					((n % (1000L * 1000L * 1000L)) / (1000L * 1000L * 100L))+
					units_base10[UNIT_TB];

		}else{

			return Formats_units_alot;
		}
	}
	
	
	public static String
	formatByteCountToBase10KBEtcPerSec(
			long		n )
	{
		return( formatByteCountToBase10KBEtc(n) + per_sec );
	}

   public static String
   formatETA(long eta)
   {
	 if (eta == 0) return PeerManager_status_finished;
	 if (eta == -1) return "";
	 if (eta > 0) return TimeFormatter.format(eta);

	 return PeerManager_status_finishedin + " " + TimeFormatter.format(eta * -1);
   }



  public static String formatPercentFromThousands(int thousands) {

	return percentage_format.format(thousands / 1000.0);
  }

  public static String formatTimeStamp(long time) {
	StringBuffer sb = new StringBuffer();
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeInMillis(time);
	sb.append('[');
	sb.append(formatIntToTwoDigits(calendar.get(Calendar.DAY_OF_MONTH)));
	sb.append('.');
	sb.append(formatIntToTwoDigits(calendar.get(Calendar.MONTH)+1));	// 0 based
	sb.append('.');
	sb.append(calendar.get(Calendar.YEAR));
	sb.append(' ');
	sb.append(formatIntToTwoDigits(calendar.get(Calendar.HOUR_OF_DAY)));
	sb.append(':');
	sb.append(formatIntToTwoDigits(calendar.get(Calendar.MINUTE)));
	sb.append(':');
	sb.append(formatIntToTwoDigits(calendar.get(Calendar.SECOND)));
	sb.append(']');
	return sb.toString();
  }

  public static String formatIntToTwoDigits(int n) {
	return n < 10 ? "0".concat(String.valueOf(n)) : String.valueOf(n);
  }

  public static String
  formatDate(
  	long		date )
  {
  	if ( date == 0 ){
  		return( "" );
  	}

  	SimpleDateFormat temp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

  	return( temp.format(new Date(date)));
  }

  public static String
  formatDateShort(
	long    date )
  {
	if ( date == 0 ){
	  return( "" );
	}

		// 24 hour clock, no point in including AM/PM

	SimpleDateFormat temp = new SimpleDateFormat("MMM dd, HH:mm");

	return( temp.format(new Date(date)));
  }

  public static String
  formatDateNum(
  	long		date )
  {
  	if ( date == 0 ){
  		return( "" );
  	}

  	SimpleDateFormat temp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  	return( temp.format(new Date(date)));
  }

  public static String
  formatTime(
	long    time )
  {
	return( TimeFormatter.formatColon( time / 1000 ));
  }

  public static String
  formatDecimal(
  	double value,
  	int		precision)
  {
  	return formatDecimal(value, precision, false);
  }


  public static String
  formatDecimal(
  	double value,
  	int		precision,
  	boolean bTruncateZeros)
  {
  	// NumberFormat rounds, so truncate at precision
  	double tValue;
  	if (precision == 0) {
  		tValue = (long)value;
  	} else {
  		double shift =  Math.pow(10, precision);
  		tValue = ((long)(value * shift)) / shift;
  	}

		NumberFormat nf =  NumberFormat.getNumberInstance();
		nf.setGroupingUsed(false); // no commas
		if (!bTruncateZeros)
			nf.setMinimumFractionDigits(precision);

		return nf.format(tValue);
  }

  		/**
  		 * Attempts vaguely smart string truncation by searching for largest token and truncating that
  		 * @param str
  		 * @param width
  		 * @return
  		 */

  	public static String
	truncateString(
		String	str,
		int		width )
  	{
  		int	excess = str.length() - width;

  		if ( excess <= 0 ){

  			return( str );
  		}

  		excess += 3;	// for ...

  		int	token_start = -1;
  		int	max_len		= 0;
  		int	max_start	= 0;

  		for (int i=0;i<str.length();i++){

  			char	c = str.charAt(i);

  			if ( Character.isLetterOrDigit( c ) || c == '-' || c == '~' ){

  				if ( token_start == -1 ){

  					token_start	= i;

  				}else{

  					int	len = i - token_start;

  					if ( len > max_len ){

  						max_len		= len;
  						max_start	= token_start;
  					}
  				}
  			}else{

  				token_start = -1;
  			}
  		}

  		if ( max_len >= excess ){

  			int	trim_point = max_start + max_len;

  			return( str.substring( 0, trim_point - excess ) + "..." + str.substring( trim_point ));
  		}else{

  			return( str.substring( 0, width-3 ) + "..." );
  		}
  	}

  	// Used to test fractions and displayformatter.
  	// Keep until everything works okay.
  	public static void main(String[] args) {
  		// set decimal display to ","
  		Locale.setDefault(Locale.GERMAN);

  		double d = 0.000003991630774821635;
  		NumberFormat nf =  NumberFormat.getNumberInstance();
  		nf.setMaximumFractionDigits(6);
  		nf.setMinimumFractionDigits(6);
  		String s = nf.format(d);

  		System.out.println("Actual: " + d);  // Displays 3.991630774821635E-6
  		System.out.println("NF/6:   " + s);  // Displays 0.000004
  		// should display 0.000003
			System.out.println("DF:     " + DisplayFormatters.formatDecimal(d , 6));
  		// should display 0
			System.out.println("DF 0:   " + DisplayFormatters.formatDecimal(d , 0));
  		// should display 0.000000
			System.out.println("0.000000:" + DisplayFormatters.formatDecimal(0 , 6));
  		// should display 0.001
			System.out.println("0.001:" + DisplayFormatters.formatDecimal(0.001, 6, true));
  		// should display 0
			System.out.println("0:" + DisplayFormatters.formatDecimal(0 , 0));
  		// should display 123456
			System.out.println("123456:" + DisplayFormatters.formatDecimal(123456, 0));
  		// should display 123456
			System.out.println("123456:" + DisplayFormatters.formatDecimal(123456.999, 0));
		}
}