/*
 * Created on 17-Jun-2005
 * Created by Paul Gardner
 * Copyright (C) 2005 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * AELITIS, SARL au capital de 30,000 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */

package lbms.azsmrc.plugin.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.plugin.main.history.DownloadHistory;
import lbms.azsmrc.plugin.main.history.DownloadHistoryEntry;
import lbms.azsmrc.shared.UserNotFoundException;

import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ipfilter.IPRange;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageGenerator;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.gudy.azureus2.plugins.utils.Utilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class WebRequestHandler	/*extends WebPlugin*/ implements TrackerWebPageGenerator {



	protected static final String	NL = "\r\n";

	protected static final String[]	welcome_pages = {"index.html", "index.htm", "index.php", "index.tmpl" };
	protected static File[]			welcome_files;

	protected PluginInterface	pluginInterface;	// unfortunately this is accessed by webui - fix sometime
	private LoggerChannel		log;

	private String				file_root;
	private String				resource_root = "lbms/azsmrc/plugin/web/resources";

	private boolean				ip_range_all = true;
	private IPRange				ip_range;

	public static final String	CATEGORY_UNKNOWN = "Uncategorized";

	protected boolean			view_mode;


	protected Comparator		comparator;

	protected TorrentAttribute	torrent_categories;

	protected String 			t_uploaded_name;

	public	WebRequestHandler(PluginInterface pluginInterface) {
		this.pluginInterface = pluginInterface;

		Utilities utilities = pluginInterface.getUtilities();

		comparator	= utilities.getFormatters().getAlphanumericComparator( true );

		torrent_categories = pluginInterface.getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);
		///////////////////////////////
		try {
			file_root = new File(pluginInterface.getPluginDirectoryName() + File.separator + "web").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			file_root =pluginInterface.getPluginDirectoryName() + File.separator + "web";
		}

		Plugin.addToLog(file_root);
		welcome_files = new File[welcome_pages.length];

		for (int i=0;i<welcome_pages.length;i++){

			welcome_files[i] = new File( file_root + File.separator + welcome_pages[i] );
		}
	}


	/*public void
	initialize(
		PluginInterface	_plugin_interface )

		throws PluginException
	{


		plugin_interface	= _plugin_interface;

		plugin_interface.addListener(
			new PluginListener()
			{
				public void
				initializationComplete()
				{
					try {
						tracker_plugin_interface = plugin_interface.getPluginManager().getPluginInterfaceByClass( "org.gudy.azureus2.ui.tracker.TrackerWebDefaultTrackerPlugin" );

						if( tracker_plugin_interface != null ) { //will be null if 'azplugins' plugin isn't installed

							tracker_plugin_config = tracker_plugin_interface.getPluginconfig();

							tracker_plugin_loaded  = true;
						}

					}catch(Exception e){
						e.printStackTrace();
					}
				}

				public void
				closedownInitiated()
				{

				}

				public void
				closedownComplete()
				{

				}
			});

		plugin_config		= plugin_interface.getPluginconfig();

		utilities = plugin_interface.getUtilities();

		comparator	= utilities.getFormatters().getAlphanumericComparator( true );

		download_manager	= plugin_interface.getDownloadManager();

		tracker = plugin_interface.getTracker();

		torrent_categories = plugin_interface.getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);
		///////////////////////////////
		file_root = utilities.getAzureusUserDir() + File.separator + "htmlwebui";

		welcome_files = new File[welcome_pages.length];

		for (int i=0;i<welcome_pages.length;i++){

			welcome_files[i] = new File( file_root + File.separator + welcome_pages[i] );
		}
		//////////////////////////////
		UIManager	ui_manager = plugin_interface.getUIManager();

			// config

		BasicPluginConfigModel config_model = ui_manager.createBasicPluginConfigModel( "plugins", "azhtmlwebui.name");

		config_model.addStringParameter2( CONFIG_HTMLWEBUI_TITLE, "azhtmlwebui.title", CONFIG_HTMLWEBUI_TITLE_DEFAULT );

		config_model.addBooleanParameter2(CONFIG_HTML_WEBUI_3TABS, "azhtmlwebui.completedtab", CONFIG_HTML_WEBUI_3TABS_DEFAULT);

			// log panel


		super.initialize( plugin_interface );


	}	*/


	// Map the Homepage URL.
	protected String mapHomePage( String url_in )
	{
	  if ( url_in.equals("/")){
		for (int i=0;i<welcome_files.length;i++){
		  if ( welcome_files[i].exists()){
			url_in = "/" + welcome_pages[i];
			return (url_in);
		  }
		}
	  }
	  return (url_in);
	}

	protected Map<String, String> decodeParams (
		String	str )
	{
		Map<String, String> params = new HashMap<String, String>();

		int	pos = 0;

		while(true){

			int	p1 = str.indexOf( '&', pos );
			String	bit;

			if ( p1 == -1 ){

				bit = str.substring(pos);

			}else{

				bit = str.substring(pos,p1);
				pos = p1+1;
			}

			int	p2 = bit.indexOf('=');

			if ( p2 == -1 ){

				params.put(bit,"true");

			}else{
				params.put(bit.substring(0, p2).toLowerCase(), bit
						.substring(p2 + 1));

			}

			if ( p1 == -1 ){

				break;
			}
		}

		return( params );
	}

	public boolean processXMLRequest (
			TrackerWebPageRequest		request,
			TrackerWebPageResponse		response )
	throws IOException {
		Plugin.addToLog("Should handle XML request");
		Map<String,String> headers = request.getHeaders();
		InputStream is = null;
		if (headers.containsKey("x-content-encoding") && headers.get("x-content-encoding").toLowerCase().indexOf("gzip") != -1) {
			//InputStream tis = request.getInputStream();
			//tis.reset();
			//is = new BufferedInputStream(new GZIPInputStream (tis));
			is = new GZIPInputStream (request.getInputStream());
			//is.mark(0);
			Plugin.addToLog("Content is Gzip encoded.");
		} else {
			is = request.getInputStream();
			//is.reset();
		}
		boolean useCompression = false;
		if (useOutputCompression(headers)) {
			useCompression = true;
			Plugin.addToLog("Usind Gzip Compression for output.");
		}
		User user = null;
		try {
			user = Plugin.getXMLConfig().getUser(request.getUser());
		} catch (UserNotFoundException e1) {
			e1.printStackTrace();
		}
		/*int c = is.read();
		while (c!=-1) {
			System.out.print((char)c);
			c = is.read();
		}*/
		try {
			//is.reset();
			SAXBuilder builder = new SAXBuilder();
			Document xmlDom = builder.build(is);
			new XMLOutputter(Format.getPrettyFormat()).output(xmlDom, System.out);
			RequestManager.getInstance().handleRequest(xmlDom, response, user, useCompression);
		} catch (JDOMException e) {
			Plugin.addToLog("Invalid XML request");
			return false;
		}
		return true;
	}


	/**
	 * @param headers
	 * @return
	 */
	private boolean useOutputCompression (Map<String, String> headers) {
		return headers.containsKey("accept-encoding") && headers.get("accept-encoding").toLowerCase().contains("gzip");
	}

	public boolean processFileUpload (
			TrackerWebPageRequest		request,
			TrackerWebPageResponse		response )
	throws IOException {
		try {
			User user = Plugin.getXMLConfig().getUser(request.getUser());
		/*	InputStream is = request.getInputStream();
			System.out.println();
			System.out.println("InputStream:");
			for (int i = is.read();i>0;i=is.read())
				System.out.print((char)i);
			System.out.println();
			System.out.println();*/

		} catch (UserNotFoundException e1) {
			e1.printStackTrace();
		}
		return true;
	}


	public boolean generateSupport(
		TrackerWebPageRequest		request,
		TrackerWebPageResponse		response )
		throws IOException
	{
		OutputStream	os = response.getOutputStream();

		try {

		String url = request.getURL();

		if ( url.equals("/")){
			url = "/index.htm";
		}

		url = mapHomePage( url );

		Plugin.addToLog("Handling: "+url);


		/*
		int	p_pos = url.indexOf( '?' );
		Hashtable	params = null;
		if ( p_pos != -1 ){

			params = decodeParams( url.substring( p_pos+1 ));

			url = url.substring(0,p_pos);
		}*/
		/////////////////////////////////////////////////////////////
		String	target = file_root + url.replace('/',File.separatorChar);
		File canonical_file = new File(target).getCanonicalFile();

		System.out.println("Trying to open File: "+canonical_file.getAbsolutePath());
			// make sure some fool isn't trying to use ../../ to escape from web dir
		if ( !canonical_file.toString().toLowerCase().startsWith( file_root.toLowerCase() )){
			System.out.println("Fileroot is not matched: "+file_root);
		} else if ( !canonical_file.isDirectory() && canonical_file.canRead()){
			System.out.println("Trying to send File.");
			String str = canonical_file.toString().toLowerCase();

			int	pos = str.lastIndexOf( "." );

			if ( pos == -1 ){

				return false;
			}

			String	file_type = str.substring(pos+1);

			FileInputStream	fis = null;

			try {
				fis = new FileInputStream(canonical_file);

				response.useStream( file_type, fis );

				return true ;

			} finally {

				if ( fis != null ) {

					fis.close();
				}
			}
		}
		////////////////////////////////////////////////////////////////
		InputStream is = WebRequestHandler.class.getClassLoader().getResourceAsStream(resource_root + url );//

		System.out.println("Trying to load per classloader: "+ url);
		if ( is == null ) {
			System.out.println("Couldn't open Stream.");
			return( false );
		}

		try	{
			int	pos = url.lastIndexOf( "." );

			if ( pos == -1 ) {

				System.out.println("Invalid file.");
				return false ;
			}
			System.out.println("Sending per Stream.");
			String	file_type = url.substring(pos+1);

			response.useStream( file_type, is );

			return true ;


		} finally {
			is.close();
		}
		} catch( Throwable e ) {

			e.printStackTrace();

			os.write( e.toString().getBytes());

			return( true );
		}


	}


	public boolean generate(
		TrackerWebPageRequest		request,
		TrackerWebPageResponse		response )
		throws IOException
	{
		Plugin.addToLog("Received request from: "+request.getClientAddress());
		//Plugin.addToLog(request.getHeader());
		if ( !ip_range_all ){
			String	client = request.getClientAddress();

			// System.out.println( "client = " + client );

			try{
				InetAddress ia = InetAddress.getByName( client );

				if ( ip_range == null ){

					if ( !ia.isLoopbackAddress()){

						log.log( LoggerChannel.LT_ERROR, "Client '" + client + "' is not local, rejecting" );

						return( false );
					}
				}else{

					if ( !ip_range.isInRange( ia.getHostAddress())){

						log.log( LoggerChannel.LT_ERROR, "Client '" + client + "' (" + ia.getHostAddress() + ") is not in range, rejecting" );

						return( false );
					}
				}
			}catch( Throwable e ){

				Debug.printStackTrace( e );

				return( false );
			}
		}

		final String requestUrlString = request.getURL().toString();
		Plugin.addToLog("URL was requested: "+requestUrlString);

		if ( requestUrlString.endsWith(".class")){

			System.out.println( "WebPlugin::generate:" + request.getURL());
		}

		if ( requestUrlString.equalsIgnoreCase("/process.cgi")) {
			try {
				return processXMLRequest(request, response);
			} catch (IOException e) {
				Plugin.addToLog("XML error", e);
				throw e;
			}
		}

		if (requestUrlString.toLowerCase().startsWith("/downloadhistory")) {
			try {
				return processDownloadHistory(request, response);
			} catch (IOException e) {
				Plugin.addToLog("Download History error", e);
				throw e;
			} catch (Exception e) {
				Plugin.addToLog("Download History error", e);
				throw new IOException(e);
			}
		}

		if ( requestUrlString.equalsIgnoreCase("/fileupload.cgi")) {
			try {
				return processFileUpload(request, response);
			} catch (IOException e) {
				Plugin.addToLog("Fileupload error", e);
				throw e;
			}
		}

		if ( generateSupport( request, response )){
			return(true);
		}

		response.setReplyStatus(404); //HTTP Status 404: NOT FOUND
		return( false );
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 * @throws JDOMException
	 * @throws TransformerException
	 */
	@SuppressWarnings("unchecked")
	private boolean processDownloadHistory (TrackerWebPageRequest request,
			TrackerWebPageResponse response) throws IOException,
			TransformerException, JDOMException {

		String url = request.getURL().toString();

		User user = null;
		try {
			user = Plugin.getXMLConfig().getUser(request.getUser());
			if (user == null) {
				return false;
			}
		} catch (UserNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}

		int p_pos = url.indexOf('?');
		Map<String, String> params = null;
		if (p_pos != -1) {

			params = decodeParams(url.substring(p_pos + 1));

			url = url.substring(0, p_pos);
		}
		if (params == null) {
			params = new HashMap<String, String>();
		}

		long now = System.currentTimeMillis();

		long endDate = now;
		long startDate = now - 24 * 60 * 60 * 1000; // one day history

		if (params.containsKey("last")) {
			Pattern lastFormat = Pattern.compile("(?:(\\d+)(d|h|m))+",
					Pattern.CASE_INSENSITIVE);
			Matcher m = lastFormat.matcher(params.get("last"));
			if (m.find()) {
				startDate = now;
				int count = m.groupCount();
				for (int i = 1; i <= count; i += 2) {
					int x = Integer.parseInt(m.group(i));
					String modifier = m.group(i + 1);
					if ("d".equalsIgnoreCase(modifier)) {
						startDate -= x * 24 * 60 * 60 * 1000;
					} else if ("h".equalsIgnoreCase(modifier)) {
						startDate -= x * 60 * 60 * 1000;
					} else if ("m".equalsIgnoreCase(modifier)) {
						startDate -= x * 60 * 1000;
					}

				}
			}
		} else if (params.containsKey("startDate")
				&& params.containsKey("endDate")) {
			startDate = Long.parseLong(params.get("startDate"));
			endDate = Long.parseLong(params.get("endDate"));
		}


		ContentEncoding ce = ContentEncoding.parseFromHeaders(request
				.getHeaders());

		DownloadHistoryEntry[] entries = DownloadHistory.getInstance()
				.getEntries(user, startDate / 1000, endDate / 1000);

		Arrays.sort(entries);

		if ("desc".equalsIgnoreCase(params.get("sort"))) {
			Arrays.sort(entries, Collections.reverseOrder());
		}

		OutputStream os = ce.wrapStream(response);

		Document rssDocument = renderDownloadHistoryAsRssDocument(entries, user
				.getUsername());

		if (params.containsKey("render")) {
			String render = params.get("render");
			if ("html".equalsIgnoreCase(render)) {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer(new StreamSource(WebRequestHandler.class.getClassLoader().getResourceAsStream(resource_root + "/rss2html.xsl" )));
				transformer.transform(new DOMSource(new DOMOutputter()
						.output(rssDocument)), new StreamResult(os));
			}
		} else {
			XMLOutputter out = new XMLOutputter();
			response.setContentType("text/xml");
			out.output(rssDocument, os);
		}

		os.close();

		return true;
	}

	/**
	 * @param entries
	 * @param os
	 * @param username
	 * @throws IOException
	 */
	private Document renderDownloadHistoryAsRssDocument (
			DownloadHistoryEntry[] entries, String username) throws IOException {

		final SimpleDateFormat rfc822dateFormat = new SimpleDateFormat(
				"EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

		Element root = new Element("rss");
		root.setAttribute("version", "2.0");
		Element channel = new Element("channel");
		root.addContent(channel);

		channel.addContent(new Element("title").setText("Download History ["
				+ username + "]"));
		channel.addContent(new Element("description")
				.setText("Download History provided by AzSMRC."));
		channel.addContent(new Element("ttl").setText("5"));

		for (DownloadHistoryEntry entry : entries) {
			final String rfc822Date = rfc822dateFormat.format(new Date(entry
					.getTimestamp()));

			Element item = new Element("item");
			item.addContent(new Element("title").setText(entry.getDlName()));
			item.addContent(new Element("description").setText(entry
					.getDlName()
					+ " has finished Downloading on " + rfc822Date));
			item.addContent(new Element("pubDate").setText(rfc822Date));

			channel.addContent(item);
		}

		return new Document(root);
	}

}


