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

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class Launcher {

	private static URLClassLoader classLoader;

	public static void main(final String[] args) {
		boolean restart = false;
		do {

			if (restart) {
				classLoader = null;
				System.gc();
			}

			Launchable[] launchables = findLaunchables();

			if (launchables.length == 0) {

				System.out.println("No Launchables found");

				return;

			} else if (launchables.length > 1) {

				System.out.println("Multiple Launchables found, running first");
			}
			final Launchable launch = launchables[0];


			System.out.println("Starting Launchable");
			restart = launch.launch(args);

			// killAllThreads();
		} while (restart);

	}

	private static Launchable[] findLaunchables() {
		// CAREFUL - this is called BEFORE any AZ initialisation has been
		// performed and must
		// therefore NOT use anything that relies on this (such as logging,
		// debug....)

		List<Launchable> res = new ArrayList<Launchable>();

		File app_dir = getApplicationFile(".");

		if (!(app_dir.exists()) && app_dir.isDirectory()) {

			System.out.println("Application dir '" + app_dir + "' not found");

			return (new Launchable[0]);
		} else {
			System.out.println("Checking " + app_dir);
		}

		File[] files = app_dir.listFiles();

		if (files == null || files.length == 0) {

			System.out.println("Application dir '" + app_dir + "' empty");

			return (new Launchable[0]);
		}

		try {

			classLoader = (Launcher.class.getClassLoader() instanceof URLClassLoader) ? (URLClassLoader) Launcher.class
					.getClassLoader()
					: new URLClassLoader(new URL[0], Launcher.class
							.getClassLoader());

			// take only the highest version numbers of jars that look versioned

			String[] file_version = { null };
			String[] file_id = { null };

			files = getHighestJarVersions(files, file_version, file_id);

			classLoader = addFilesToClassPath(classLoader, files);

			Properties props = new Properties();

			File properties_file = new File(app_dir, "launch.properties");

			// if properties file exists on its own then override any properties
			// file
			// potentially held within a jar

			if (properties_file.exists()) {

				FileInputStream fis = null;

				try {
					fis = new FileInputStream(properties_file);

					props.load(fis);

				} finally {

					if (fis != null) {

						fis.close();
					}
				}
			} else {

				if (classLoader instanceof URLClassLoader) {

					URLClassLoader current = classLoader;

					URL url = current.findResource("launch.properties");

					if (url != null) {

						props.load(url.openStream());
					}
				}
			}

			String launch_class = (String) props.get("launch.class");

			// don't support multiple Launchables

			if (launch_class != null && launch_class.indexOf(';') == -1) {
				System.out.println("Trying to load: " + launch_class);
				Class c = classLoader.loadClass(launch_class);

				Launchable launchable = (Launchable) c.newInstance();

				if (launchable instanceof Launchable) {
					res.add(launchable);
				}
			}
		} catch (Throwable e) {

			System.out.println("Load of Launchable in '" + app_dir + "' fails");
			e.printStackTrace();
		}

		Launchable[] x = new Launchable[res.size()];

		res.toArray(x);

		return (x);
	}

	private static File getApplicationFile(String filename) {
		String path = SystemProperties.getApplicationPath();

		return new File(path, filename);
	}

	public static File[] getHighestJarVersions(File[] files,
			String[] version_out, String[] id_out) // currently the version of
	// last versioned jar
	// found...
	{
		// WARNING!!!!
		// don't use Debug/lglogger here as we can be called before AZ has been
		// initialised

		List<File> res = new ArrayList<File>();
		Map<String, String> version_map = new HashMap<String, String>();

		for (int i = 0; i < files.length; i++) {

			File f = files[i];

			String name = f.getName().toLowerCase();

			if (name.endsWith(".jar")) {
				System.out.println("Checking: " + name);

				int cvs_pos = name.lastIndexOf("_cvs");

				int sep_pos;

				if (cvs_pos <= 0) {
					sep_pos = name.lastIndexOf("_");
				} else {
					sep_pos = name.lastIndexOf("_", cvs_pos - 1);
				}

				String prefix;

				String version;
				;

				if (sep_pos == -1 || sep_pos == name.length() - 1
						|| !Character.isDigit(name.charAt(sep_pos + 1))) {

					prefix = name.substring(0, name.indexOf('.'));
					version = "-1.0";

				} else {
					prefix = name.substring(0, sep_pos);

					version = name.substring(sep_pos + 1, (cvs_pos <= 0) ? name
							.length() - 4 : cvs_pos);
				}
				String prev_version = version_map.get(prefix);

				if (prev_version == null) {

					version_map.put(prefix, version);

				} else {

					if (compareVersions(prev_version, version) < 0) {

						version_map.put(prefix, version);
					}
				}
			}
		}

		Iterator<String> it = version_map.keySet().iterator();

		while (it.hasNext()) {

			String prefix = it.next();
			String version = version_map.get(prefix);
			String target;
			if (version.equalsIgnoreCase("-1.0")) {
				target = prefix;
			} else {
				target = prefix + "_" + version;
			}

			version_out[0] = version;
			id_out[0] = prefix;

			for (int i = 0; i < files.length; i++) {

				File f = files[i];

				String lc_name = f.getName().toLowerCase();

				if (lc_name.equals(target + ".jar")
						|| lc_name.equals(target + "_cvs.jar")) {
					System.out.println("Adding " + target + " to classpath");
					res.add(f);
					break;
				}
			}
		}

		File[] res_array = new File[res.size()];

		res.toArray(res_array);

		return (res_array);
	}

	/*
	 * public static ClassLoader addFileToClassPath(ClassLoader classLoader,
	 * File f) { if (f.exists() && (!f.isDirectory()) &&
	 * f.getName().endsWith(".jar")) { try { // URL classloader doesn't seem to
	 * delegate to parent // classloader properly // so if you get a chain of
	 * them then it fails to find things. // Here we // make sure that all of
	 * our added URLs end up within a single // URLClassloader // with its
	 * parent being the one that loaded this class itself if (classLoader
	 * instanceof URLClassLoader) { URL[] old = ((URLClassLoader)
	 * classLoader).getURLs(); URL[] new_urls = new URL[old.length + 1];
	 * System.arraycopy(old, 0, new_urls, 0, old.length);
	 * new_urls[new_urls.length - 1] = f.toURL(); classLoader = new
	 * URLClassLoader( new_urls, classLoader == Launcher.class.getClassLoader() ?
	 * classLoader : classLoader.getParent()); } else { classLoader = new
	 * URLClassLoader(new URL[] { f.toURL() }, classLoader); } } catch
	 * (Exception e) { // don't use Debug/lglogger here as we can be called
	 * before AZ // has been initialised e.printStackTrace(); } } return
	 * (classLoader); }
	 */

	public static URLClassLoader addFilesToClassPath(URLClassLoader classLoader,
			File[] files) {
		List<URL> urls = new ArrayList<URL>();
		URL[] old = (classLoader).getURLs();
		urls.addAll(Arrays.asList(old));
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			try {
				if (f.exists() && (!f.isDirectory())
						&& f.getName().endsWith(".jar")) {
					urls.add(f.toURL());
					//urls.add(f.toURI().toURL());
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		URL[] new_urls = new URL[urls.size()];
		urls.toArray(new_urls);
		classLoader = new URLClassLoader(new_urls,
				classLoader == Launcher.class.getClassLoader() ? classLoader
						: classLoader.getParent());
		return (classLoader);
	}

	public static int compareVersions(String version_1, String version_2) {
		try {
			if (version_1.startsWith(".")) {
				version_1 = "0" + version_1;
			}
			if (version_2.startsWith(".")) {
				version_2 = "0" + version_2;
			}

			StringTokenizer tok1 = new StringTokenizer(version_1, ".");
			StringTokenizer tok2 = new StringTokenizer(version_2, ".");

			while (true) {
				if (tok1.hasMoreTokens() && tok2.hasMoreTokens()) {

					int i1 = Integer.parseInt(tok1.nextToken());
					int i2 = Integer.parseInt(tok2.nextToken());

					if (i1 != i2) {

						return (i1 - i2);
					}
				} else if (tok1.hasMoreTokens()) {

					int i1 = Integer.parseInt(tok1.nextToken());

					if (i1 != 0) {

						return (1);
					}
				} else if (tok2.hasMoreTokens()) {

					int i2 = Integer.parseInt(tok2.nextToken());

					if (i2 != 0) {

						return (-1);
					}
				} else {
					return (0);
				}
			}
		} catch (Throwable e) {

			e.printStackTrace();

			return (0);
		}
	}

	private static void killAllThreads() {
		// Find the root thread group
		ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
		while (root.getParent() != null) {
			root = root.getParent();
		}

		// Visit each thread group
		visit(root, 0);
	}

	// This method recursively visits all thread groups under `group'.
	public static void visit(ThreadGroup group, int level) {
		System.out.println("Visiting TG: " + group.getName());
		// Get threads in `group'
		int numThreads = group.activeCount();
		Thread[] threads = new Thread[numThreads * 2];
		numThreads = group.enumerate(threads, false);

		// Enumerate each thread in `group'
		for (int i = 0; i < numThreads; i++) {
			// Get thread
			Thread thread = threads[i];
			System.out.println("\tThread: " + thread.getName());
		}

		// Get thread subgroups of `group'
		int numGroups = group.activeGroupCount();
		ThreadGroup[] groups = new ThreadGroup[numGroups * 2];
		numGroups = group.enumerate(groups, false);

		// Recursively visit each subgroup
		for (int i = 0; i < numGroups; i++) {
			visit(groups[i], level + 1);
		}
	}

}
