/*
 * File    : TOTorrentFileImpl.java
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


import java.io.*;
import java.util.*;

import org.gudy.azureus2.core3.util.FileUtil;

import lbms.azsmrc.remote.client.internat.LocaleUtil;
import lbms.azsmrc.remote.client.internat.LocaleUtilDecoder;
import lbms.azsmrc.remote.client.torrent.*;
import lbms.azsmrc.shared.RemoteConstants;


public class
TOTorrentFileImpl
	implements TOTorrentFile
{
	protected TOTorrent	torrent;
	protected long		file_length;
	protected byte[][]	path_components;

	protected Map		additional_properties = new HashMap();

	protected boolean	is_utf8;

	protected
	TOTorrentFileImpl(
		TOTorrent		_torrent,
		long			_len,
		String			_path )

		throws TOTorrentException
	{
		torrent	= _torrent;

		is_utf8	= true;

		try{

			file_length			= _len;

			Vector	temp = new Vector();

			int	pos = 0;

			while(true){

				int	p1 = _path.indexOf( File.separator, pos );

				if ( p1 == -1 ){

					temp.add( _path.substring( pos ).getBytes( RemoteConstants.DEFAULT_ENCODING ));

					break;
				}

				temp.add( _path.substring( pos, p1 ).getBytes( RemoteConstants.DEFAULT_ENCODING ));

				pos = p1+1;
			}

			path_components		= new byte[temp.size()][];

			temp.copyInto( path_components );

			checkComponents();

		}catch( UnsupportedEncodingException e ){

			throw( new TOTorrentException( 	"Unsupported encoding for '" + _path + "'",
											TOTorrentException.RT_UNSUPPORTED_ENCODING));
		}
	}

	protected
	TOTorrentFileImpl(
		TOTorrent		_torrent,
		long			_len,
		byte[][]		_path_components )

		throws TOTorrentException
	{
		torrent				= _torrent;
		file_length			= _len;
		path_components		= _path_components;

		checkComponents();
	}

	protected void
	checkComponents()

		throws TOTorrentException
	{
		for (int i=0;i<path_components.length;i++){

			byte[]	comp = path_components[i];

			if (	comp.length == 2 &&
					comp[0] == (byte)'.' &&
					comp[1] == (byte)'.' ){

				throw( 	new TOTorrentException(
						"Torrent file contains illegal '..' component",
						TOTorrentException.RT_DECODE_FAILS ));
			}
		}
	}

	public TOTorrent
	getTorrent()
	{
		return( torrent );
	}

	public long
	getLength()
	{
		return( file_length );
	}

	public byte[][]
	getPathComponents()
	{
		return( path_components );
	}

	protected boolean
	isUTF8()
	{
		return( is_utf8 );
	}

	protected void
	setAdditionalProperty(
		String		name,
		Object		value )
	{
		additional_properties.put( name, value );
	}

	protected Map
	getAdditionalProperties()
	{
		return( additional_properties );
	}

	public String getRelativePath() {
		if (torrent == null)
			return "";
		String sRelativePath = "";

		LocaleUtilDecoder decoder = null;
		try {
			decoder = LocaleUtil.getSingleton()
				.getTorrentEncodingIfAvailable(torrent);
		} catch (Exception e) {
			// Do Nothing
		}

		if (decoder != null) {
			for (int j = 0; j < path_components.length; j++) {

				try {
					String comp;
					try {
						comp = decoder.decodeString(path_components[j]);
					} catch (UnsupportedEncodingException e) {
						System.out.println("file - unsupported encoding!!!!");
						try {
							comp = new String(path_components[j]);
						} catch (Exception e2) {
							comp = "UnsupportedEncoding";
						}
					}

					comp = FileUtil.convertOSSpecificChars(comp);

					sRelativePath += (j == 0 ? "" : File.separator) + comp;
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

		}
		return sRelativePath;
	}
}
