package lbms.azsmrc.remote.client.torrent;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TOTorrentFactoryTest {
    @Test
    public void shouldParseTorrentWithUdpAnnounceURL() throws Exception {
        TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedFile(new File(
                getClass().getClassLoader().getResource("test1.torrent").getPath()));
        System.out.println(torrent.getAnnounceURL());
    }

}
