 /*
 * Created on Oct 10, 2003
 * Modified Apr 14, 2004 by Alon Rohter
 * Copyright (C) 2003, 2004, 2005, 2006 Aelitis, All Rights Reserved.
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
 * 
 */

package lbms.azsmrc.remote.client.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import lbms.azsmrc.remote.client.torrent.TOTorrentFactory;
import lbms.azsmrc.shared.RemoteConstants;

/**
 * File utility class.
 */
public class FileUtil {
  public static final String DIR_SEP = System.getProperty("file.separator");


  public static String getCanonicalFileName(String filename) {
	// Sometimes Windows use filename in 8.3 form and cannot
	// match .torrent extension. To solve this, canonical path
	// is used to get back the long form

	String canonicalFileName = filename;
	try {
	  canonicalFileName = new File(filename).getCanonicalPath();
	}
	catch (IOException ignore) {}
	return canonicalFileName;
  }





  public static boolean isTorrentFile(String filename) throws FileNotFoundException, IOException {
	File check = new File(filename);
	if (!check.exists())
	  throw new FileNotFoundException("File "+filename+" not found.");
	if (!check.canRead())
	  throw new IOException("File "+filename+" cannot be read.");
	if (check.isDirectory())
	  throw new FileIsADirectoryException("File "+filename+" is a directory.");
	try {
	  TOTorrentFactory.deserialiseFromBEncodedFile(check);
	  return true;
	} catch (Throwable e) {
	  return false;
	}
  }




  public static long
  getFileOrDirectorySize(
  	File		file )
  {
  	if ( file.isFile()){

  		return( file.length());

  	}else{

  		long	res = 0; 

  		File[] files = file.listFiles();

  		if ( files != null ){

  			for (int i=0;i<files.length;i++){

  				res += getFileOrDirectorySize( files[i] );
  			}
  		}

  		return( res );
  	}
  }

  /**
   * Deletes the given dir and all dirs underneath if empty.
   * Don't delete default save path or completed files directory, however,
   * allow deletion of their empty subdirectories
   * Files defined to be ignored for the sake of torrent creation are automatically deleted
   * For example, by default this includes thumbs.db
   */



  public static String
  convertOSSpecificChars(
  	String	file_name_in )
  {
  		// this rule originally from DiskManager

  	char[]	chars = file_name_in.toCharArray();

  	for (int i=0;i<chars.length;i++){

  		if ( chars[i] == '"' ){

  			chars[i] = '\'';
  		}
  	}

  	if ( !RemoteConstants.isOSX ){

  		if ( RemoteConstants.isWindows ){

  				//  this rule originally from DiskManager

  		 	for (int i=0;i<chars.length;i++){

  		 		char	c = chars[i];

					if ( c == '\\' || c == '/' || c == ':' || c == '?' || c == '*' ){

						chars[i] = '_';
					}
				}
  		}

  			// '/' is valid in mac file names, replace with space
  			// so it seems are cr/lf

	 	for (int i=0;i<chars.length;i++){

			char	c = chars[i];

			if ( c == '/' || c == '\r' || c == '\n'  ){

				chars[i] = ' ';
			}
		}
  	}

  	String	file_name_out = new String(chars);

	try{

			// mac file names can end in space - fix this up by getting
			// the canonical form which removes this on Windows

		String str = new File(file_name_out).getCanonicalFile().toString();

		int	p = str.lastIndexOf( File.separator );

		file_name_out = str.substring(p+1);

	}catch( Throwable e ){
		// ho hum, carry on, it'll fail later
		//e.printStackTrace();
	}

	//System.out.println( "convertOSSpecificChars: " + file_name_in + " ->" + file_name_out );

	return( file_name_out );
  }


	/**
	 * Backup the given file to filename.bak, removing the old .bak file if necessary.
	 * If _make_copy is true, the original file will copied to backup, rather than moved.
	 * @param _filename name of file to backup
	 * @param _make_copy copy instead of move
	 */
	public static void backupFile( final String _filename, final boolean _make_copy ) {
	  backupFile( new File( _filename ), _make_copy );
	}

	/**
	 * Backup the given file to filename.bak, removing the old .bak file if necessary.
	 * If _make_copy is true, the original file will copied to backup, rather than moved.
	 * @param _file file to backup
	 * @param _make_copy copy instead of move
	 */
	public static void backupFile( final File _file, final boolean _make_copy ) {
	  if ( _file.length() > 0L ) {
		File bakfile = new File( _file.getAbsolutePath() + ".bak" );
		if ( bakfile.exists() ) bakfile.delete();
		if ( _make_copy ) {
		  copyFile( _file, bakfile );
		}
		else {
		  _file.renameTo( bakfile );
		}
	  }
	}


	/**
	 * Copy the given source file to the given destination file.
	 * Returns file copy success or not.
	 * @param _source_name source file name
	 * @param _dest_name destination file name
	 * @return true if file copy successful, false if copy failed
	 */
	public static boolean copyFile( final String _source_name, final String _dest_name ) {
	  return copyFile( new File(_source_name), new File(_dest_name));
	}

	/**
	 * Copy the given source file to the given destination file.
	 * Returns file copy success or not.
	 * @param _source source file
	 * @param _dest destination file
	 * @return true if file copy successful, false if copy failed
	 */
	/*
	// FileChannel.transferTo() seems to fail under certain linux configurations.
	public static boolean copyFile( final File _source, final File _dest ) {
	  FileChannel source = null;
	  FileChannel dest = null;
	  try {
		if( _source.length() < 1L ) {
		  throw new IOException( _source.getAbsolutePath() + " does not exist or is 0-sized" );
		}
		source = new FileInputStream( _source ).getChannel();
		dest = new FileOutputStream( _dest ).getChannel();

		source.transferTo(0, source.size(), dest);
		return true;
	  }
	  catch (Exception e) {
		Debug.out( e );
		return false;
	  }
	  finally {
		try {
		  if (source != null) source.close();
		  if (dest != null) dest.close();
		}
		catch (Exception ignore) {}
	  }
	}
	*/

	public static boolean copyFile( final File _source, final File _dest ) {
	  try {
		copyFile( new FileInputStream( _source ), new FileOutputStream( _dest ) );
		return true;
	  }
	  catch( Throwable e ) {
		  e.printStackTrace();
		return false;
	  }
	}

	public static boolean copyFile( final File _source, final OutputStream _dest, boolean closeOutputStream ) {
		try {
		  copyFile( new FileInputStream( _source ), _dest, closeOutputStream );
		  return true;
		}
		catch( Throwable e ) {
			 e.printStackTrace();
		  return false;
		}
	  }
	public static void
	copyFile(
	  InputStream   is,
	  OutputStream  os )
	throws IOException {
	  copyFile(is,os,true);
	}

	public static void
	copyFile(
		InputStream		is,
		OutputStream	os,
	boolean closeInputStream)

		throws IOException
	{
		try{

			if ( !(is instanceof BufferedInputStream )){

				is = new BufferedInputStream(is);
			}

			byte[]	buffer = new byte[65536*2];

			while(true){

				int	len = is.read(buffer);

				if ( len == -1 ){

					break;
				}

				os.write( buffer, 0, len );
			}
		}finally{
			try{
		if(closeInputStream)
				  is.close();
			}catch( IOException e ){

			}

			os.close();
		}
	}


	/**
	 * Returns the file handle for the given filename or it's
	 * equivalent .bak backup file if the original doesn't exist
	 * or is 0-sized.  If neither the original nor the backup are
	 * available, a null handle is returned.
	 * @param _filename root name of file
	 * @return file if successful, null if failed
	 */
	public static File getFileOrBackup( final String _filename ) {
	  try {
		File file = new File( _filename );
		//make sure the file exists and isn't zero-length
		if ( file.length() <= 1L ) {
		  //if so, try using the backup file
		  File bakfile = new File( _filename + ".bak" );
		  if ( bakfile.length() <= 1L ) {
			return null;
		  }
		  else return bakfile;
		}
		else return file;
	  }
	  catch (Exception e) {
		 e.printStackTrace();
		return null;
	  }
	}


	public static File
	getJarFileFromURL(
		String		url_str )
	{
		if (url_str.startsWith("jar:file:")) {

			// java web start returns a url like "jar:file:c:/sdsd" which then fails as the file
			// part doesn't start with a "/". Add it in!
			// here's an example
			// jar:file:C:/Documents%20and%20Settings/stuff/.javaws/cache/http/Dparg.homeip.net/P9090/DMazureus-jnlp/DMlib/XMAzureus2.jar1070487037531!/org/gudy/azureus2/internat/MessagesBundle.properties

			// also on Mac we don't get the spaces escaped

			url_str = url_str.replaceAll(" ", "%20" );

			if ( !url_str.startsWith("jar:file:/")){


				url_str = "jar:file:/".concat(url_str.substring(9));
			}

			try{
					// 	you can see that the '!' must be present and that we can safely use the last occurrence of it

				int posPling = url_str.lastIndexOf('!');

				String jarName = url_str.substring(4, posPling);

					//        System.out.println("jarName: " + jarName);

				URI uri = URI.create(jarName);

				File jar = new File(uri);

				return( jar );

			}catch( Throwable e ){

				e.printStackTrace();
			}
		}

		return( null );
	}

	public static boolean
	renameFile(
		File		from_file,
		File		to_file )
	{
		if ( to_file.exists()){
			System.out.println(
					"renameFile: target file '" + to_file + "' already exists, failing");

			return( false );
		}

		if ( !from_file.exists()){
			System.out.println("renameFile: source file '" + from_file+ "' doesn't exist, failing");

			return( false );
		}

		if ( from_file.isDirectory()){

			to_file.mkdirs();

			File[]	files = from_file.listFiles();

			if ( files == null ){

					// empty dir

				return( true );
			}

			int	last_ok = 0;

			for (int i=0;i<files.length;i++){

  				File	ff = files[i];
				File	tf = new File( to_file, ff.getName());

				try{
					 if ( renameFile( ff, tf )){

						last_ok++;

					}else{

						break;
					}
				}catch( Throwable e ){

					System.out.println("renameFile: failed to rename file '" + ff.toString() + "' to '"
									+ tf.toString() + "'");
					e.printStackTrace();

					break;
				}
			}

			if ( last_ok == files.length ){

				File[]	remaining = from_file.listFiles();

				if ( remaining != null && remaining.length > 0 ){
					System.out.println("renameFile: files remain in '" + from_file.toString()
									+ "', not deleting");

				}else{

					if ( !from_file.delete()){
						System.out.println(
								"renameFile: failed to delete '" + from_file.toString() + "'");
					}
				}

				return( true );
			}

				// recover by moving files back

			  for (int i=0;i<last_ok;i++){

				File	ff = files[i];
				File	tf = new File( to_file, ff.getName());

				try{

					if ( !renameFile( tf, ff )){
						System.out.println("renameFile: recovery - failed to move file '" + tf.toString()
										+ "' to '" + ff.toString() + "'");
					}
				}catch( Throwable e ){
					System.out.println("renameFile: recovery - failed to move file '" + tf.toString()
									+ "' to '" + ff.toString() + "'");
					e.printStackTrace();

				}
			  }

			  return( false );

		}else{
			if ( 	/*(!COConfigurationManager.getBooleanParameter("Copy And Delete Data Rather Than Move")) &&*/
					from_file.renameTo( to_file )){

				return( true );

			}else{

				boolean		success	= false;

					// can't rename across file systems under Linux - try copy+delete

				FileInputStream		fis = null;

				FileOutputStream	fos = null;

				try{
					fis = new FileInputStream( from_file );

					fos = new FileOutputStream( to_file );

					byte[]	buffer = new byte[65536];

					while( true ){

						int	len = fis.read( buffer );

						if ( len <= 0 ){

							break;
						}

						fos.write( buffer, 0, len );
					}

					fos.close();

					fos	= null;

					fis.close();

					fis = null;

					if ( !from_file.delete()){
						System.out.println("renameFile: failed to delete '"
										+ from_file.toString() + "'");

						throw( new Exception( "Failed to delete '" + from_file.toString() + "'"));
					}

					success	= true;

					return( true );

				}catch( Throwable e ){

					System.out.println("renameFile: failed to rename '" + from_file.toString()
									+ "' to '" + to_file.toString() + "'");
					e.printStackTrace();

					return( false );

				}finally{

					if ( fis != null ){

						try{
							fis.close();

						}catch( Throwable e ){
						}
					}

					if ( fos != null ){

						try{
							fos.close();

						}catch( Throwable e ){
						}
					}

						// if we've failed then tidy up any partial copy that has been performed

					if ( !success ){

						if ( to_file.exists()){

							to_file.delete();
						}
					}
				}
			}
		}
	}



	public static void writeBytesAsFile( String filename, byte[] file_data ) {
	  try{
		File file = new File( filename );

		FileOutputStream out = new FileOutputStream( file );

		out.write( file_data );

		out.close();
	  }
	  catch( Throwable t ) {
		 t.printStackTrace();
	  }

	}
}
