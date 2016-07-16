/*
 * File    : TOTorrentImpl.java
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
import java.net.*;
import java.util.*;


import lbms.azsmrc.remote.client.torrent.*;
import lbms.azsmrc.remote.client.util.ByteFormatter;
import lbms.azsmrc.remote.client.util.HashWrapper;
import lbms.azsmrc.remote.client.util.SHA1Hasher;
import lbms.azsmrc.shared.*;




public class
TOTorrentImpl
	implements TOTorrent
{
	protected static final String TK_ANNOUNCE			= "announce";
	protected static final String TK_ANNOUNCE_LIST		= "announce-list";
	protected static final String TK_COMMENT			= "comment";
	protected static final String TK_CREATION_DATE		= "creation date";
	protected static final String TK_CREATED_BY			= "created by";

	protected static final String TK_INFO				= "info";
	protected static final String TK_NAME				= "name";
	protected static final String TK_LENGTH				= "length";
	protected static final String TK_PATH				= "path";
	protected static final String TK_FILES				= "files";
	protected static final String TK_PIECE_LENGTH		= "piece length";
	protected static final String TK_PIECES				= "pieces";

	protected static final String TK_PRIVATE			= "private";

	protected static final String TK_NAME_UTF8			= "name.utf-8";
	protected static final String TK_PATH_UTF8			= "path.utf-8";
	protected static final String TK_COMMENT_UTF8		= "comment.utf-8";

	protected static final List<String> TK_ADDITIONAL_OK_ATTRS =
		Arrays.asList(new String[]{TK_COMMENT_UTF8, AZUREUS_PROPERTIES });

	private byte[]							torrent_name;
	private byte[]							torrent_name_utf8;

	private byte[]							comment;
	private URI								announce_url;
	private TOTorrentAnnounceURLGroupImpl	announce_group = new TOTorrentAnnounceURLGroupImpl(this);

	private long		piece_length;
	private int			number_of_pieces;
	private byte[][]	pieces;

	private byte[]		torrent_hash;
	private HashWrapper	torrent_hash_wrapper;

	private boolean				simple_torrent;
	private TOTorrentFileImpl[]	files;

	private long				creation_date;
	private byte[]				created_by;

	private Map					additional_properties 		= new HashMap();
	private Map					additional_info_properties	= new HashMap();


	/**
	 * Constructor for deserialisation
	 */

	protected
	TOTorrentImpl()
	{
	}

	/**
	 * Constructor for creation
	 */

	protected
	TOTorrentImpl(
		String		_torrent_name,
		URI			_announce_url,
		boolean		_simple_torrent )

		throws TOTorrentException
	{
		try{

			torrent_name		= _torrent_name.getBytes( RemoteConstants.DEFAULT_ENCODING );

			torrent_name_utf8	= torrent_name;

			setAnnounceURL( _announce_url );

			simple_torrent		= _simple_torrent;

		}catch( UnsupportedEncodingException e ){

			throw( new TOTorrentException( 	"TOTorrent: unsupported encoding for '" + _torrent_name + "'",
											TOTorrentException.RT_UNSUPPORTED_ENCODING));
		}
	}

	public void
	serialiseToBEncodedFile(
		final File		output_file )

		throws TOTorrentException
	{
		byte[]	res = serialiseToByteArray();

		BufferedOutputStream bos = null;

		try{
			File parent = output_file.getParentFile();

			if ( parent == null ){

				throw( new Exception( "Path '" + output_file + "' is invalid" ));
			}

			parent.mkdirs();

			File temp = new File( parent, output_file.getName() + ".saving");

			bos = new BufferedOutputStream( new FileOutputStream( temp, false ), 8192 );

			bos.write( res );

			bos.flush();

			bos.close();

			bos = null;

			  //only use newly saved file if it got this far, i.e. it was written successfully

			if ( temp.length() > 1L ) {

				if ( output_file.exists() ) {

					output_file.delete();
				}

				temp.renameTo( output_file );
			}

		}catch( Throwable e){

			throw( new TOTorrentException( 	"TOTorrent::serialise: fails '" + e.toString() + "'",
															TOTorrentException.RT_WRITE_FAILS ));

		}finally{

			if ( bos != null ){

				try{
					bos.close();

				}catch( IOException e ){

					e.printStackTrace();
				}
			}
		}
	}

	public byte[]
	serialiseToByteArray()

		throws TOTorrentException
	{
		Map	root = serialiseToMap();

		try{
			return( BEncoder.encode( root ));

		}catch( IOException e ){

			throw( 	new TOTorrentException(
							"TOTorrent::serialiseToByteArray: fails '" + e.toString() + "'",
							TOTorrentException.RT_WRITE_FAILS ));

		}
	}

	public Map
	serialiseToMap()

		throws TOTorrentException
	{
		Map<String, Object>	root = new HashMap<String, Object>();

		writeStringToMetaData( root, TK_ANNOUNCE, announce_url.toString());

		TOTorrentAnnounceURLSet[] sets = announce_group.getAnnounceURLSets();

		if (sets.length > 0 ){

			List<List<byte[]>>	announce_list = new ArrayList<List<byte[]>>();

			for (TOTorrentAnnounceURLSet set : sets) {

				URI[] urls = set.getAnnounceURLs();

				if (urls.length == 0) {

					continue;
				}

				List<byte[]> sub_list = new ArrayList<byte[]>();

				announce_list.add(sub_list);

				for (URI url : urls) {

					sub_list.add(writeStringToMetaData(url.toString()));
				}
			}

			if ( announce_list.size() > 0 ){

				root.put( TK_ANNOUNCE_LIST, announce_list );
			}
		}

		if ( comment != null ){

			root.put( TK_COMMENT, comment );
		}

		if ( creation_date != 0 ){

			root.put( TK_CREATION_DATE, new Long( creation_date ));
		}

		if ( created_by != null ){

			root.put( TK_CREATED_BY, created_by );
		}

		Map info = new HashMap();

		root.put( TK_INFO, info );

		info.put( TK_PIECE_LENGTH, new Long( piece_length ));

		if ( pieces == null ){

			throw( new TOTorrentException( "Pieces is null", TOTorrentException.RT_WRITE_FAILS ));
		}

		byte[]	flat_pieces = new byte[pieces.length*20];

		for (int i=0;i<pieces.length;i++){

			System.arraycopy( pieces[i], 0, flat_pieces, i*20, 20 );
		}

		info.put( TK_PIECES, flat_pieces );

		info.put( TK_NAME, torrent_name );

		if ( torrent_name_utf8 != null ){

			info.put( TK_NAME_UTF8, torrent_name_utf8 );
		}

		if ( simple_torrent ){

			TOTorrentFile	file = files[0];

			info.put( TK_LENGTH, new Long( file.getLength()));

		}else{

			List	meta_files = new ArrayList();

			info.put( TK_FILES, meta_files );

			for (int i=0;i<files.length;i++){

				TOTorrentFileImpl	file	= files[i];

				Map	file_map = new HashMap();

				meta_files.add( file_map );

				file_map.put( TK_LENGTH, new Long( file.getLength()));

				List path = new ArrayList();

				file_map.put( TK_PATH, path );

				byte[][]	path_comps = file.getPathComponents();

				for (int j=0;j<path_comps.length;j++){

					path.add( path_comps[j]);
				}

				if ( file.isUTF8()){

					List utf8_path = new ArrayList();

					file_map.put( TK_PATH_UTF8, utf8_path );

					for (int j=0;j<path_comps.length;j++){

						utf8_path.add( path_comps[j]);
					}
				}

				Map file_additional_properties = file.getAdditionalProperties();

				Iterator prop_it = file_additional_properties.keySet().iterator();

				while( prop_it.hasNext()){

					String	key = (String)prop_it.next();

					file_map.put( key, file_additional_properties.get( key ));
				}
			}
		}

		Iterator info_it = additional_info_properties.keySet().iterator();

		while( info_it.hasNext()){

			String	key = (String)info_it.next();

			info.put( key, additional_info_properties.get( key ));
		}

		Iterator it = additional_properties.keySet().iterator();

		while( it.hasNext()){

			String	key = (String)it.next();

			Object	value = additional_properties.get( key );

			if ( value != null ){

				root.put( key, value );
			}
		}

		return( root );
	}

	public byte[]
	getName()
	{
		return( torrent_name );
	}

	protected void
	setName(
		byte[]	_name )
	{
		torrent_name	= _name;
	}

	public boolean
	isSimpleTorrent()
	{
		return( simple_torrent );
	}

	public byte[]
	getComment()
	{
		return( comment );
	}

	protected void
	setComment(
		byte[]		_comment )

	{
		comment = _comment;
	}

	public void
	setComment(
		String	_comment )
	{
		try{

			byte[]	utf8_comment = _comment.getBytes( RemoteConstants.DEFAULT_ENCODING );

			setComment( utf8_comment );

			setAdditionalByteArrayProperty( TK_COMMENT_UTF8, utf8_comment );

		}catch( UnsupportedEncodingException e ){

			e.printStackTrace();

			comment = null;
		}
	}

	public URI
	getAnnounceURL()
	{
		return( announce_url );
	}

	public boolean
	setAnnounceURL(
		URI		url )
	{
		URI newURL = anonymityTransform( url );
		String s0 = (newURL == null) ? "" : newURL.toString();
		String s1 = (announce_url == null) ? "" : announce_url.toString();
		if (s0.equals(s1))
			return false;

		announce_url	= newURL;
		return true;
	}

	public long
	getCreationDate()
	{
		return( creation_date );
	}

	public void
	setCreationDate(
		long		_creation_date )
	{
		creation_date 	= _creation_date;
	}

	protected void
	setCreatedBy(
		byte[]		_created_by )
	{
		created_by	= _created_by;
	}

	protected void
	setCreatedBy(
		String		_created_by )
	{
		try{

			setCreatedBy( _created_by.getBytes( RemoteConstants.DEFAULT_ENCODING ));

		}catch( UnsupportedEncodingException e ){

			e.printStackTrace();

			created_by = null;
		}
	}

	public byte[]
	getCreatedBy()
	{
		return( created_by );
	}

	public byte[]
	getHash()

		throws TOTorrentException
	{
		if ( torrent_hash == null ){

			Map	root = serialiseToMap();

			Map info = (Map)root.get( TK_INFO );

			setHashFromInfo( info );
		}

		return( torrent_hash );
	}

	public HashWrapper
	getHashWrapper()

		throws TOTorrentException
	{
		if ( torrent_hash_wrapper == null ){
			getHash();
		}

		return( torrent_hash_wrapper );
	}

	public boolean
	hasSameHashAs(
		TOTorrent		other )
	{
		try{
			byte[]	other_hash = other.getHash();

			return( Arrays.equals( getHash(), other_hash ));

		}catch( TOTorrentException e ){

			e.printStackTrace();

			return( false );
		}
	}

	protected void
	setHashFromInfo(
		Map		info )

		throws TOTorrentException
	{
		try{
			SHA1Hasher s = new SHA1Hasher();

			torrent_hash = s.calculateHash(BEncoder.encode(info));

			torrent_hash_wrapper = new HashWrapper( torrent_hash );

		}catch( Throwable e ){

			throw( new TOTorrentException( 	"TOTorrent::setHashFromInfo: fails '" + e.toString() + "'",
											TOTorrentException.RT_HASH_FAILS ));
		}
	}

	public void
	setPrivate(
		boolean	_private_torrent )

		throws TOTorrentException
	{
		additional_info_properties.put( TK_PRIVATE, new Long(_private_torrent?1:0));

			// update torrent hash

		torrent_hash	= null;

		getHash();
	}

	public boolean
	getPrivate()
	{
		Object o = additional_info_properties.get( TK_PRIVATE );

		if ( o instanceof Long ){

			return(((Long)o).intValue() != 0 );
		}

		return( false );
	}

	public TOTorrentAnnounceURLGroup
	getAnnounceURLGroup()
	{
		return( announce_group );
	}

	protected void
	addTorrentAnnounceURLSet(
		URI[]		urls )
	{
		announce_group.addSet( new TOTorrentAnnounceURLSetImpl( this, urls ));
	}

	public long
	getSize()
	{
		long	res = 0;

		for (int i=0;i<files.length;i++){

			res += files[i].getLength();
		}

		return( res );
	}

	public long
	getPieceLength()
	{
		return( piece_length );
	}

	protected void
	setPieceLength(
		long	_length )
	{
		piece_length	= _length;
	}

	public int
	getNumberOfPieces()
	{
		return( number_of_pieces );
	}

	public byte[][]
	getPieces()
	{
		return( pieces );
	}

	public void
	setPieces(
		byte[][]	_pieces )
	{
		pieces = _pieces;

		if ( pieces != null ){

			number_of_pieces = pieces.length;
		}
	}

	public TOTorrentFile[]
	getFiles()
	{
		return( files );
	}

	protected void
	setFiles(
		TOTorrentFileImpl[]		_files )
	{
		files	= _files;
	}

	protected boolean
	getSimpleTorrent()
	{
		return( simple_torrent );
	}

	protected void
	setSimpleTorrent(
		boolean	_simple_torrent )
	{
		simple_torrent	= _simple_torrent;
	}

	protected Map
	getAdditionalProperties()
	{
		return( additional_properties );
	}

	public void
	setAdditionalStringProperty(
		String		name,
		String		value )
	{
		try{

			setAdditionalByteArrayProperty( name, writeStringToMetaData( value ));

		}catch( TOTorrentException e ){

				// hide encoding exceptions as default encoding must be available

			e.printStackTrace();
		}
	}

	public String
	getAdditionalStringProperty(
		String		name )
	{
		try{

			return( readStringFromMetaData( getAdditionalByteArrayProperty(name)));

		}catch( TOTorrentException e ){

				// hide encoding exceptions as default encoding must be available

			e.printStackTrace();

			return( null );
		}
	}

	public void
	setAdditionalByteArrayProperty(
		String		name,
		byte[]		value )
	{
		additional_properties.put( name, value );
	}

	public void
	setAdditionalProperty(
		String		name,
		Object		value )
	{
		if ( name instanceof String ){

			setAdditionalStringProperty(name,(String)value);

		}else{

			additional_properties.put( name, value );
		}
	}

	public byte[]
	getAdditionalByteArrayProperty(
		String		name )
	{
		Object	obj = additional_properties.get( name );

		if ( obj instanceof byte[] ){

			return((byte[])obj);
		}

		return( null );
	}

	public void
	setAdditionalLongProperty(
		String		name,
		Long		value )
	{
		additional_properties.put( name, value );
	}

	public Long
	getAdditionalLongProperty(
		String		name )
	{
		Object	obj = additional_properties.get( name );

		if ( obj instanceof Long ){

			return((Long)obj);
		}

		return( null );
	}

	public void
	setAdditionalListProperty(
		String		name,
		List		value )
	{
		additional_properties.put( name, value );
	}

	public List
	getAdditionalListProperty(
		String		name )
	{
		Object	obj = additional_properties.get( name );

		if ( obj instanceof List ){

			return((List)obj);
		}

		return( null );
	}

	public void
	setAdditionalMapProperty(
		String		name,
		Map 		value )
	{
		additional_properties.put( name, value );
	}

	public Map
	getAdditionalMapProperty(
		String		name )
	{
		Object	obj = additional_properties.get( name );

		if ( obj instanceof Map ){

			return((Map)obj);
		}

		return( null );
	}

	public Object
	getAdditionalProperty(
		String		name )
	{
		return(additional_properties.get( name ));
	}

	public void
	removeAdditionalProperty(
		String name )
	{
		additional_properties.remove( name );
	}

	public void
	removeAdditionalProperties()
	{
		Map	new_props = new HashMap();

		Iterator it = additional_properties.keySet().iterator();

		while( it.hasNext()){

			String	key = (String)it.next();

			if ( TK_ADDITIONAL_OK_ATTRS.contains(key)){

				new_props.put( key, additional_properties.get( key ));
			}
		}

		additional_properties = new_props;
	}

	protected void
	addAdditionalProperty(
		String			name,
		Object			value )
	{
		additional_properties.put( name, value );
	}

	protected void
	addAdditionalInfoProperty(
		String			name,
		Object			value )
	{
		additional_info_properties.put( name, value );
	}

	protected Map
	getAdditionalInfoProperties()
	{
		return( additional_info_properties );
	}

	protected String
	readStringFromMetaData(
		Map		meta_data,
		String	name )

		throws TOTorrentException
	{
		Object	obj = meta_data.get(name);

		if ( obj instanceof byte[]){

			return(readStringFromMetaData((byte[])obj));
		}

		return( null );
	}

	protected String
	readStringFromMetaData(
		byte[]		value )

		throws TOTorrentException
	{
		try{
			if ( value == null ){

				return( null );
			}

			return(	new String(value, RemoteConstants.DEFAULT_ENCODING ));

		}catch( UnsupportedEncodingException e ){

			throw( new TOTorrentException( 	"TOTorrentDeserialise: unsupported encoding for '" + value + "'",
											TOTorrentException.RT_UNSUPPORTED_ENCODING));
		}
	}

	protected void
	writeStringToMetaData(
		Map		meta_data,
		String	name,
		String	value )

		throws TOTorrentException
	{
		meta_data.put( name, writeStringToMetaData( value ));
	}

	protected byte[]
	writeStringToMetaData(
		String		value )

		throws TOTorrentException
	{
		try{

			return(	value.getBytes( RemoteConstants.DEFAULT_ENCODING ));

		}catch( UnsupportedEncodingException e ){

			throw( new TOTorrentException( 	"TOTorrent::writeStringToMetaData: unsupported encoding for '" + value + "'",
											TOTorrentException.RT_UNSUPPORTED_ENCODING));
		}
	}

	protected URI
	anonymityTransform(
		URI		url )
	{
		/*
		 * 	hmm, doing this is harder than it looks as we have issues hosting
		 *  (both starting tracker instances and also short-cut loopback for seeding
		 *  leave as is for the moment
		if ( HostNameToIPResolver.isNonDNSName( url.getHost())){

			// remove the port as it is uninteresting and could leak information about the
			// tracker

			String	url_string = url.toString();

			String	port_string = ":" + (url.getPort()==-1?url.getDefaultPort():url.getPort());

			int	port_pos = url_string.indexOf( ":" + url.getPort());

			if ( port_pos != -1 ){

				try{

					return( new URL( url_string.substring(0,port_pos) + url_string.substring(port_pos+port_string.length())));

				}catch( MalformedURLException e){

					Debug.printStackTrace(e);
				}
			}
		}
		*/

		return( url );
	}

	public void
	print()
	{
		try{
			byte[]	hash = getHash();

			System.out.println( "name = " + torrent_name );
			System.out.println( "announce url = " + announce_url );
			System.out.println( "announce group = " + announce_group.getAnnounceURLSets().length );
			System.out.println( "creation date = " + creation_date );
			System.out.println( "creation by = " + created_by );
			System.out.println( "comment = " + comment );
			System.out.println( "hash = " + ByteFormatter.nicePrint( hash ));
			System.out.println( "piece length = " + getPieceLength() );
			System.out.println( "pieces = " + getNumberOfPieces() );

			Iterator info_it = additional_info_properties.keySet().iterator();

			while( info_it.hasNext()){

				String	key = (String)info_it.next();
				Object	value = additional_info_properties.get( key );

				try{

					System.out.println( "info prop '" + key + "' = '" +
										( value instanceof byte[]?new String((byte[])value, RemoteConstants.DEFAULT_ENCODING):value.toString()) + "'" );
				}catch( UnsupportedEncodingException e){

					System.out.println( "info prop '" + key + "' = unsupported encoding!!!!");
				}
			}

			Iterator it = additional_properties.keySet().iterator();

			while( it.hasNext()){

				String	key = (String)it.next();
				Object	value = additional_properties.get( key );

				try{

					System.out.println( "prop '" + key + "' = '" +
										( value instanceof byte[]?new String((byte[])value, RemoteConstants.DEFAULT_ENCODING):value.toString()) + "'" );
				}catch( UnsupportedEncodingException e){

					System.out.println( "prop '" + key + "' = unsupported encoding!!!!");
				}
			}

			if ( pieces == null ){

				System.out.println( "\tpieces = null" );

			}else{
				for (int i=0;i<pieces.length;i++){

					System.out.println( "\t" + ByteFormatter.nicePrint(pieces[i]));
				}
			}

			for (int i=0;i<files.length;i++){

				byte[][]path_comps = files[i].getPathComponents();

				String	path_str = "";

				for (int j=0;j<path_comps.length;j++){

					try{

						path_str += (j==0?"":File.separator) + new String( path_comps[j], RemoteConstants.DEFAULT_ENCODING );

					}catch( UnsupportedEncodingException e ){

						System.out.println( "file - unsupported encoding!!!!");
					}
				}

				System.out.println( "\t" + path_str + " (" + files[i].getLength() + ")" );
			}
		}catch( TOTorrentException e ){

			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see org.gudy.azureus2.core3.logging.LogRelation#getLogRelationText()
	 */
	public String getRelationText() {
		return "Torrent: '" + new String(torrent_name) + "'";
	}

	/* (non-Javadoc)
	 * @see org.gudy.azureus2.core3.logging.LogRelation#queryForClass(java.lang.Class)
	 */
	/*
	public Object[] getQueryableInterfaces() {
		// yuck
		try {
			return new Object[] { AzureusCoreFactory.getSingleton()
					.getGlobalManager().getDownloadManager(this) };
		} catch (Exception e) {
		}

		return null;
	}*/
}
