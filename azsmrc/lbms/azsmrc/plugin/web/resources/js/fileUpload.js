function addTorrentContent() {
	var div = document.createElement("div");
	var head = document.createElement("h2");
	head.appendChild(document.createTextNode("Torrent Control"));
	div.appendChild(head);
	head = document.createElement("h3");
	head.appendChild(document.createTextNode("Add Torrent by File Upload"));
	div.appendChild(head);
	var p = document.createElement("p");
	p.appendChild(document.createTextNode("under construction"));
	p.className = "hint";
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
	
	head = document.createElement("h3");
	head.appendChild(document.createTextNode("Add Torrent by URL"));
	div.appendChild(head);
	form = document.createElement("form");
	p = document.createElement("p");
	p.appendChild(document.createTextNode("http, ftp and magnet works"));
	p.className = "hint";
	form.appendChild(p);
	label = document.createElement("label");
	label.setAttribute("for", "torrentURL");
	label.appendChild(document.createTextNode("Torrent URL"));
	input = document.createElement("input");
	input.setAttribute("type", "text");
	input.setAttribute("id", "torrentURL");
	input.value = "http://";
	form.appendChild(label);
	form.appendChild(input);
	p = document.createElement("p");
	p.appendChild(document.createTextNode("Leave information below empty if torrent is public"));
	p.className = "hint";
	form.appendChild(p);
	
	label = document.createElement("label");
	label.setAttribute("for", "torrentUser");
	label.appendChild(document.createTextNode("Torrent user"));
	input = document.createElement("input");
	input.setAttribute("type", "text");
	input.setAttribute("id", "torrentUser");
	form.appendChild(label);
	form.appendChild(input);
	
	label = document.createElement("label");
	label.setAttribute("for", "torrentPasswd");
	label.appendChild(document.createTextNode("Torrent password"));
	input = document.createElement("input");
	input.setAttribute("type", "password");
	input.setAttribute("id", "torrentPasswd");
	form.appendChild(label);
	form.appendChild(input);
	
	input = document.createElement("input");
	input.setAttribute("type", "button");
	input.value = "Send to Server";
	input.onclick = function () {
		SendRequestToServer(3);
	}
	form.appendChild(input);
	div.appendChild(form);	
	return div;
}
function encode64(input) {
	var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	var output = "";
	var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
	var i = 0;
	while (i < input.length) {
		chr1 = input.charCodeAt(i++);
		chr2 = input.charCodeAt(i++);
		chr3 = input.charCodeAt(i++);

		enc1 = chr1 >> 2;
		enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
		enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
		enc4 = chr3 & 63;

		if (isNaN(chr2)) {
			enc3 = enc4 = 64;
		} else if (isNaN(chr3)) {
			enc4 = 64;
		}
		output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) + keyStr.charAt(enc3) + keyStr.charAt(enc4);
	}
	return output;
}
function uploadFile() {
	var binary;
	var filename = document.getElementById("torrentfile").value;
	// request more permissions
	try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	} catch (e) {
		alert("Permission to read file was denied.\nPlease check your security settings.");
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
		var filecontent = encode64(binary.readBytes(binary.available()));
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
		xmlhttp.setRequestHeader("Content-type", "application/xml;");
		xmlhttp.setRequestHeader("Connection", "close");
		xmlhttp.setRequestHeader("Content-length", xmlrequest.length);
		xmlhttp.send(xmlrequest);
	}
}