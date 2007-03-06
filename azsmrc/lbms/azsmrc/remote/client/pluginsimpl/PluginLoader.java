package lbms.azsmrc.remote.client.pluginsimpl;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import lbms.azsmrc.remote.client.plugins.Plugin;
import lbms.tools.ExtendedProperties;
import lbms.tools.launcher.Constants;
import lbms.tools.launcher.SystemProperties;

/**
 * @author Damokles
 *
 */
public class PluginLoader {

	public static void	findAndLoadPlugins(PluginManagerImpl manager, ExtendedProperties azsmrcProps) {

		File	app_dir	 = getApplicationFile("plugins");

		if ( !( app_dir.exists()) && app_dir.isDirectory()){

			System.out.println(  "Plugin dir '" + app_dir + "' not found" );

			return;
		} else {
			System.out.println(  "Checking " + app_dir );
		}

		File[] plugins = app_dir.listFiles();

		if ( plugins == null || plugins.length == 0 ){

			System.out.println(  "Plugin dir '" + app_dir + "' empty" );

			return;
		}
		for ( int i=0;i<plugins.length;i++ ) {

			File	plugin_dir = plugins[i];

			if( !plugin_dir.isDirectory()){

				continue;
			}

			File[] pluginContents = plugin_dir.listFiles();

			boolean	looks_like_plugin	= false;

			for (int j=0;j<pluginContents.length;j++){

				String	name = pluginContents[j].getName().toLowerCase();

				if ( name.endsWith( ".jar") || name.equals( "plugin.properties" )){

					looks_like_plugin = true;

					break;
				}
			}

			if (!looks_like_plugin) continue;

			try {

				System.out.println("Checking dir for Plugins: "+plugin_dir.getCanonicalPath());

				ClassLoader	root_cl = getRootClassLoader(new File("."));

				ClassLoader classLoader = root_cl;

				File[] pluginjars	= getHighestJarVersions( plugins[i].listFiles());

				for( int j = 0 ; j < pluginjars.length ; j++){
					classLoader = addFileToClassPath(root_cl,classLoader, pluginjars[j]);
				}

				Properties props = new Properties();

				File properties_file = new File( plugin_dir, "plugin.properties");

				// if properties file exists on its own then override any properties file
				// potentially held within a jar

				if ( properties_file.exists()) {

					FileInputStream	fis = null;

					try{
						fis = new FileInputStream( properties_file );

						props.load( fis );

					} finally {

						if ( fis != null ){

							fis.close();
						}
					}
				} else {
					if ( classLoader instanceof URLClassLoader ){

						URLClassLoader	current = (URLClassLoader)classLoader;

						URL url = current.findResource("plugin.properties");

						if ( url != null ){

							props.load(url.openStream());
						} else {
							System.out.println("Failed to load Properties from jar.");
						}
					}
				}

				String pluginID = props.getProperty("plugin.id");

				if (azsmrcProps.propertyExists("plugins."+pluginID+".load")) {
					//if we don't want to load this plugin continue
					if (!azsmrcProps.getPropertyAsBoolean("plugins."+pluginID+".load")) {
						System.out.println( "Skipping load of "+pluginID);
						manager.addDisabledPlugin(props, pluginID);
						continue;
					}
				} else {
					//add the plugin to the load list
					azsmrcProps.setProperty("plugins."+pluginID+".load", true);
				}

				String plugin_class = props.getProperty( "azsmrc.plugin.class");

//				if (plugin_class == null) continue;
				// don't support multiple Launchables

				Class c = classLoader.loadClass(plugin_class);

				Plugin	    plugin	= (Plugin) c.newInstance();

				if ( plugin instanceof Plugin ) {

					manager.addPlugin(plugin, props, plugin_dir.getCanonicalPath());
				}
			}catch( Throwable e ) {

				System.out.println( "Load of Plugin in '" + plugin_dir + "' fails");
				e.printStackTrace();
			}
		}
	}

	private static ClassLoader
	getRootClassLoader(
			File		dir )
	{
		ClassLoader root_class_loader = PluginLoader.class.getClassLoader();
		dir = new File( dir, "shared" );
		if ( dir.exists() && dir.isDirectory()){

			File[]	files = dir.listFiles();

			if ( files != null ) {
				files = getHighestJarVersions(files);

				for (int i=0;i<files.length;i++) {
					root_class_loader = addFileToClassPath(root_class_loader,
							root_class_loader, files[i] );
				}

			}
		}
		return root_class_loader;
	}

	private static File
	getApplicationFile(
			String filename)
	{
		String path = SystemProperties.getApplicationPath();

		if (Constants.isOSX ) {

			path = path + "/" + SystemProperties.getApplicationName() + ".app/Contents/";
		}

		return new File(path, filename);
	}

	public static File[] getHighestJarVersions (File[] files)	// currently the version of last versioned jar found...
	{

		List	res 		= new ArrayList();
		Map		version_map	= new HashMap();

		for (int i=0;i<files.length;i++){

			File	f = files[i];

			String	name = f.getName().toLowerCase();

			if ( name.endsWith(".jar") ) {
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

				} else {
					prefix = name.substring(0,sep_pos);

					version = name.substring(sep_pos+1, (cvs_pos <= 0) ? name.length()-4 : cvs_pos);
				}
				String	prev_version = (String)version_map.get(prefix);

				if ( prev_version == null ){

					version_map.put( prefix, version );

				} else {

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

			for (int i=0;i<files.length;i++){

				File	f = files[i];

				String	lc_name = f.getName().toLowerCase();

				if ( lc_name.equals( target + ".jar" ) ||
						lc_name.equals( target + "_cvs.jar" )){
					System.out.println("Adding "+target+" to PluginClasspath.");
					res.add( f );
					break;
				}
			}
		}

		File[]	res_array = new File[res.size()];

		res.toArray( res_array );

		return( res_array );
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

	public static ClassLoader
	addFileToClassPath(
			ClassLoader		root,
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
							classLoader==root?
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
}
