var Server = window.location.href;
/* example request XML sent to Server
<?xml version="1.0" encoding="UTF-8"?>
<Request version="1.0">
  <Query switch="Ping" />
</Request>
*/
var HealthStates = ["", "gray",  "blue",  "yellow", "green", "red", "red"];
var selectedTransfers = [];
var selectableDetails = ["Name", "Position", "Download Average", "Upload Average", "Downloaded", "Uploaded", "Health", "Completition", "Availability", "ETA", "State", "Status", "Share Ratio", "Tracker Status", "Download Limit", "Upload Limit", "Connected Seeds", "Connected Leecher", "Total Seeds", "Total Leecher", "Discarded", "Size", "Elapsed Time", "Total Average", "Scrape Times", "All Seeds", "All Leecher"];
// standard selection
var selectedDetails = [1,1,0,0,1,1,1,1,0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0];
function doNothing() {
	// empty function doing nothing
	// used for links
}
function fetchData(xmlhttp) {
// callback function
// fetch data from responseText
	var doc = xmlhttp.responseXML; // allows to use DOM methods on XML document
	addDebugEntry("XML Response: "+xmlhttp.responseText);
	var results = doc.getElementsByTagName("Result");
	var result;
	// setting defaults
	document.getElementById("eventlist").style.display = "none";
	document.getElementById("eventstatus").firstChild.data = "no new events";
	for (var i in results)
		if (results[i].nodeType == 1) {
			result = results[i].getAttribute("switch")
			switch (result) {
				case "Events":
					// send events to handlers
					handleEvents(results[i]);
				break;
				case "Ping":
					var img = document.getElementById("connectionstatus");
					document.getElementById("ping").firstChild.data = "Ping: "+results[i].firstChild.data;
					if (results[i].firstChild.data == "Pong") {
						img.src = "img/connect_established.png";
						img.setAttribute("alt","Connection established");
						img.setAttribute("title","Connected to Server");
					}					
				break;
				case "listTransfers":
					handlelistTransfers(doc);
				break;
				default:
					addDebugEntry("unhandled request: "+result+" (aborting)");
				break;
			}
		}	
}
function getRequestOptions(request) {
	var options = "";
	switch (request) {
		case "listTransfers":
		// static for testings
			options = 0;
			for (var i in selectedDetails)
				if (selectedDetails[i] == 1) {
					options += Math.pow(2, i);
				}
			options = ' options="'+options+'"';
			return options;			
		break;
		default:
			return options;
		break;
	}
}
function initAzSMRCwebUI() {	
	initDebugLog()
	//showSplashScreen();
	initTabControl();
	PingToServer();
}
function PingToServer() {
	var img = document.getElementById("connectionstatus");
	img.src = "img/connect_no.png";
	img.setAttribute("alt","Connection not established");
	img.setAttribute("title","Not connected to Server");
	document.getElementById("ping").firstChild.data = "Ping: no response";
	SendRequestToServer(0);
}
function refreshView() {
	switch (tabs[activeTab]) {
		case "listTransfers":
			SendRequestToServer(1);
		break;
		case "debug":
			clearDebugLog();
		break;
		default: 
			PingToServer();
		break;
	}
}
function removeSplashScreen() {
	document.getElementById("splashscreen").style.display = "none";
}
function round(val,dig) {
	var fac = Math.pow(10,dig);
	return Math.round(val*fac)/fac;
}
function selectDetails(id) {
	id = id.substring(11,id.length);
	selectedDetails[id] = (document.getElementById("selDetails_"+id).checked == true) ? 1 : 0;
}
function selectTC(obj) {
	var input = obj.firstChild.firstChild;
	var hash = obj.getAttribute("hash");
	// visual
	input.checked = (input.checked == true) ? false : true;
	// set array content
	var inserted = false;
	if (input.checked == true) {
		for (var i in selectedTransfers) {
			if (selectedTransfers[i] == null) {
				selectedTransfers[i] = hash;
				inserted = true;
				break;
			}				
		}
		if (!inserted) {
			selectedTransfers[selectedTransfers.length] = hash;
		}
	} else
		for (var i in selectedTransfers) {
			if (selectedTransfers[i] == hash) {
				selectedTransfers[i] = null;
				break;
			}
		}
	// addDebugEntry("selected Transfers: "+selectedTransfers);
}
function SendRequestToServer(request) {
	request = registeredRequests[request];
	var xmlhttp;
	var statusbarentry = document.getElementById("requeststatus").firstChild;
	// respondig file fo server: process.cgi
	// edit line below if changes
	var requestURL = Server+"process.cgi";
	var options = getRequestOptions(request);
	var xmlrequest = '<?xml version="1.0" encoding="UTF-8"?><Request version="1.0"><Query switch="'+request+'"'+options+' /></Request>';
	addDebugEntry("XML Request: "+xmlrequest);
	try {
		if (window.XMLHttpRequest) {
			xmlhttp = new XMLHttpRequest();
		} else if(window.ActiveXObject) {
		    xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		}
	} catch(e) {
	    return false;
	}
	xmlhttp.open("POST", requestURL, true);
	xmlhttp.setRequestHeader("Content-type","application/xml"); 
	xmlhttp.setRequestHeader("Connection","close"); 
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
				fetchData(xmlhttp);		
			break;
			default:
				statusbarentry.data = "unknown state";
			break;
		}
	}
	xmlhttp.send(xmlrequest);
}
function showSplashScreen() {
	// just a funny splashscreen function
	document.getElementById("splashscreen").style.display = "block";
	setTimeout("removeSplashScreen()", 5000);
}
