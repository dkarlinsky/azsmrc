/*
 * File    : TOTorrentAnnounceURLSetImpl.java
 * Created : 5 Oct. 2003
 * By      : Parg 
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package lbms.azsmrc.remote.client.torrent.impl;

import java.net.URI;
import java.net.URL;

import lbms.azsmrc.remote.client.torrent.*;

public class
TOTorrentAnnounceURLSetImpl
	implements TOTorrentAnnounceURLSet
{
	private TOTorrentImpl	torrent;
	private URI[]			urls;

	protected
	TOTorrentAnnounceURLSetImpl(
		TOTorrentImpl	_torrent,
		URI[]			_urls )
	{
		torrent	= _torrent;

		setAnnounceURLs( _urls );
	}

	public URI[]
	getAnnounceURLs()
	{
		return( urls );
	}


	public void
	setAnnounceURLs(
		URI[]	_urls )
	{
		urls	= new URI[_urls.length];

		for (int i=0;i<urls.length;i++){

			urls[i]	= torrent.anonymityTransform( _urls[i] );
		}
	}
}
