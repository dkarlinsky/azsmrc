package lbms.tools.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lbms.tools.CryptoTools;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class UpdateCreator {

	private Update update;

	public UpdateCreator() {
		update = new Update();
	}

	public UpdateCreator (File updateFile) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(updateFile);
			if (updateFile.toString().contains(".gz")) {
				update = readCompressedUpdate(fis);
			} else {
				update = readUpdate(fis);
			}
		} catch (IOException e) {
			e.printStackTrace();
			update = new Update();
		} finally {
			if (fis!=null)
				try {
					fis.close();
				} catch (IOException e) {}
		}
	}

	private Update readCompressedUpdate (InputStream is) throws IOException {
		GZIPInputStream gis = new GZIPInputStream(is);
		return readUpdate(gis);
	}

	private Update readUpdate (InputStream is) throws IOException {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document xmlDom = builder.build(is);
			return new Update(xmlDom);
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return null;
	}

	public UpdateFile addFile(File file, String path, String url, int type) throws Exception{
		UpdateFile f = new UpdateFile();
		f.setUrl(url);
		f.setName(file.getName());
		f.setPath(path);
		f.setSize(file.length());
		String fn = file.getName();

		int pos = fn.indexOf('_');
		int end = fn.lastIndexOf('.');
		if (pos ==-1 || end-pos < 2) {
			f.setVersion(new Version("0"));
		} else {
			f.setVersion(new Version(fn.substring(pos+1, end)));
		}

		f.setType(type);
		f.setHash(CryptoTools.formatByte(CryptoTools.messageDigestFile(file.getAbsolutePath(), "SHA-1")));
		update.addFile(f);
		return f;
	}

	public void removeFile (UpdateFile f) {
		update.removeFile(f);
	}

	public Update getUpdate() {
		return this.update;
	}

	public boolean construct (File file, boolean compress) throws IOException {
		if (!update.isComplete()) return false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (compress) {
				GZIPOutputStream gos = new GZIPOutputStream(fos);
				new XMLOutputter(Format.getCompactFormat()).output(update.toDocument(), gos);
				gos.close();
			} else {
				new XMLOutputter(Format.getPrettyFormat()).output(update.toDocument(), fos);
			}
		} finally {
			if (fos!=null) fos.close();
		}
		return true;
	}

	public boolean generateChanglogTxt (File f) throws IOException {
		if (update.getChangeLog()==null) return false;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(f);
			List<String> clog = update.getChangeLog().getFeatures();
			pw.println("Changelog for Version: "+update.getVersion()+"\n");
			if (clog.size()>0) {
				pw.println("***New Features***\n");
				for (String c:clog) {
					pw.println("- "+c);
				}
				pw.println();
			}
			clog = update.getChangeLog().getChanges();
			if (clog.size()>0) {
				pw.println("***Changes***\n");
				for (String c:clog) {
					pw.println("- "+c);
				}
				pw.println();
			}
			clog = update.getChangeLog().getBugFixes();
			if (clog.size()>0) {
				pw.println("***BugFixes***\n");
				for (String c:clog) {
					pw.println("- "+c);
				}
			}
		} finally {
			if (pw!=null) pw.close();
		}

		return true;
	}
}
