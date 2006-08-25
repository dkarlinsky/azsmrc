/*
 * Created on Feb 27, 2004
 * Created by Alon Rohter
 * Copyright (C) 2004, 2005, 2006 Alon Rohter, All Rights Reserved.
 * 
 * Copyright (C) 2004, 2005, 2006 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */
package lbms.tools.launcher;

/**
 * Utility class to manage system-dependant information.
 */
public class SystemProperties {

		// note this is also used in the restart code....

	/**
	 * Path separator charactor.
	 */
	public static final String SEP = System.getProperty("file.separator");

	private static 		String APPLICATION_NAME 		= "AzSMRC";


	private static String user_path;
	private static String app_path;

	public static void
	setApplicationName(
			String		name )
	{
		if ( name != null && name.trim().length() > 0 ){

			name	= name.trim();

			if ( user_path != null ){

				if ( !name.equals( APPLICATION_NAME )){

					System.out.println( "**** SystemProperties::setApplicationName called too late! ****" );
				}
			}

			APPLICATION_NAME			= name;
		}
	}


	public static String
	getApplicationName()
	{
		return( APPLICATION_NAME );
	}

  /**
   * Returns the full path to the user's home azureus directory.
   * Under unix, this is usually ~/.azureus/
   * Under Windows, this is usually .../Documents and Settings/username/Application Data/Azureus/
   * Under OSX, this is usually /Users/username/Library/Application Support/Azureus/
   */



  /**
   * Returns the full path to the directory where AzSMRC is installed
   * and running from.
   */
  public static String 
  getApplicationPath() 
  {
	  if ( app_path != null ){

		  return( app_path );
	  }

	  String temp_app_path = System.getProperty("azsmrc.install.path", System.getProperty("user.dir"));

	  if ( !temp_app_path.endsWith(SEP)){

		  temp_app_path += SEP;
	  }

	  app_path = temp_app_path;

	  return( app_path );
  }
}
