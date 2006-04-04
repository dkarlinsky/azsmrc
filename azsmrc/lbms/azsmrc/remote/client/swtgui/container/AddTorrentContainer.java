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
import lbms.azsmrc.shared.RemoteConstants;

public class AddTorrentContainer {

    private TOTorrent torrent;
    private int[] fileProperties;
    private File fTorrentFile;


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
}

