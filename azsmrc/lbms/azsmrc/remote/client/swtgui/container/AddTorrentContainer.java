/*
 * Created on Mar 22, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.container;

import java.io.File;
import java.io.UnsupportedEncodingException;

import lbms.azsmrc.remote.client.torrent.TOTorrent;
import lbms.azsmrc.remote.client.torrent.TOTorrentException;
import lbms.azsmrc.remote.client.torrent.TOTorrentFactory;
import lbms.azsmrc.remote.client.torrent.TOTorrentFile;
import lbms.azsmrc.remote.client.torrent.scraper.ScrapeResult;
import lbms.azsmrc.shared.RemoteConstants;

public class AddTorrentContainer {

	private TOTorrent torrent;
	private int[] fileProperties;
	private File fTorrentFile;
	private ScrapeResult sr;
	private String saveTo = "";

	public AddTorrentContainer(File torrentFile) throws TOTorrentException{
		torrent = TOTorrentFactory.deserialiseFromBEncodedFile(torrentFile);
		fTorrentFile = torrentFile;
		fileProperties = new int[torrent.getFiles().length];
		for(int i = 0; i < fileProperties.length; i++){
			fileProperties[i] = 1;
		}
	}

	/**
	 * Obtain the name of the torrent by parsing the byte[]
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getName() throws UnsupportedEncodingException{
		return torrent.getName() instanceof byte[]?new String((byte[])torrent.getName(), RemoteConstants.DEFAULT_ENCODING):torrent.getName().toString();
	}

	/**
	 * Obtain how many files are IN the torrent
	 * @return
	 */
	public int getNumberOfFile(){
		return torrent.getFiles().length;
	}


	/**
	 *
	 * @return int[] fileProperties
	 */
	public int[] getFileProperties(){
		return fileProperties;
	}

	/**
	 * Return the TOTorrent Itself
	 * @return
	 */
	public TOTorrent getTorrent(){
		return torrent;
	}


	/**
	 * Obtain the torrent File
	 * @return
	 */
	public File getTorrentFile(){
		return fTorrentFile;
	}

	/**
	 * Deletes the file associated with this container
	 * @return
	 */
	public boolean deleteFile(){
		return fTorrentFile.delete();
	}


	/**
	 * Searches through the properties and returns false if any file property is not == 1
	 * @return
	 */
	public boolean isWholeFileSent(){
		for(int i = 0; i < fileProperties.length; i++){
			if(fileProperties[i] != 1) return false;
		}
		return true;
	}

	/**
	 * Any file in the torrent that has a property of 1 will be counted and sized
	 * @return
	 */
	public long getTotalSizeOfDownloads(){
		if(fileProperties == null)
			return torrent.getSize();
		else{
			long size = 0;
			for(int i = 0; i < fileProperties.length; i++){
				if(fileProperties[i] == 1){
					size += torrent.getFiles()[i].getLength();
				}
			}
			return size;
		}
	}

	/**
	 * obtain all of the files from the torrent
	 * @return
	 */
	public TOTorrentFile[] getFiles(){
		return torrent.getFiles();
	}

	/**
	 * Pull the string of the parent (return the path of the file)
	 * @return
	 */
	public String getFilePath(){
		return fTorrentFile.getParent();
	}

	/**
	 * If this container is used in the scrape dialog and has been scraped, we return the SR here
	 * @return
	 */
	public ScrapeResult getScrapeResults(){
		return sr;
	}

	/**
	 * If this container is used in the scrape dialog and has been sraped, we store the SR here
	 * @param scrapeResults
	 */
	public void setScrapeResults(ScrapeResult scrapeResults){
		sr = scrapeResults;
	}


	/**
	 * Set the file properties.. needs to be an array of int as large as the number of files in the torrent
	 * set to the following:
	 * 0 = DND
	 * 1 = Normal
	 * 2 = High
	 * @param iProperties
	 */
	public void setFileProperties(int[] iProperties){
		fileProperties = iProperties;
	}

	/**
	 * Sets one file's properties at give fileNumber in the int[] fileProperties
	 * PROPERTY:
	 * 0 = DND
	 * 1 = Normal
	 * 2 = High
	 * @param fileNumber
	 * @param property
	 */
	public void setFileProperty(int fileNumber, int property){
		fileProperties[fileNumber] = property;
	}

	/**
	 * Set a specific save to directory
	 * @param dir
	 */
	public void setSaveToDirectory(String dir){
		saveTo = dir;
	}

	/**
	 * Returns the specific save to directory
	 * @return String dir
	 */
	public String getSaveToDirectory(){
		return saveTo;
	}
}

