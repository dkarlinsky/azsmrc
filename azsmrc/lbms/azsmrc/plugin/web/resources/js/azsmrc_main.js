var Server = window.location.href;
/* example request XML sent to Server
<?xml version="1.0" encoding="UTF-8"?>
<Request version="1.0">
  <Query switch="Ping" />
</Request>
*/
var HealthStates = ["", "gray",  "blue",  "yellow", "green", "red", "red"];
var formalHealthStates = ["", "torrent not running",  "not connected to any peer or tracker down",  "no remote connection, check NAT", "everything is fine", "not connected to any peer", "not connected to any peer"];
var selectedTransfers = [];
var selectableDetails = ["Name", "Position", "Download Average", "Upload Average", "Downloaded", "Uploaded", "Health", "Completition", "Availability", "ETA", "State", "Status", "Share Ratio", "Tracker Status", "Download Limit", "Upload Limit", "Connected Seeds", "Connected Leecher", "Total Seeds", "Total Leecher", "Discarded", "Size", "Elapsed Time", "Total Average", "Scrape Times", "All Seeds", "All Leecher"];
// standard selection
var selectedDetails = [1,1,0,0,1,1,1,1,0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0];
// SI Unit byte prefix
var SI_byte = ["bytes", "kB", "MB", "GB", "TB"];
// max positions [dl, ul]
var positions = [0, 0];
function adjustMaxTabWidth() {
	var maxwidth = Math.floor(window.innerWidth*0.98);
	for (var s = 0; s < document.styleSheets.length; s++)
		for (var r = 0; r < document.styleSheets[s]["cssRules"].length; r++)
			if ((document.styleSheets[s]["cssRules"][r].selectorText == "div.tab") ||
				(document.styleSheets[s]["cssRules"][r].selectorText == "div.moveTab")) {
				document.styleSheets[s]["cssRules"][r].style["maxWidth"] = maxwidth+"px";
				document.styleSheets[s]["cssRules"][r].style["max-width"] = maxwidth+"px";
			}
}
function configAutoRefresh() {
	var regTabID, i;
	for (i in autoRefreshObjs)
		if (autoRefreshObjs[i] != null)
			window.clearInterval(autoRefreshObjs[i]);
	for (i in tabs) 
		if (tabs[i]) {
			regTabID = getRegTabById(i);
			if (autoRefresh[regTabID] > 0) {
				autoRefreshObjs[regTabID] = window.setInterval("doAutoRefresh("+regTabID+")", autoRefresh[regTabID]);
			}
		}
}
function createXMLHTTP() {
	var xmlhttp = null;
	try {
		if (window.XMLHttpRequest) {
			xmlhttp = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
		    xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		}
	} catch(e) {
	    return false;
	}
	return xmlhttp;
}
function doAutoRefresh(regTabID) {
	if (refreshRequests[regTabID] > -1) 
		SendRequestToServer(refreshRequests[regTabID]);
}
function doNothing() {
	// empty function doing nothing
	// used for links
}
function fetchData(xmlhttp) {
// callback function
// fetch data from responseText
	var doc = xmlhttp.responseXML; // allows to use DOM methods on XML document
	addDebugEntry("XML Response: "+xmlhttp.responseText);
	if (xmlhttp.responseText != "") {
		var results = doc.getElementsByTagName("Result");
		var result;
		// setting defaults
		//document.getElementById("eventlist").style.display = "none";
		//document.getElementById("eventstatus").firstChild.data = "no new events";
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
					case "getRemoteInfo":
						document.getElementById("azversion").firstChild.data = "Azureus "+results[i].getAttribute("azureusVersion");
						document.getElementById("azsmrcversion").firstChild.data = "AzSMRC "+results[i].getAttribute("pluginVersion");
					break;
					case "getUsers":
						if (!document.getElementById("userTable"))
							addTab("userManagement");
						else {
							var userTable = document.getElementById("userTableBody");
							var users = doc.getElementsByTagName("User");
							while (userTable.firstChild) userTable.removeChild(userTable.firstChild);
							var tr, td;
							for (var j in users) 
								if (j < users.length) {
									tr = document.createElement("tr");
									td = document.createElement("td");
									td.appendChild(document.createTextNode(users[j].getAttribute("username")));
									tr.appendChild(td);
									td = document.createElement("td");
									td.appendChild(document.createTextNode(users[j].getAttribute("outputDir")));
									tr.appendChild(td);
									td = document.createElement("td");
									td.appendChild(document.createTextNode(users[j].getAttribute("autoImportDir")));
									tr.appendChild(td);
									td = document.createElement("td");
									td.appendChild(document.createTextNode(users[j].getAttribute("downloadSlots")));
									tr.appendChild(td);
									td = document.createElement("td");
									td.appendChild(document.createTextNode((users[j].getAttribute("userRights") == 1) ? "true" : "false"));
									tr.appendChild(td);
									userTable.appendChild(tr);
								}
						}
					break;
					default:
						addDebugEntry("unhandled request: "+result+" (aborting)");
					break;
				}
			}
	} else PingToServer();
}
function getLoadType(hash) {
	var tab = document.getElementById("tab_"+getTabIdByContent("listTransfers"));
	var dlTabs = tab.childNodes[3].firstChild;
	// check weither real download table
	if (dlTabs.firstChild.firstChild.data != "Downloads")
		return 1;
	dlTabs = dlTabs.childNodes[2];
	for (var i in dlTabs.childNodes)
		if (i < dlTabs.childNodes.length)
			if (dlTabs.childNodes[i].getAttribute("hash") == hash)
				return 0;
	return 1;
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
		case "addDownload":
			var torrentURL = document.getElementById("torrentURL").value;
			var torrentUser = document.getElementById("torrentUser").value;
			var torrentPasswd = document.getElementById("torrentPasswd").value;
			if ((torrentURL != null) && (torrentURL != "")) {
				options = ' url="'+torrentURL+'"';
			}
			if (torrentUser && torrentPasswd) {
				options += ' username="'+torrentUser+'" password="'+torrentPasswd+'"';
			}
			return options;
		break;
		default:
			return options;
		break;
	}
}
function getRequestQuery(req, par) {
	var request = '';
	switch (req) {
		case "listTransfers":
			request += '<Query switch="'+req+'"'+getRequestOptions(req)+' />';
		break;
		case "addDownload":
			request += '<Query switch="'+req+'" location="url"'+getRequestOptions(req)+' />';
			if (request == '<Query switch="'+req+'" location="url" />')
				request = '';
		break;	
		case "removeDownload":			
			if (!window.confirm("Are you sure to delete all selected downloads and uploads?"))
				break;			
			if (par) {
				request += '<Query switch="'+req+'" hash="'+par+'" />';
			} else
				for (var i in selectedTransfers)
					if (selectedTransfers[i] != null) {
						request += '<Query switch="'+req+'" hash="'+selectedTransfers[i]+'" />';
						selectedTransfers[i] = null;
					}
		break;
		case "setForceStart":
			request += '<Query switch="'+req+'" start="false" hash="'+par+'" />';
		break;
		case "recheckDataDownload":
		case "restartDownload":
		case "startDownload":
		case "stopDownload":
		case "stopAndQueueDownload":
		case "moveUp":
		case "moveDown":
		case "requestDownloadScrape":
		case "requestDownloadAnnounce":
			if (par) {
				request += '<Query switch="'+req+'" hash="'+par+'" />';
			} else
				for (var i in selectedTransfers)
					if (selectedTransfers[i] != null)
						request += '<Query switch="'+req+'" hash="'+selectedTransfers[i]+'" />';
			
		break;
		case "moveToPosition":
			for (var i in selectedTransfers)
				if (selectedTransfers[i] != null) {
					if (par == "top")
						position = 1;
					else if (par == "bottom")
						position = positions[getLoadType(selectedTransfers[i])];
					else 
						position = par;
					request += '<Query switch="'+req+'" position="'+position+'" hash="'+selectedTransfers[i]+'" />';
				}
		break;
		default:
			request += '<Query switch="'+req+'" />';
		break;	
	}
	return request;
}
function getTCState(hash) {
	for (var i in selectedTransfers)
		if (selectedTransfers[i] == hash)
			return true;
	return false;
}
function initAzSMRCwebUI() {
	initDebugLog();
	initCookies();
	initContextMenu();
	addTab("listTransfers");
	initTabControl();
	adjustMaxTabWidth();
	setJSHint(); 
	//showSplashScreen();
	configAutoRefresh();
	PingToServer();
	SendRequestToServer(40);
	refreshView();
	init_dragdrop('tab', true);
	//initTableSort();
	//alert(window.innerWidth);
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
			selectedTransfers = [];
			// listTransfers
			SendRequestToServer(1);
		break;
		case "debug":
			clearDebugLog();
		break;
		case "about":
			// getRemoteInfo
			SendRequestToServer(40);
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
function savePreferences() {
	var value = null;
	// read data
	for (var i in registeredTabs) {
		if (refreshRequests[i] > -1) {
			value = Math.floor(document.getElementById("cookie_autorefresh_"+i).value);
			if (!Math.floor(value))
				document.getElementById("cookie_autorefresh_"+i).value = 0;
			autoRefresh[i] = value > 0 ? value : 0;			
		}
		if (i > 0)
			startupTabs[i] = document.getElementById("startup_"+i).checked;
	}
	configAutoRefresh();
	// cookies
	var now = new Date();
	fixDate(now);
	// expires after one year
	now.setTime(now.getTime() + 365 * 24 * 60 * 60 * 1000);
	// startup tabs
	value = startupTabs.join(",");
	setCookie("startupTabs", value, now);
	// auto refresh	
	var saveCookie = document.getElementById("cookie_autorefresh").checked;	
	if (saveCookie) {
		// set autorefresh cookie
		value = autoRefresh.join(",");
		setCookie("autoRefresh", value, now);
	}
	addDebugEntry("saved Cookies: "+document.cookie);
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
	} else {
		for (var i in selectedTransfers) {
			if (selectedTransfers[i] == hash) {
				selectedTransfers[i] = null;
				break;
			}
		}
	}
	obj.className = (input.checked == true)? "activeTC" : "";
	// addDebugEntry("selected Transfers: "+selectedTransfers);
}
function SendRequestToServer(request, par) {
	request = registeredRequests[request];
	// respondig file fo server: process.cgi
	// edit line below if changes
	var requestURL = Server+"process.cgi";
	var xmlhttp = createXMLHTTP();
	if (xmlhttp) {
		var statusbarentry = document.getElementById("requeststatus").firstChild;
		var xmlrequest = '<?xml version="1.0" encoding="UTF-8"?><Request version="1.0">'+getRequestQuery(request, par)+'</Request>';
		// no empty requests
		if (xmlrequest != '<?xml version="1.0" encoding="UTF-8"?><Request version="1.0"></Request>') {
			addDebugEntry("XML Request: "+xmlrequest);
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
	}
}
function setJSHint() {
	var body = document.getElementsByTagName("body")[0];
	body.firstChild.setAttribute("id", "JS-Hint");
}
function showSplashScreen() {
	// just a funny splashscreen function
	document.getElementById("splashscreen").style.display = "block";
	setTimeout("removeSplashScreen()", 5000);
}