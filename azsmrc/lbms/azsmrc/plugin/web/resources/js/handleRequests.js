// attributes that are allowed to be shown
var attributes = ["health", "position", "name", "state", "status", "downloaded", "uploaded", "forceStart", "downloadAVG", "uploadAVG", "totalAVG", "elapsedTime", "eta", "availability", "completition", "shareRatio", "tracker", "downloadLimit", "uploadLimit", "total_seeds", "total_leechers", "size", "last_scrape", "next_scrape", "hash"];
// enable attributefiltering for tables
var onlyDLAtt = [0,0,0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0];
var onlyULAtt = [0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];
// output strings for attributes
// empty string defines no output
var formalAttributes = ["Health", "#", "Name", "", "Status", "Downloaded", "Uploaded", "", "Down Speed", "Up Speed", "Total Speed", "Elapsed time", "ETA", "Availability", "Done", "Share Ratio", "Tracker Status", "max. DL speed", "max UL speed", "Seeds", "Peers", "Size", "Scrape", "next Scrape", ""];
// event types
var eventTypes = ["unknown", "Download Completed", "Download Torrent Removed", "Download Exception", "System Exception", "Update Available", "Message", "Error Message", "Plugin Message"];
// additional interactions listed in interaction menu
// with selected torrents
var interactions = [["request download scrape", 17], ["request download announce", 18]];
// with all torrents at once
var globalinteractions = [["start all downloads", 21], ["resume all downloads", 24], ["stop all downloads", 22]];
// system (admin) interactions
var systeminteractions = [["restart Azureus", 39]];
function addAdvInteraction() {
	var div = document.createElement("div");
	// torrent interaction
	form = document.createElement("form");
	p = document.createElement("p");
	p.appendChild(document.createTextNode("interact with selected torrents"));
	p.className = "hint";
	form.appendChild(p);
	form.appendChild(createTorrentInteractions());
	div.appendChild(form);
	return div;
}
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
	button.setAttribute("value", "Save to cookie");
	button.setAttribute("title", "Save selected labels into a cookie");
	button.className = "closeButton";
	button.onclick = function () {
		var now = new Date();
		fixDate(now);
		// expires after one year
		now.setTime(now.getTime() + 365 * 24 * 60 * 60 * 1000);
		var value = selectedDetails.join(",");
		setCookie("selectedDetails", value, now);
	};
	form.appendChild(button);
	button = document.createElement("input");
	button.setAttribute("type", "button");
	button.setAttribute("value", "Close");
	button.setAttribute("title", "Close Form");
	button.className = "closeButton";
	button.onclick = function () {
		document.getElementById("labelselectionform").style.display = "none";
		if (!autoRefresh[getRegTabById(getTabIdByContent("listTransfers"))])
			if (window.confirm("Do you want to apply the new settings?\n\tClick OK for refresh!"))
				refreshView();
	};
	form.appendChild(button);
	form.className = "tab";
	set_dragbar(form);
	div.appendChild(form);

	link = document.createElement("a");
	link.setAttribute("title", "Open Labelselection");
	link.onclick = function () { document.getElementById("labelselectionform").style.display = "block"; };
	link.appendChild(document.createTextNode("Open Labelselection"));
	li = document.createElement("li");
	li.appendChild(link);
	ul.appendChild(li);
	link = document.createElement("a");
	link.setAttribute("title", "Open Advanced Interactionmenu");
	link.onclick = function () { addTab("advanced_interaction"); };
	link.appendChild(document.createTextNode("Open Advanced Interactionmenu"));
	li = document.createElement("li");
	li.appendChild(link);
	ul.appendChild(li);
	div.appendChild(ul);
	div.style.display = "block";
	return div;
}
function addPreferences() {
	var div = document.createElement("div");
	var head = document.createElement("h2");
	head.appendChild(document.createTextNode("System Preferences"));
	div.appendChild(head);
	var form = document.createElement("form");
	var fieldset = document.createElement("fieldset");
	var legend = document.createElement("legend");
	var label, input, p = null;
	p = document.createElement("p");
	p.appendChild(document.createTextNode("All settings can be saved in a cookie. If no cookie is set the new settings will only be applied temporarly to the current session."));
	p.className = "description";
	form.appendChild(p);
	legend.appendChild(document.createTextNode("Autorefresh"));
	fieldset.appendChild(legend);
	p = document.createElement("p");
	p.appendChild(document.createTextNode("All values below are in milliseconds! You can assign several refresh times for each available tab."));
	p.className = "description";
	fieldset.appendChild(p);
	for (var i in registeredTabs)
		if (refreshRequests[i] > -1) {
			label = document.createElement("label");
			label.appendChild(document.createTextNode(tabLabels[i]));
			label.setAttribute("for", "cookie_autorefresh_"+i);
			input = document.createElement("input");
			input.setAttribute("id", "cookie_autorefresh_"+i);
			input.setAttribute("type", "text");
			input.setAttribute("value", autoRefresh[i]);
			fieldset.appendChild(label);
			fieldset.appendChild(input);
		}
	label = document.createElement("label");
	label.appendChild(document.createTextNode("Save data in cookie"));
	label.setAttribute("for", "cookie_autorefresh");
	fieldset.appendChild(label);
	input = document.createElement("input");
	input.setAttribute("id", "cookie_autorefresh");
	input.setAttribute("type", "checkbox");
	fieldset.appendChild(input);
	form.appendChild(fieldset);

	fieldset = document.createElement("fieldset");
	legend = document.createElement("legend");
	legend.appendChild(document.createTextNode("Tabs on startup"));
	fieldset.appendChild(legend);
	p = document.createElement("p");
	p.appendChild(document.createTextNode("These settings will only be available with cookies activated. 'ALL Torrents' tab will always be displayed."));
	p.className = "description";
	fieldset.appendChild(p);
	var container = document.createElement("div");
	container.className = "form_checks";
	for (i in registeredTabs)
		if (i > 0) {
			label = document.createElement("label");
			label.appendChild(document.createTextNode(tabLabels[i]));
			label.setAttribute("for", "startup_"+i);
			input = document.createElement("input");
			input.setAttribute("type", "checkbox");
			input.setAttribute("id", "startup_"+i);
			if (startupTabs[i])
				input.setAttribute("checked", true);
			container.appendChild(label);
			container.appendChild(input);
		}
	fieldset.appendChild(container);
	form.appendChild(fieldset);

	fieldset = document.createElement("fieldset");
	legend = document.createElement("legend");
	legend.appendChild(document.createTextNode("Tab Positions"));
	fieldset.appendChild(legend);
	container = document.createElement("div");
	container.className = "form_checks";

	label = document.createElement("label");
	label.appendChild(document.createTextNode("Save Tabpositions in cookie"));
	label.setAttribute("for", "cookie_tabpositions");
	container.appendChild(label);
	input = document.createElement("input");
	input.setAttribute("id", "cookie_tabpositions");
	input.setAttribute("type", "checkbox");
	input.checked = optionSet("tabpositions");
	container.appendChild(input);

	label = document.createElement("label");
	label.appendChild(document.createTextNode("Save 'On The Fly'"));
	label.setAttribute("for", "cookie_tabposonthefly");
	container.appendChild(label);
	input = document.createElement("input");
	input.setAttribute("id", "cookie_tabposonthefly");
	input.setAttribute("type", "checkbox");
	input.checked = optionSet("tabposonthefly");
	container.appendChild(input);

	label = document.createElement("label");
	label.appendChild(document.createTextNode("Save maximized tab"));
	label.setAttribute("for", "cookie_maxtab");
	container.appendChild(label);
	input = document.createElement("input");
	input.setAttribute("id", "cookie_maxtab");
	input.setAttribute("type", "checkbox");
	input.checked = optionSet("maxtab");
	container.appendChild(input);

	fieldset.appendChild(container);
	p = document.createElement("p");
	p.className = "description";
	p.appendChild(document.createTextNode("This option allows AzSMRC to remember where you placed your tabs. If you enable this option, tabs that come with startup will be places on their old positions! 'On The Fly' means your data will be saved as soon as you change a position and will be loaded everytime you (re)open a certain tab - not only on startup."));
	fieldset.appendChild(p);
	form.appendChild(fieldset);

	input = document.createElement("input");
	input.setAttribute("type", "button");
	input.setAttribute("value", "Save settings");
	input.onclick = function() {
		savePreferences();
	}
	form.appendChild(input);
	p = document.createElement("p");
	p.appendChild(document.createTextNode("This button deletes all cookies for this AzSMRC Server: "));
	var strong = document.createElement("strong");
	strong.appendChild(document.createTextNode(Server));
	p.appendChild(strong);
	p.className = "description";
	input = document.createElement("input");
	input.setAttribute("type", "button");
	input.setAttribute("value", "Delete cookies");
	input.onclick = function () {
		clearCookies();
	}
	form.appendChild(p);
	form.appendChild(input);
	div.appendChild(form);
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
	div.setAttribute("id", "advinteractform");
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
		case "last_scrape":
			date = new Date();
			date.setTime( value );
			return document.createTextNode( date.toLocaleString() );
		break;
		case "next_scrape":
			// get now
			date = new Date();
			now = date.getTime();
			// calculate difference between now and next_scrape timestamp
			diff = Math.round( (value - now) / 1000 );
			hours = 0;
			mins = 0;
			secs = 0;
			// at least one hour given
			if ( diff >= 3600 )
			{
				hours = Math.floor( diff / 3600 );
				diff = diff - (hours * 3600);
			}
			if ( diff >= 60 )
			{
				mins = Math.floor( diff / 60 );
				diff = diff - (mins * 60);
			}
			secs = diff;

			timeStr = "in ";
			if ( hours )
			{
				timeStr += hours + "h ";
			}
			if ( mins )
			{
				timeStr += mins + "m ";
			}
			if ( secs )
			{
				timeStr += secs + "s ";
			}

			return document.createTextNode( timeStr );
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
	var Event = Events.firstChild;
	if (Event) {
		showEvent(Event);

		while (Event.nextSibling) {
			Event = Event.nextSibling;
			showEvent(Event);
		}
	}
}
function handleEvents_old(Events) {
	// addDebugEntry("Handling Events..");
	// childnodes: <Event ...>
	var evType = null;
	var Event, time;
	var evList = document.getElementById("eventlist");
	var evDetails = null;
	var tbody, tr, td, i, li;
	var wasClear = true;
	if (evList.childNodes.length > 1) {
		if (evList.childNodes[1].nodeName == "TABLE") {
			var evTable = evList.childNodes[1];
			tbody = evTable.childNodes[2];
			wasClear = false;
		} else
			while (evList.lastChild.className != "dragbar") evList.removeChild(evList.lastChild);
	}
	if (wasClear) {
		if (evList.lastChild)
			while (evList.lastChild.className != "dragbar") evList.removeChild(evList.lastChild);
		var evTable = document.createElement("table");
		evTable.setAttribute("summary", "Events since last request");
		evTable.setAttribute("rules", "groups");
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
	}
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

	if (wasClear) {
		evTable.appendChild(tbody);
		evList.appendChild(evTable);
		var button = document.createElement("input");
		button.setAttribute("type", "button");
		button.setAttribute("value", "Clear Events");
		button.onclick = function() {
			var evList = document.getElementById("eventlist");
			document.getElementById("eventlist").style.display = "none";
			document.getElementById("eventstatus").firstChild.data = "no new events";
			while (evList.firstChild) evList.removeChild(evList.firstChild);
			var p = document.createElement("p");
			p.appendChild(document.createTextNode("No events since last request."));
			evList.appendChild(p);
		}
		evList.appendChild(button);
		if (evList.firstChild.className != "dragbar")
			set_dragbar(evList);
	}

	evList.style.display = "block";
	document.getElementById("eventstatus").firstChild.data = "new events";
}
function handlelistTransfers(xmldoc) {
	var transfers = xmldoc.getElementsByTagName("Transfers");
	var transfer = null;
	var transferDataField = [];
	var i = 0;
	var hash = -1;
	positions = [0, 0];
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
		list = getContentFrameByTab(getTabByContent("listTransfers"));
		if (list == null) {
			addTab("listTransfers");
			list = document.getElementById("tab_"+tabCount);
			list.appendChild(addlistTransfersInteraction());
		} else
			while (list.lastChild.getAttribute("id") != "interaction_container")
				list.removeChild(list.lastChild);
		var container = document.createElement("div");
		var downloads = document.createElement("table");
		var uploads = document.createElement("table");
		var caption, dlbody, ulbody, tr, td;
		var content = null;
		var isDoneCol = -1;
		var isStatusCol = -1;
		var isForceCol = -1;
		var nodl, noul;
		downloads.setAttribute("summary", "List of current Downloads on Server");
		downloads.setAttribute("rules", "groups");
		downloads.setAttribute("id", "listTransferDownloads");
		caption = document.createElement("caption");
		caption.appendChild(document.createTextNode("Downloads"));
		downloads.appendChild(caption);
		uploads.setAttribute("summary", "List of current Uploads on Server");
		uploads.setAttribute("rules", "groups");
		uploads.setAttribute("id", "listTransferUploads");
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
			td.className = "invisible";
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
				forceStates[transferDataField[j][hash]] = transferDataField[j][isForceCol];
				if (getTCState(transferDataField[j][hash]))
				{
					$(tr).addClass( 'activeTC' );
				}
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
				td.className = "invisible";
				tr.appendChild(td);
				for (i in transferDataField[j]) {
					if (formalAttributes[transferDataField[0][i]] != "")
						if (((activeTable == 0) && (onlyULAtt[transferDataField[0][i]] == 0)) || ((activeTable == 1) && (onlyDLAtt[transferDataField[0][i]] == 0))) {
							td = document.createElement("td");
							if ((i == isStatusCol) && (transferDataField[j][isForceCol] == "true"))
									td.appendChild(document.createTextNode("Forced "));
							content = getAttributeFormat(transferDataField[0][i], transferDataField[j][i]);
							td.appendChild(content);
							tr.appendChild(td);
						}
				}
				if (activeTable == 0)
				{
					dlbody.appendChild(tr);
				}
				else
				{
					ulbody.appendChild(tr);
				}
				positions[activeTable]++;
			}
		if (dlbody.hasChildNodes()) {
			downloads.appendChild(dlbody);
			container.appendChild(downloads);
			nodl = 0;
			//addTableToSort(downloads);
		} else nodl = 1;
		if (ulbody.hasChildNodes()) {
			uploads.appendChild(ulbody);
			container.appendChild(uploads);
			noul = 0;
			//addTableToSort(uploads);
		} else noul = 1;
		if (nodl) {
			var p = document.createElement("p");
			p.appendChild(document.createTextNode("No downloads present!"));
			p.className = "hint";
			list.appendChild(p);
		}
		if (noul) {
			var p = document.createElement("p");
			p.appendChild(document.createTextNode("No uploads present!"));
			p.className = "hint";
			list.appendChild(p);
		}
		list.appendChild(container);
		//addDebugEntry("Positions: "+positions);
	}
}
function showEvent(Event) {
	var message = '';
	var time = Math.floor(Event.getAttribute("time"));
	time = new Date(time);

	if (Event.attributes.length > 2) {
		for (i in Event.attributes)
			if (Event.attributes[i].nodeName && (Event.attributes[i].nodeName != "time") && (Event.attributes[i].nodeName != "type")) {
				message = Event.attributes[i].nodeName+': '+Event.attributes[i].nodeValue+'<br />';
			}
	}

	message('new Event at '+time, message, 'event');
}