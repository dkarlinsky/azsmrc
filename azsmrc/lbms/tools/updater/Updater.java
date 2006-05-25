package lbms.tools.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lbms.tools.CryptoTools;
import lbms.tools.Download;
import lbms.tools.HTTPDownload;
import lbms.tools.LowPriorityDeamonThread;
import lbms.tools.SFDownload;
import lbms.tools.ArchiveTools;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Updater {
	private final static int THREAD_POOL_SIZE = 2;
	ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE,new LowPriorityDeamonThread());

	private List<UpdateListener> listeners = new Vector<UpdateListener>();
	private List<UpdateProgressListener> progressListeners = new Vector<UpdateProgressListener>();

	private URL remoteUpdateFile;
	private File currentUpdates;
	private File dir;
	private File tmpDir;

	private UpdateList localUpdateList;
	private UpdateList remoteUpdateList;
	private Update choosenUpdate;
	private boolean updateAvailable = false;
	private boolean failed = false;
	private String lastError = "";

	public Updater (URL remoteUpdateFile, File currentUpdates, File dir) {
		this.remoteUpdateFile = remoteUpdateFile;
		this.currentUpdates = currentUpdates;
		this.dir = dir;
	}

	/**
	 * @return Returns the error.
	 */
	protected boolean isFailed() {
		return failed;
	}

	/**
	 * @return Returns the updateAvailable.
	 */
	protected boolean isUpdateAvailable() {
		return updateAvailable;
	}

	public void checkForUpdates(final boolean beta) {
		Thread t = new Thread (new Runnable() {
			public void run() {
				InputStream is = null;
				FileInputStream fis = null;
				try {
					HttpURLConnection conn = (HttpURLConnection)remoteUpdateFile.openConnection();
					conn.setDoInput(true);
					is =  conn.getInputStream();
					if (remoteUpdateFile.toExternalForm().contains(".gz")) {
						remoteUpdateList = readCompressedUpdateList(is);
					} else {
						remoteUpdateList = readUpdateList(is);
					}
					if (beta) {
						choosenUpdate = remoteUpdateList.getLatest();
					} else {
						choosenUpdate = remoteUpdateList.getLatestStable();
					}
					if (currentUpdates!=null && currentUpdates.exists()) {
						fis = new FileInputStream(currentUpdates);
						if (currentUpdates.toString().contains(".gz")) {
							localUpdateList = readCompressedUpdateList(fis);
						} else {
							localUpdateList = readUpdateList(fis);
						}

						if (localUpdateList.getLatest().compareTo(choosenUpdate) < 0) {
							updateAvailable = true;
							callListenerUpdate(choosenUpdate);
						} else {
							callListenerNoUpdate();
						}
					} else {
						updateAvailable = true;
						callListenerUpdate(choosenUpdate);
					}
				} catch (IOException e) {
					callListenerException(e);
					e.printStackTrace();
				} finally {
					if (is!= null) {
						try {
							is.close();
						} catch (IOException e) {}
					}
					if (fis!= null) {
						try {
							fis.close();
						} catch (IOException e) {}
					}
				}

			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.setDaemon(true);
		t.start();
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
			callListenerException(e);
			e.printStackTrace();
		}
		return null;
	}

	public void doUpdate() {
		if (!updateAvailable) return;
		tmpDir = new File(dir,"_Update");
		tmpDir.mkdir();
		tmpDir.deleteOnExit();
		Thread t = new Thread (new Runnable() {
			public void run() {
				System.out.println("Updater: commencing update");
				List<Download> downloads = new ArrayList<Download>();
				List<UpdateFile> files = choosenUpdate.getFileList();
				List<UpdateFile> filesToDL = new Vector<UpdateFile>();
				for (UpdateFile u:files) {
					try {
						URL remoteLoc;
						if (u.getUrl() != null && u.getUrl().length() != 0) {
							remoteLoc = new URL(u.getUrl());
						} else {
							remoteLoc = new URL (choosenUpdate.getUrl()+"/"+u.getPath()+u.getName());
						}
						File localLoc = new File(dir,u.getPath()+u.getName());
						File localTmp = new File(tmpDir,u.getPath()+u.getName());
						if (localLoc.exists()) {
							try {
								String localHash = CryptoTools.formatByte(CryptoTools.messageDigestFile(localLoc.getAbsolutePath(), "SHA-1"));
								if (localHash.equalsIgnoreCase(u.getHash())) {
									System.out.println("Updater: file exists: "+localLoc);
									continue;
								}
							} catch (Exception e) {
								e.printStackTrace();
								callListenerException(e);
							}
						} else if (u.isArchive()) { //if the file doesn't exist check if it is an archive, when all files inside the archive exist
													//we don't need to download it
							List<UpdateFile> archiveFiles = u.getArchivFiles();
							boolean exist = true;
							for (UpdateFile a:archiveFiles) {
								File aLoc = new File(dir,a.getPath()+a.getName());
								if (aLoc.exists()) {
									try {
										String localHash = CryptoTools.formatByte(CryptoTools.messageDigestFile(aLoc.getAbsolutePath(), "SHA-1"));
										if (localHash.equalsIgnoreCase(a.getHash())) {
											System.out.println("Updater: file exists: "+aLoc);
											continue;
										}
									} catch (Exception e) {
										e.printStackTrace();
										callListenerException(e);
									}
								} else {
									exist = false;
									break;
								}
							}
							if (exist) continue; // all Files inside the archive exist so we don't need to download it.
						}
						filesToDL.add(u);
						Download dl;
						switch (u.getType()) {
						case UpdateFile.TYPE_NORMAL:
							dl = new HTTPDownload (remoteLoc,localTmp);
							break;
						case UpdateFile.TYPE_SF_NET:
							dl = new SFDownload (remoteLoc,localTmp);
							break;
						default:
							dl=null;
						}
						if (dl!=null) {
							downloads.add(dl);
							System.out.println("Updater: adding to DL: "+dl.getSource().toExternalForm());
						}
					} catch (MalformedURLException e) {
						callListenerException(e);
						e.printStackTrace();
					}
				}
				callListenerInitializeUpdate(downloads.toArray(new Download[0]));
				callProgressListenerStateChanged(UpdateProgressListener.STATE_INITIALIZING);
				//Need to typecast here since java screws up
				List<Callable<Download>> x = new ArrayList<Callable<Download>>(downloads);
				if (downloads.size()!=0) {
					System.out.println("Updater: Downloading: "+x.size());
					try {
						callProgressListenerStateChanged(UpdateProgressListener.STATE_DOWNLOADING);
						List<Future<Download>> result = threadPool.invokeAll(x);
						boolean check = true;
						while (check) {
							check = false;

							for (Future<Download> test:result) {
								if (!test.isDone()) check = true;
								/*else {
									System.out.println("Updater: Remaining: "+result.size()+"/"+x.size());
									result.remove(test);
								}*/
							}
							Thread.sleep(50);
						}

					} catch (InterruptedException e) {
						callListenerException(e);
						e.printStackTrace();
					}

					//check if all files for the update are present
					for (UpdateFile u:filesToDL) {
						File localTmp = new File(tmpDir,u.getPath()+u.getName());
						File localLoc = new File(dir,u.getPath()+u.getName());
						if (!(localTmp.exists() || localLoc.exists())) {
							failed = true;
							lastError = "File "+u.getName()+" is missing";
							callListenerUpdateError(lastError);
						}
					}

					if (!failed) { //if all files for the update are present
						callProgressListenerStateChanged(UpdateProgressListener.STATE_INSTALLING);
						for (UpdateFile u:filesToDL) {
							File localTmp = new File(tmpDir,u.getPath()+u.getName());
							File localLoc = new File(dir,u.getPath()+u.getName());
							if (localTmp.exists()) {
								try {
									String localHash = CryptoTools.formatByte(CryptoTools.messageDigestFile(localTmp.getAbsolutePath(), "SHA-1"));
									if (!localHash.equalsIgnoreCase(u.getHash())) {
										failed = true;
										lastError = "File "+u.getName()+" failed Hash check";
										callListenerUpdateError(lastError);
										localTmp.delete();
										continue;
									}
									if (u.isArchive()) {
										if (u.getName().endsWith(".gz")) {
											ArchiveTools.unpackGZipFile(localTmp, dir);
										} else if (u.getName().endsWith(".zip")){
											ArchiveTools.unpackZip(localTmp, dir);
										}
										localTmp.delete(); //Delete the Archive after unpack
									} else {
										localTmp.renameTo(localLoc);
									}
								} catch (Exception e) {
									failed = true;
									lastError = e.getMessage();
									e.printStackTrace();
									callListenerException(e);
								}
							} else if (!localLoc.exists()) {
								failed = true;
								lastError = "File doesn't exist"+localTmp.getAbsolutePath();
								callListenerUpdateError(lastError);
							}
						}
					}
				}
				if (failed) {
					callListenerUpdateFailed(lastError);
					callProgressListenerStateChanged(UpdateProgressListener.STATE_ERROR);
				} else {
					if (currentUpdates != null) {
						OutputStream os = null;
						try {
							if (currentUpdates.getName().endsWith(".gz")) {
								os = new GZIPOutputStream(new FileOutputStream(currentUpdates));
							} else {
								os = new FileOutputStream(currentUpdates);
							}
							new XMLOutputter(Format.getCompactFormat()).output(remoteUpdateList.toDocument(), os);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (os!=null)
								try {
									os.close();
								} catch (IOException e) {}
						}
					}
					callProgressListenerStateChanged(UpdateProgressListener.STATE_FINISHED);
					callListenerUpdateFinished();
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.setDaemon(true);
		t.start();
	}


	/**
	 * @return Returns the localUpdateList.
	 */
	public UpdateList getLocalUpdateList() {
		return localUpdateList;
	}

	/**
	 * @return Returns the remoteUpdate.
	 */
	public UpdateList getRemoteUpdateList() {
		return remoteUpdateList;
	}

	/**
	 * @return Returns the choosenUpdate.
	 */
	public Update getChoosenUpdate() {
		return choosenUpdate;
	}

	/**
	 * @param choosenUpdate The choosenUpdate to set.
	 */
	public void setChoosenUpdate(Update choosenUpdate) {
		this.choosenUpdate = choosenUpdate;
	}

	private void callListenerException (Exception e) {
		System.out.println("UpdateException " + e.getMessage());
		for (UpdateListener l:listeners) {
			System.out.println("X");
			l.exception(e);
		}
	}

	private void callListenerUpdate(Update u) {
		for (UpdateListener l:listeners) {
			l.updateAvailable(u);
		}
	}

	private void callListenerNoUpdate() {
		for (UpdateListener l:listeners) {
			l.noUpdate();
		}
	}

	private void callListenerUpdateFailed(String reason) {
		for (UpdateListener l:listeners) {
			l.updateFailed(reason);
		}
	}

	private void callListenerUpdateError(String error) {
		for (UpdateListener l:listeners) {
			l.updateError(error);
		}
	}

	private void callListenerUpdateFinished() {
		for (UpdateListener l:listeners) {
			l.updateFinished();
		}
	}

	private void callListenerInitializeUpdate(Download[] dls) {
		for (UpdateListener l:listeners) {
			l.initializeUpdate(dls);
		}
	}

	private void callProgressListenerStateChanged (int newState) {
		for (UpdateProgressListener l:progressListeners) {
			l.stateChanged(newState);
		}
	}

	public void addListener (UpdateListener l) {
		listeners.add(l);
	}

	public void removeListener (UpdateListener l) {
		listeners.remove(l);
	}

	public void addProgressListener (UpdateProgressListener l) {
		progressListeners.add(l);
	}

	public void removeProgressListener (UpdateProgressListener l) {
		progressListeners.remove(l);
	}
}
