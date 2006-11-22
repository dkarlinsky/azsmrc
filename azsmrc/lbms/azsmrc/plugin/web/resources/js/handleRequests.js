// attributes that are allowed to be shown
var attributes = ["health", "position", "name", "state", "status", "downloaded", "uploaded", "forceStart", "downloadAVG", "uploadAVG", "totalAVG", "elapsedTime", "eta", "availability", "completition", "shareRatio", "tracker", "downloadLimit", "uploadLimit", "total_seeds", "total_leechers", "size", "last_scrape", "next_scrape", "hash"];
// enable attributefiltering for tables
var onlyDLAtt = [0,0,0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,1,0,0,0,0,0,0,0]; 
var onlyULAtt = [0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];
// output strings for attributes
// empty string defines no output
var formalAttributes = ["Health", "#", "Name", "", "Status", "Downloaded", "Uploaded", "", "Down Speed", "Up Speed", "Total Speed", "Elapsed time", "ETA", "Availability", "Done", "Share Ratio", "Tracker Status", "max. DL speed", "max UL speed", "Seeds", "Peers", "Size", "Scrape", "next Scrape", ""];
// event types
var eventTypes = ["unknown", "Download Completed", "Download Torrent Removed", "Download Exception", "System Exception", "Update Available", "Message", "Error Message", "Plugin Message"];
// additional interactions listed in interaction menu
// with selected torrents
var interactions = [["request download scrape",17], ["request download announce", 18]];
// with all torrents at once
var globalinteractions = [["start all downloads", 21], ["resume all downloads", 24], ["stop all downloads",22]];
// system (admin) interactions
var systeminteractions = [["restart Azureus", 39]];
function addlistTransfersInteraction() {
	var div, form, label, selector, p, link, button, i, ul, li;
	// details selection for interaction
	div = document.getElementById("interaction_container");
	if (!div) { 
		div = document.createElement("div");
		div.setAttribute("id", "interaction_container");
	} else
		while (div.firstChild) div.removeChild(div.firstChild);	
	ul = document.createElement("ul");
	ul.setAttribute("title", "Submenu");
	// label selection
	form = document.createElement("form");
	form.setAttribute("id", "labelselectionform");
	p = document.createElement("p");
	p.appendChild(document.createTextNode("Select information to show in DL/UL tables."));
	p.className = "hint";
	form.appendChild(p);
	for (i in selectableDetails) {
		label = document.createElement("label");
		label.appendChild(document.createTextNode(selectableDetails[i]));
		label.setAttribute("for", "selDetails_"+i);				
		selector = document.createElement("input");
		selector.setAttribute("type", "checkbox");
		selector.setAttribute("id", "selDetails_"+i);
		selector.checked = (selectedDetails[i] == 1) ? true : false;
		selector.setAttribute("value", "show "+selectableDetails[i]);
		selector.onchange = function () { selectDetails(this.getAttribute("id")); };
		form.appendChild(label);
		form.appendChild(selector);
	}
	button = document.createElement("input");
	button.setAttribute("type", "button");
	button.setAttribute("value", "Close");
	button.setAttribute("title", "Close Form");
	button.className = "closeButton";
	button.onclick = function () {
		document.getElementById("labelselectionform").style.display = "none";
		if (window.confirm("Do you want to apply the new settings?\n\tClick OK for refresh!"))
			refreshView();
	};
	form.appendChild(button);
	link = document.createElement("a");
	link.setAttribute("title", "Open Labelselection");
	link.onclick = function () { document.getElementById("labelselectionform").style.display = "block"; };
	link.appendChild(document.createTextNode("Open Labelselection"));	
	li = document.createElement("li");
	li.appendChild(link);
	ul.appendChild(li);
	div.appendChild(form);
	// torrent interaction
	form = document.createElement("form");
	form.setAttribute("id", "interactionform");
	p = document.createElement("p");
	p.appendChild(document.createTextNode("interact with selected torrents"));
	p.className = "hint";
	form.appendChild(p);
	
	form.appendChild(createTorrentInteractions());
	
	button = document.createElement("input");
	button.setAttribute("type", "button");
	button.setAttribute("value", "Close");
	button.setAttribute("title", "Close Form");
	button.className = "closeButton";
	button.onclick = function () { document.getElementById("interactionform").style.display = "none"; };
	form.appendChild(button);	
	link = document.createElement("a");
	link.setAttribute("title", "Open Advanced Interactionmenu");
	link.onclick = function () { document.getElementById("interactionform").style.display = "block"; };
	link.appendChild(document.createTextNode("Open Advanced Interactionmenu"));
	li = document.createElement("li");
	li.appendChild(link);
	ul.appendChild(li);	
	div.appendChild(ul);
	div.appendChild(form);	
	div.style.display = "block";
	return div;
}
function addUserManagement() {
	var div = document.createElement("div");
	var heading = document.createElement("h2");
	heading.appendChild(document.createTextNode("User Management"));
	div.appendChild(heading);
	
	var userTable, tr, td, tbody;
	userTable = document.createElement("table");
	userTable.setAttribute("id", "userTable");
	userTable.setAttribute("summary", "List of users for AzSMRC");
	userTable.setAttribute("rules", "groups");		
	tr = document.createElement("caption");
	tr.appendChild(document.createTextNode("List of users"));
	userTable.appendChild(tr);
	tbody = document.createElement("thead");
	tr = document.createElement("tr");
	td = document.createElement("th");
	td.appendChild(document.createTextNode("Username"));
	tr.appendChild(td);
	td = document.createElement("th");
	td.appendChild(document.createTextNode("Output Directory"));
	tr.appendChild(td);
	td = document.createElement("th");
	td.appendChild(document.createTextNode("Import Directory"));
	tr.appendChild(td);
	td = document.createElement("th");
	td.appendChild(document.createTextNode("Downloadslots"));
	tr.appendChild(td);
	td = document.createElement("th");
	td.appendChild(document.createTextNode("has Adminrights"));
	tr.appendChild(td);
	tbody.appendChild(tr);	
	userTable.appendChild(tbody);
	tbody = document.createElement("tbody");
	tbody.setAttribute("id", "userTableBody");
	userTable.appendChild(tbody);	
	div.appendChild(userTable);	
	return div;
}
// creates torrent interaction buttons and menues
function createTorrentInteractions() {
	var div = document.createElement("div");
	var button, p;
	for (var i in interactions) {
		button = document.createElement("input");
		button.setAttribute("type", "button");
		button.setAttribute("reqID", interactions[i][1]);
		button.setAttribute("value", interactions[i][0]);
		button.className = "interact";
		button.onclick = function () { SendRequestToServer(this.getAttribute("reqID")); };
		div.appendChild(button);
	}
	p = document.createElement("p");
	p.appendChild(document.createTextNode("interact with all torrents"));
	p.className = "hint";
	div.appendChild(p);
	for (i in globalinteractions) {
		button = document.createElement("input");
		button.setAttribute("type", "button");
		button.setAttribute("reqID", globalinteractions[i][1]);
		button.setAttribute("value", globalinteractions[i][0]);
		button.className = "interact";
		button.onclick = function () { SendRequestToServer(this.getAttribute("reqID")); };
		div.appendChild(button);
	}
	p = document.createElement("p");
	p.appendChild(document.createTextNode("interact with Azureus (system)"));
	p.className = "hint";
	div.appendChild(p);
	for (i in systeminteractions) {
		button = document.createElement("input");
		button.setAttribute("type", "button");
		button.setAttribute("reqID", systeminteractions[i][1]);
		button.setAttribute("value", systeminteractions[i][0]);
		button.className = "interact";
		button.onclick = function () { SendRequestToServer(this.getAttribute("reqID")); };
		div.appendChild(button);
	}
	return div;
}
// function for output format definitions
function getAttributeFormat(attributeID, value) {
	var attribute = attributes[attributeID];
	switch (attribute) {
		case "completition":
			return document.createTextNode((value/10)+" %");
		break;
		case "totolAVG":
		case "downloadAVG":
		case "uploadAVG":
			var i = 0;
			while ((value > 1000) && (i < SI_byte.length-1)) {
				value = value/1000;
				i++;
			}		
			value = round(value, 2);		
			return document.createTextNode(value+" "+SI_byte[i]+"/s");
		break;
		case "size":
		case "downloaded":
		case "uploaded":
			var i = 0;
			while ((value > 1000) && (i < SI_byte.length-1)) {
				value = value/1000;
				i++;
			}		
			value = round(value, 2);		
			return document.createTextNode(value+" "+SI_byte[i]);
		break;
		case "health":
			var img = document.createElement("img");
			img.src = "img/Health_"+HealthStates[value]+".png";
			img.setAttribute("alt", formalHealthStates[value]);
			img.setAttribute("title", formalHealthStates[value]);
			return img;
		break;
		case "shareRatio":
			return document.createTextNode((value/1000));
		break;
		default:
			return document.createTextNode(value);
		break;
	}
}
function getEventType(evType) {
	return eventTypes[evType] ? eventTypes[evType] : eventTypes[0];
}
function handleEvents(Events) {
	//addDebugEntry("Handling Events..");
	// childnodes: <Event ...>
	var evType = null;
	var Event, time;	
	var evList = document.getElementById("eventlist");	
	while (evList.firstChild) evList.removeChild(evList.firstChild);
	var evTable = document.createElement("table");
	evTable.setAttribute("summary", "Events since last request");
	evTable.setAttribute("rules", "groups");		
	var evDetails = null;
	var tbody, tr, td, i, li;
	tbody = document.createElement("caption");
	tbody.appendChild(document.createTextNode("new Events"));
	evTable.appendChild(tbody);
	
	tbody = document.createElement("thead");
	tr = document.createElement("tr");
	td = document.createElement("th");
	td.appendChild(document.createTextNode("Time"));
	tr.appendChild(td);
	td = document.createElement("th");
	td.appendChild(document.createTextNode("Event"));
	tr.appendChild(td);
	td = document.createElement("th");
	td.appendChild(document.createTextNode("Details"));
	tr.appendChild(td);
	tbody.appendChild(tr);
	evTable.appendChild(tbody);
	
	tbody = document.createElement("tbody");	
	//addDebugEntry("Events: "+Events.nodeType+" - "+Events.nodeName);
	Event = Events.firstChild;
	time = Math.floor(Event.getAttribute("time"));
	time = new Date(time);
	//addDebugEntry("Event: "+Event.nodeType+" - "+Event.nodeName);
	tr = document.createElement("tr");
	td = document.createElement("td");
	td.appendChild(document.createTextNode(time.toLocaleString()));
	tr.appendChild(td);
	td = document.createElement("td");
	td.appendChild(document.createTextNode(getEventType(Event.getAttribute("type"))));
	tr.appendChild(td);
	td = document.createElement("td");
	if (Event.attributes.length > 2) {
		evDetails = document.createElement("ul");
		evDetails.className = "eventDetails";
		for (i in Event.attributes) 
			if (Event.attributes[i].nodeName && (Event.attributes[i].nodeName != "time") && (Event.attributes[i].nodeName != "type")) {
				li = document.createElement("li");
				li.appendChild(document.createTextNode(Event.attributes[i].nodeName+": "+Event.attributes[i].nodeValue));
				evDetails.appendChild(li);
			}		
		td.appendChild(evDetails);
	} else td.appendChild(document.createTextNode("no details given"));
	tr.appendChild(td);
	tbody.appendChild(tr);
	
	while (Event.nextSibling) {
		Event = Event.nextSibling;
		time = Math.floor(Event.getAttribute("time"));
		time = new Date(time);
		tr = document.createElement("tr");
		td = document.createElement("td");
		td.appendChild(document.createTextNode(time.toLocaleString()));
		tr.appendChild(td);
		td = document.createElement("td");
		td.appendChild(document.createTextNode(getEventType(Event.getAttribute("type"))));
		tr.appendChild(td);
		td = document.createElement("td");
		if (Event.attributes.length > 2) {
			evDetails = document.createElement("ul");
			evDetails.className = "eventDetails";
			for (i in Event.attributes) 
				if (Event.attributes[i].nodeName && (Event.attributes[i].nodeName != "time") && (Event.attributes[i].nodeName != "type")) {
					li = document.createElement("li");
					li.appendChild(document.createTextNode(Event.attributes[i].nodeName+": "+Event.attributes[i].nodeValue));
					evDetails.appendChild(li);
				}		
				td.appendChild(evDetails);
		} else td.appendChild(document.createTextNode("no details given"));
		tr.appendChild(td);
		tbody.appendChild(tr);
	}
	evTable.appendChild(tbody);
	evList.appendChild(evTable);
	
	evList.style.display = "block";
	document.getElementById("eventstatus").firstChild.data = "new events";
}
function handlelistTransfers(xmldoc) {
	var transfers = xmldoc.getElementsByTagName("Transfers");
	var transfer = null;
	var transferDataField = [];
	var i = 0;
	var hash = -1;
	//addDebugEntry("Transfers: "+transfers);
	for (var t in transfers) 
		if (transfers[t].nodeType == 1) {
			//addDebugEntry("ChildNodes "+t+": "+transfers[t].hasChildNodes());
			//addDebugEntry("FirstChild "+t+" (nodeType): "+transfers[t].firstChild.nodeType);
			//addDebugEntry("FirstChild "+t+" (nodeName): "+transfers[t].firstChild.nodeName);
			if (transfers[t].hasChildNodes()) {
				transferDataField[i] = [];
				//addDebugEntry("Transfer (Obj): "+transfer);
				if (transfer == null) {
					transfer = transfers[t].firstChild;
					// check attribute list
					var a = 0;
					for (var j in attributes)
						if (transfer.getAttribute(attributes[j]) != null) {
							transferDataField[i][a] = j;
							if (attributes[j] == "hash")
								hash = a;
							a++;
						}
					//addDebugEntry("Attributelist: "+transferDataField[0]);
					i++;
					transferDataField[i] = [];
					for (j in transferDataField[0])
						transferDataField[i][j] = transfer.getAttribute(attributes[transferDataField[0][j]]);					
					//addDebugEntry("DataField: "+transferDataField);
					//addDebugEntry("Transfer "+i+": "+transferDataField[i]);
				}
				while (transfer.nextSibling)  {
					i++;
					transferDataField[i] = [];
					transfer = transfer.nextSibling;
					for (j in transferDataField[0]) 
						transferDataField[i][j] = transfer.getAttribute(attributes[transferDataField[0][j]]);					
					//addDebugEntry("Transfer "+i+": "+transferDataField[i]);					
				}
			}	
		}
	// fetch data to viewport
	if (transfer != null) {
		var list = null;
		//addDebugEntry("Tabs: "+tabs);
		list = getTabByContent("listTransfers");
		if (list == null) {
			addTab("listTransfers");
			list = document.getElementById("tab_"+tabCount);
			list.appendChild(addlistTransfersInteraction());
		} else 
			if (list.lastChild != list.firstChild)
				list.removeChild(list.lastChild);
			else {
				list.removeChild(list.firstChild);
				list.appendChild(addlistTransfersInteraction());				
			}
		var container = document.createElement("div");
		var downloads = document.createElement("table");
		var uploads = document.createElement("table");
		var caption, dlbody, ulbody, tr, td;
		var isDoneCol = -1;
		var isStatusCol = -1;
		var isForceCol = -1;
		downloads.setAttribute("summary", "List of current Downloads on Server");
		downloads.setAttribute("rules", "groups");
		caption = document.createElement("caption");
		caption.appendChild(document.createTextNode("Downloads"));
		downloads.appendChild(caption);
		uploads.setAttribute("summary", "List of current Uploads on Server");
		uploads.setAttribute("rules", "groups");
		caption = document.createElement("caption");
		caption.appendChild(document.createTextNode("Uploads"));
		uploads.appendChild(caption);
		// fetch to tables		
		var activeTable = 0;
		// creating thead
		for (j = 0; j < 2; j++) {
			dlbody = document.createElement("thead");
			tr = document.createElement("tr");
			td = document.createElement("th");
			td.appendChild(document.createTextNode("TC"));
			td.setAttribute("title", "TransferController");
			tr.appendChild(td);
			for (i in transferDataField[0]) {
				if ((formalAttributes[transferDataField[0][i]] != "")
					// or required for speciel cols
					|| (attributes[transferDataField[0][i]] == "forceStart")
					) {
					// special cols
						if (attributes[transferDataField[0][i]] == "forceStart") isForceCol = i;
						if (attributes[transferDataField[0][i]] == "completition") isDoneCol = i;
						if (attributes[transferDataField[0][i]] == "status") isStatusCol = i;
					// normal viewport
						if (((j == 0) && (onlyULAtt[transferDataField[0][i]] == 0)) || ((j == 1) && (onlyDLAtt[transferDataField[0][i]] == 0))) {
							td = document.createElement("th");
							td.appendChild(document.createTextNode(formalAttributes[transferDataField[0][i]]));
							tr.appendChild(td);
						}
					}
			}
			dlbody.appendChild(tr);
			if (j == 0) downloads.appendChild(dlbody);
			else uploads.appendChild(dlbody);
		}
		dlbody = document.createElement("tbody");
		ulbody = document.createElement("tbody");
		// filling tbody
		var transferCtrl = null;
		for (j in transferDataField)		
			if (j > 0) {
				// check dl or ul
				activeTable = 0;
				if (transferDataField[j][isDoneCol] == 1000) activeTable = 1;
				// fetch
				tr = document.createElement("tr");
				tr.setAttribute("hash", transferDataField[j][hash]);
				tr.onclick = function () { selectTC(this); };
				td = document.createElement("td");
				transferCtrl = document.createElement("input");
				transferCtrl.setAttribute("type", "checkbox");
				transferCtrl.checked = getTCState(transferDataField[j][hash]);
				transferCtrl.setAttribute("value", "check for interaction selection");
				transferCtrl.setAttribute("name", "transferCtrl");
				transferCtrl.className = "transferCtrl";
				transferCtrl.onclick = function () { selectTC(this.parentNode.parentNode); };
				td.appendChild(transferCtrl);
				tr.appendChild(td);
				for (i in transferDataField[j]) {
					if (formalAttributes[transferDataField[0][i]] != "")
						if (((activeTable == 0) && (onlyULAtt[transferDataField[0][i]] == 0)) || ((activeTable == 1) && (onlyDLAtt[transferDataField[0][i]] == 0))) {
							td = document.createElement("td");
							if ((i == isStatusCol) && (transferDataField[j][isForceCol] == "true")) 
									td.appendChild(document.createTextNode("Forced "));
							td.appendChild(getAttributeFormat(transferDataField[0][i], transferDataField[j][i]));						
							tr.appendChild(td);
						}
				}
				if (activeTable == 0) dlbody.appendChild(tr);
				else ulbody.appendChild(tr);
			}
		if (dlbody.hasChildNodes()) {
			downloads.appendChild(dlbody);
			container.appendChild(downloads);
		}
		if (ulbody.hasChildNodes()) {
			uploads.appendChild(ulbody);
			container.appendChild(uploads);		
		}
		list.appendChild(container);
	}
}