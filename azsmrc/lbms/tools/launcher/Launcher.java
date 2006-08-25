/*
 * Created on 25-Jul-2005
 * Created by Paul Gardner
 * Copyright (C) 2005, 2006 Aelitis, All Rights Reserved.
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

package lbms.tools.launcher;

import java.io.*;
import java.net.*;
import java.util.*;




public class Launcher {


	public static void
	main(
		final String[]		args )
	{
			// try and infer the application name. this is only required on OSX as the app name
			// is a component of the "application path" used to find jars etc.

		if ( Constants.isOSX ){

			/* example class path

			 /Applications/Utilities/Azureus.app/Contents/Resources/ 
			Java/swt.jar:/Applications/Utilities/Azureus.app/Contents/Resources/
			Java/swt-pi.jar:/Applications/Utilities/Azureus.app/Contents/Resources/
			Java/Azureus2.jar:/System/Library/Java
			*/

			String	classpath = System.getProperty("java.class.path");

			if ( classpath == null ){

				System.out.println( "classpath is null!!!!" );

			}else{

				int	dot_pos = classpath.indexOf( ".app/Contents" );

				if ( dot_pos == -1 ){

					// System.out.println( "can't find .app/Contents" );

				}else{

					int	start_pos = dot_pos;

					while( start_pos >= 0 && classpath.charAt(start_pos) != '/' ){

						start_pos--;
					}

					String	app_name = classpath.substring( start_pos+1, dot_pos );

					SystemProperties.setApplicationName( app_name );
				}
			}
		}



		Launchable[]	launchables = findLaunchables();

		if ( launchables.length == 0 ){

			System.out.println(  "No Launchables found" );

			return;

		}else if ( launchables.length > 1 ){

			System.out.println( "Multiple Launchables found, running first" );
		}
		final Launchable launch = launchables[0];
		new Thread (new Runnable() {
			public void run() {
				launch.launch(args);

			}
		}).start();
/*
		try{
				// set default details for restarter

			SystemProperties.setApplicationEntryPoint( "org.gudy.azureus2.plugins.PluginLauncher" );

			launchables[0].setDefaults( args );

				// see if we're a secondary instance

			if ( PluginSingleInstanceHandler.process( listener, args )){

				return;
			}
				// we have to run the core startup on a separate thread and then effectively pass "this thread"
				// through to the launchable "process" method

			Thread core_thread =
				new Thread( "PluginLauncher" )
				{
					public void
					run()
					{
						try{
								// give 'process' call below some time to start up

							Thread.sleep(500);

							AzureusCore azureus_core = AzureusCoreFactory.create();

							azureus_core.start();

						}catch( Throwable e ){

							listener.messageLogged( "PluginLauncher: launch fails", e );
						}
					}
				};

			core_thread.setDaemon( true );

			core_thread.start();

			boolean	restart = false;

			boolean	process_succeeded	= false;

			try{
				restart = launchables[0].process();

				process_succeeded	= true;

			}finally{

				try{
					if ( restart ){

						AzureusCoreFactory.getSingleton().restart();

					}else{

						AzureusCoreFactory.getSingleton().stop();
					}
				}catch( Throwable e ){

						// only report this exception if we're not already failing

					if ( process_succeeded ){

						throw( e );
					}
				}
			}

		}catch( Throwable e ){

			listener.messageLogged( "PluginLauncher: launch fails", e );
		}*/
	}




 	private static Launchable[]
	findLaunchables()
	{
				// CAREFUL - this is called BEFORE any AZ initialisation has been performed and must
				// therefore NOT use anything that relies on this (such as logging, debug....)

		List	res = new ArrayList();

		File	app_dir	 = getApplicationFile(".");

		if ( !( app_dir.exists()) && app_dir.isDirectory()){

			System.out.println(  "Application dir '" + app_dir + "' not found" );

			return( new Launchable[0] );
		} else {
			System.out.println(  "Checking " + app_dir );
		}

		File[] files = app_dir.listFiles();

		if ( files == null || files.length == 0 ){

			System.out.println(  "Application dir '" + app_dir + "' empty" );

			return( new Launchable[0] );
		}

				try{

				  ClassLoader classLoader = Launcher.class.getClassLoader();


					// take only the highest version numbers of jars that look versioned

				String[]	file_version 	= {null};
				String[]	file_id 		= {null};

				files	= getHighestJarVersions( files, file_version, file_id );

				for( int j = 0 ; j < files.length ; j++){

					classLoader = addFileToClassPath(classLoader, files[j]);
				}

				Properties props = new Properties();

				File	properties_file = new File( app_dir, "launch.properties");

					// if properties file exists on its own then override any properties file
					// potentially held within a jar

				  if ( properties_file.exists()){

					  FileInputStream	fis = null;

					  try{
						  fis = new FileInputStream( properties_file );

						  props.load( fis );

					  }finally{

						  if ( fis != null ){

							  fis.close();
						  }
					  }
				  }else{

					if ( classLoader instanceof URLClassLoader ){

						URLClassLoader	current = (URLClassLoader)classLoader;

						  URL url = current.findResource("launch.properties");

						  if ( url != null ){

							  props.load(url.openStream());
						  }
					  }
				  }

				String launch_class = (String)props.get( "launch.class");

					// don't support multiple Launchables

				if ( launch_class != null && launch_class.indexOf(';') == -1 ){
					System.out.println("Trying to load: "+launch_class);
					Class c = classLoader.loadClass(launch_class);

					Launchable launchable = (Launchable) c.newInstance();

					if ( launchable instanceof Launchable ){
						res.add( launchable );
					}
				}
			}catch( Throwable e ){

				System.out.println( "Load of Launchable in '" + app_dir + "' fails");
				e.printStackTrace();
			}


		Launchable[]	x = new Launchable[res.size()];

		res.toArray( x );

		return( x );
	}

 	private static File 
 	getApplicationFile(
 		String filename) 
 	{      
		 String path = SystemProperties.getApplicationPath();

		 if (Constants.isOSX ){

			 path = path + "/" + SystemProperties.getApplicationName() + ".app/Contents/";
		 }

		 return new File(path, filename);
 	}

  	public static File[]
	getHighestJarVersions(
		File[]		files,
		String[]	version_out ,
		String[]	id_out )	// currently the version of last versioned jar found...
	{
  			// WARNING!!!!
  			// don't use Debug/lglogger here as we can be called before AZ has been initialised

  		List	res 		= new ArrayList();
  		Map		version_map	= new HashMap();

  		for (int i=0;i<files.length;i++){

  			File	f = files[i];

  			String	name = f.getName().toLowerCase();

  			if ( name.endsWith(".jar")){
  				System.out.println("Checking: "+name);

  				int cvs_pos = name.lastIndexOf("_cvs");

  				int sep_pos;

  				if (cvs_pos <= 0)
  					sep_pos = name.lastIndexOf("_");
  				else
  					sep_pos = name.lastIndexOf("_", cvs_pos - 1);

  				String	prefix;

				String	version;;


  				if ( 	sep_pos == -1 || 
  						sep_pos == name.length()-1 ||
						!Character.isDigit(name.charAt(sep_pos+1))){

  					prefix = name.substring(0, name.indexOf('.'));
  					version = "-1.0";

  				}else{
  					prefix = name.substring(0,sep_pos);

  					version = name.substring(sep_pos+1, (cvs_pos <= 0) ? name.length()-4 : cvs_pos);
  				}
  				String	prev_version = (String)version_map.get(prefix);

  				if ( prev_version == null ){

					version_map.put( prefix, version );

				}else{

					if ( compareVersions( prev_version, version ) < 0 ){

						version_map.put( prefix, version );
					}
				}
   			}
  		}

  		Iterator it = version_map.keySet().iterator();

  		while(it.hasNext()){

  			String	prefix 	= (String)it.next();
  			String	version	= (String)version_map.get(prefix);
  			String	target;
  			if (version.equalsIgnoreCase("-1.0"))
  				target = prefix;
  			else
  				target = prefix + "_" + version;

  			version_out[0] 	= version;
  			id_out[0]		= prefix;

  			for (int i=0;i<files.length;i++){

  				File	f = files[i];

  				String	lc_name = f.getName().toLowerCase();

  				if ( lc_name.equals( target + ".jar" ) ||
  					 lc_name.equals( target + "_cvs.jar" )){
  					System.out.println("Adding "+target+" to classpath");
  					res.add( f );
  					break;
  				}
  			}
  		}



  		File[]	res_array = new File[res.size()];

  		res.toArray( res_array );

  		return( res_array );
  	}

	public static ClassLoader
	addFileToClassPath(
		ClassLoader		classLoader,
		File 			f)
	{
	  if ( 	f.exists() &&
			  (!f.isDirectory())&&
			  f.getName().endsWith(".jar")){

		  try {

				  // URL classloader doesn't seem to delegate to parent classloader properly
				  // so if you get a chain of them then it fails to find things. Here we
				  // make sure that all of our added URLs end up within a single URLClassloader
				  // with its parent being the one that loaded this class itself

			  if ( classLoader instanceof URLClassLoader ){

				  URL[]	old = ((URLClassLoader)classLoader).getURLs();

				  URL[]	new_urls = new URL[old.length+1];

				  System.arraycopy( old, 0, new_urls, 0, old.length );

				  new_urls[new_urls.length-1]= f.toURL();

				  classLoader = new URLClassLoader(
									  new_urls,
  									classLoader==Launcher.class.getClassLoader()?
  											classLoader:
  											classLoader.getParent());
			  }else{

				  classLoader = new URLClassLoader(new URL[]{f.toURL()},classLoader);
			  }
		  }catch( Exception e){

				  // don't use Debug/lglogger here as we can be called before AZ has been initialised

			  e.printStackTrace();
		  }
		 }

	  return( classLoader );
	}

	public static int
	compareVersions(
		String		version_1,
		String		version_2 )
	{
		try{
			if ( version_1.startsWith("." )){
				version_1 = "0" + version_1;
			}
			if ( version_2.startsWith("." )){
				version_2 = "0" + version_2;
			}

			StringTokenizer	tok1 = new StringTokenizer(version_1,".");
			StringTokenizer	tok2 = new StringTokenizer(version_2,".");

			while( true ){
				if ( tok1.hasMoreTokens() && tok2.hasMoreTokens()){

					int	i1 = Integer.parseInt(tok1.nextToken());
					int	i2 = Integer.parseInt(tok2.nextToken());

					if ( i1 != i2 ){

						return( i1 - i2 );
					}
				}else if ( tok1.hasMoreTokens()){

					int	i1 = Integer.parseInt(tok1.nextToken());

					if ( i1 != 0 ){

						return( 1 );
					}
				}else if ( tok2.hasMoreTokens()){

					int	i2 = Integer.parseInt(tok2.nextToken());

					if ( i2 != 0 ){

						return( -1 );
					}
				}else{
					return( 0 );
				}
			}
		}catch( Throwable e ){

			e.printStackTrace();

			return( 0 );
		}
	}
}
