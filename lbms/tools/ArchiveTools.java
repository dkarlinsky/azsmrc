package lbms.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveTools {
	public static void unpackZip(File zipFile, File parentDir) throws IOException {
		ZipFile zip = new ZipFile(zipFile);
		Enumeration<ZipEntry> e =(Enumeration<ZipEntry>)zip.entries();
		try {
			while (e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				if (ze.isDirectory()) {
					new File (parentDir,ze.getName()).mkdirs();
				} else {
					InputStream input = null;
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(new File (parentDir,ze.getName()));
						input = zip.getInputStream(ze);
						for (int b=0;(b=input.read())!=-1;) {
							fos.write(b);
						}
					} finally {
						if (input != null) input.close();
						if (fos != null) fos.close();
					}
				}
				System.out.println(ze.getName());
			}
		} finally {
			zip.close();
		}
	}

	public static Enumeration<ZipEntry> getZipEntries (File zipFile) throws IOException {
		ZipFile zip = new ZipFile(zipFile);
		Enumeration<ZipEntry> e =(Enumeration<ZipEntry>)zip.entries();
		zip.close();
		return e;
	}

	public static void unpackGZipFile(File gzipFile, File parentDir) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(gzipFile);
			GZIPInputStream gis = new GZIPInputStream(fis);
			fos = new FileOutputStream(new File(parentDir,gzipFile.getName().replace(".gz", "")));
			for (int b=0;(b=gis.read())!=-1;) {
				fos.write(b);
			}
		} finally {
			if (fis != null) fis.close();
			if (fos != null) fos.close();
		}

	}
}
