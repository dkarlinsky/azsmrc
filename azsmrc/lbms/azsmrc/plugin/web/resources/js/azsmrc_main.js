var Server = "http://blackhawk.serveftp.com:49009/"; // server uri
// static while developing can be easily set to dynamic through confirmation or form requesting
// has to be accessable in any way for the client
/* request XML sent to Server
<?xml version="1.0" encoding="UTF-8"?>
<Request version="1.0">
  <Query switch="Ping" />
</Request>
*/
var HealthStates = ["", "gray",  "blue",  "yellow", "green", "red", "red"];
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
	for (var i in results)
		if (results[i].nodeType == 1) {
			result = results[i].getAttribute("switch")
			switch (result) {
				case "Ping":
					document.getElementById("ping").firstChild.data = "Ping: "+results[i].firstChild.data;
				break;
				case "listTransfers":
					handlelistTransfers(doc);
				break;
				default:
					alert("unknown request: aborting");
				break;
			}
		}	
}
function getRequestOptions(request) {
	var options = "";
	switch (request) {
		case "listTransfers":
		// static for testings
			options = ' options="3184"';
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
	SendRequestToServer(0);
}
function refreshView() {

}
function removeSplashScreen() {
	document.getElementById("splashscreen").style.display = "none";
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
