var binary;
function addUploadContent() {
	var div = document.createElement("div");
	var head = document.createElement("h2");
	head.appendChild(document.createTextNode("Torrent File Upload"));
	div.appendChild(head);
	var p = document.createElement("p");
	p.appendChild(document.createTextNode("under construction"));
	div.appendChild(p);
	var form = document.createElement("form");
	form.setAttribute("enctype", "multipart/form-data");
	var label = document.createElement("label");
	label.appendChild(document.createTextNode("TorrentFile"));
	label.setAttribute("for", "torrentfile");
	form.appendChild(label);
	var input = document.createElement("input");
	input.setAttribute("type", "file");
	input.setAttribute("id", "torrentfile");
	input.setAttribute("name", "torrentfile");
	form.appendChild(input);
	input = document.createElement("input");
	input.setAttribute("type", "button");
	input.setAttribute("name", "uplsubmit");
	input.setAttribute("value", "Send file");
	input.onclick = function () { uploadFile(); };
	form.appendChild(input);
	div.appendChild(form);
	return div;
}
function uploadFile() {
	var filename = document.getElementById("torrentfile").value;
	// request more permissions
	try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	} catch (e) {
		alert("Permission to read file was denied.");
	}	
	// open the local file
	var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath(filename);
	var stream = Components.classes["@mozilla.org/network/file-input-stream;1"].createInstance(Components.interfaces.nsIFileInputStream);
	stream.init(file, 0x01, 00004, null);
	var bstream =  Components.classes["@mozilla.org/network/buffered-input-stream;1"].getService();
	bstream.QueryInterface(Components.interfaces.nsIBufferedInputStream);
	bstream.init(stream, 1000);
	bstream.QueryInterface(Components.interfaces.nsIInputStream);
	binary = Components.classes["@mozilla.org/binaryinputstream;1"].createInstance(Components.interfaces.nsIBinaryInputStream);
	binary.setInputStream(stream);
	
	var requestURL = Server+"process.cgi";
	var xmlhttp = createXMLHTTP();	
	if (xmlhttp) {
		var statusbarentry = document.getElementById("requeststatus").firstChild;
		var filecontent = escape(binary.readBytes(binary.available()));
		var boundaryString = 'base64';
		var xmlrequest = '<?xml version="1.0" encoding="UTF-8"?><Request version="1.0">'
			+ '<Query switch="addDownload" location="xml">'
			+ '<Torrent>'+ filecontent +'</Torrent>'
			+ '</Query>'
			+ '</Request>';
		addDebugEntry("XML Request: "+xmlrequest);
		xmlhttp.onreadystatechange = function () {
			switch (xmlhttp.readyState) {
				case 0:
					statusbarentry.data = "uninitialized";
				break;
				case 1:
					statusbarentry.data = "open request";
				break;
				case 2:
					statusbarentry.data = "request sent";
				break;
				case 3:
					statusbarentry.data = "receiving response";
				break;
				case 4:
					statusbarentry.data = "response loaded";	
					addDebugEntry("XML Response: "+xmlhttp.responseText);
				break;
				default:
					statusbarentry.data = "unknown state";
				break;
			}
		}
		xmlhttp.open("POST", requestURL, true);
		xmlhttp.setRequestHeader("Content-type", "application/xml; boundary=\"" + boundaryString + "\"");
		xmlhttp.setRequestHeader("Connection", "close");
		xmlhttp.setRequestHeader("Content-length", xmlrequest.length);
		xmlhttp.send(xmlrequest);
	}
}