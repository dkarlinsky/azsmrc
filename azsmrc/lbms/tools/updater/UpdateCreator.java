package lbms.tools.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lbms.tools.CryptoTools;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class UpdateCreator {

	private Update currentUpdate;
	private UpdateList uList;

	public UpdateCreator() {
		uList = new UpdateList();
		currentUpdate = new Update();
		uList.addUpdate(currentUpdate);
	}

	public UpdateCreator (File updateFile) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(updateFile);
			if (updateFile.toString().contains(".gz")) {
				uList = readCompressedUpdateList(fis);
				currentUpdate = uList.getLatest();
			} else {
				uList = readUpdateList(fis);
				currentUpdate = uList.getLatest();
			}
		} catch (IOException e) {
			e.printStackTrace();
			uList = new UpdateList();
			currentUpdate = new Update();
			uList.addUpdate(currentUpdate);
		} finally {
			if (fis!=null)
				try {
					fis.close();
				} catch (IOException e) {}
		}
	}

	private UpdateList readCompressedUpdateList (InputStream is) throws IOException {
		GZIPInputStream gis = new GZIPInputStream(is);
		return readUpdateList(gis);
	}

	private UpdateList readUpdateList (InputStream is) throws IOException {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document xmlDom = builder.build(is);
			UpdateList updateUl = new UpdateList(xmlDom);
			new XMLOutputter(Format.getPrettyFormat()).output(updateUl.toDocument(), System.out);
			return updateUl;
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return null;
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

		f.setHash(CryptoTools.formatByte(CryptoTools.messageDigestFile(file.getAbsolutePath(), UpdateFile.HASH_ALGORITHM)));
		currentUpdate.addFile(f);
		return f;
	}

	public void setZipArchivFiles (File file, UpdateFile uf) throws Exception {
		ZipFile zip = new ZipFile(file);
		Enumeration<ZipEntry> e =(Enumeration<ZipEntry>)zip.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = e.nextElement();
			UpdateFile f = new UpdateFile();
			f.setInArchive(true);
			f.setUrl("");
			f.setPath("");
			f.setSize(ze.getSize());
			f.setType(UpdateFile.TYPE_DND);
			String fn = ze.getName();
			f.setName(fn);
			int pos = fn.indexOf('_');
			int end = fn.lastIndexOf('.');
			if (pos ==-1 || end-pos < 2) {
				f.setVersion(new Version("0"));
			} else {
				f.setVersion(new Version(fn.substring(pos+1, end)));
			}
			InputStream in = zip.getInputStream(ze);
			f.setHash(CryptoTools.formatByte(CryptoTools.messageDigestStream(in, UpdateFile.HASH_ALGORITHM)));
			in.close();
		}
		zip.close();
	}

	public void removeFile (UpdateFile f) {
		currentUpdate.removeFile(f);
	}

	public Update getCurrentUpdate() {
		return this.currentUpdate;
	}

	/**
	 * @param currentUpdate The currentUpdate to set.
	 */
	public void setCurrentUpdate(Update currentUpdate) {
		this.currentUpdate = currentUpdate;
	}

	/**
	 * @return Returns the uList.
	 */
	public UpdateList getUpdateList() {
		return uList;
	}

	public boolean construct (File file, boolean compress) throws IOException {
		//if (!currentUpdate.isComplete()) return false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (compress) {
				GZIPOutputStream gos = new GZIPOutputStream(fos);
				new XMLOutputter(Format.getCompactFormat()).output(uList.toDocument(), gos);
				gos.close();
			} else {
				new XMLOutputter(Format.getPrettyFormat()).output(uList.toDocument(), fos);
			}
		} finally {
			if (fos!=null) fos.close();
		}
		return true;
	}

	public String generateChangelog(Update u) {
		String changelog = "";
		List<String> clog = u.getChangeLog().getFeatures();
		changelog += "Changelog for Version: "+currentUpdate.getVersion()+"\n\n";
		if (clog.size()>0) {
			changelog += "***New Features***\n\n";
			for (String c:clog) {
				changelog += "- "+c+"\n";
			}
			changelog += "\n";
		}
		clog = u.getChangeLog().getChanges();
		if (clog.size()>0) {
			changelog += "***Changes***\n\n";
			for (String c:clog) {
				changelog += "- "+c+"\n";
			}
			changelog += "\n";
		}
		clog = u.getChangeLog().getBugFixes();
		if (clog.size()>0) {
			changelog += "***BugFixes***\n\n";
			for (String c:clog) {
				changelog += "- "+c+"\n";
			}
		}
		return changelog;
	}

	public boolean generateChangelogTxt (File f) throws IOException {
		if (currentUpdate.getChangeLog()==null) return false;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(f);
			pw.println(generateChangelog(getCurrentUpdate()));
		} finally {
			if (pw!=null) pw.close();
		}
		return true;
	}

	public boolean generateCombinedChangelogTxt (File f) throws IOException {
		if (currentUpdate.getChangeLog()==null) return false;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(f);
			Iterator<Update> uIter = uList.getUpdateSet().iterator();
			while (uIter.hasNext()) {
				pw.println(generateChangelog(uIter.next()));
				pw.println("\n");
			}
		} finally {
			if (pw!=null) pw.close();
		}
		return true;
	}
}
