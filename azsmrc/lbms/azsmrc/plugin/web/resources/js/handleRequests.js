// attributes that are allowed to be shown
var attributes = ["health", "position", "name", "state", "status", "downloaded", "uploaded", "forceStart", "downloadAVG", "uploadAVG", "totalAVG", "elapsedTime", "eta", "availability", "completition", "shareRatio", "tracker", "downloadLimit", "uploadLimit", "total_seeds", "total_leechers", "size", "last_scrape", "next_scrape"];
// output strings for attributes
// empty string defines no output
var formalAttributes = ["Health", "#", "Name", "", "Status", "Downloaded", "Uploaded", "", "Down Speed", "Up Speed", "Total Speed", "Elapsed time", "ETA", "Availability", "Done", "Share Ratio", "Tracker Status", "max. #DL", "max #UL", "Seeds", "Peers", "Size", "Scrape", "next Scrape"];
// function for output format definitions
function getAttributeFormat(attributeID, value) {
	var attribute = attributes[attributeID];
	switch (attribute) {
		case "completition":
			return document.createTextNode((value/10)+" %");
		break;
		case "downloadAVG":
			return document.createTextNode(round((value/1024), 2)+" KB/s");
		break;
		case "downloaded":
			var val = round(((value/1024)/1024), 2);
			return document.createTextNode(val+" MB");
		break;
		case "health":
			var img = document.createElement("img");
			img.src = "img/Health_"+HealthStates[value]+".png";
			return img;
		break;
		case "shareRatio":
			return document.createTextNode((value/1000));
		break;
		case "size":
			var val = round(((value/1024)/1024), 2);
			return document.createTextNode(val+" MB");
		break;
		case "uploadAVG":
			return document.createTextNode(round((value/1024), 2)+" KB/s");
		break;
		case "uploaded":
			var val = round(((value/1024)/1024), 2);
			return document.createTextNode(val+" MB");
		break;
		default:
			return document.createTextNode(value);
		break;
	}
}
function handlelistTransfers(xmldoc) {
	var transfers = xmldoc.getElementsByTagName("Transfers");
	var transfer = null;
	var transferDataField = [];
	var i = 0;
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
		} else	while (list.firstChild) list.removeChild(list.firstChild);
		var downloads = document.createElement("table");
		var uploads = document.createElement("table");
		var caption, dlbody, ulbody, tr, td;
		var isDoneCol = -1;
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
			for (i in transferDataField[0])
				if (formalAttributes[transferDataField[0][i]] != "") {
					td = document.createElement("th");
					td.appendChild(document.createTextNode(formalAttributes[transferDataField[0][i]]));
					// special Cols
					if (attributes[transferDataField[0][i]] == "completition") isDoneCol = i;
					tr.appendChild(td);
				}	
			dlbody.appendChild(tr);
			if (j == 0) downloads.appendChild(dlbody);
			else uploads.appendChild(dlbody);
		}
		dlbody = document.createElement("tbody");
		ulbody = document.createElement("tbody");
		// filling tbody
		for (j in transferDataField)
			if (j > 0) {
				// check dl or ul
				activeTable = 0;
				if (transferDataField[j][isDoneCol] == 1000) activeTable = 1;
				// fetch
				tr = document.createElement("tr");
				for (i in transferDataField[j]) {
					if (formalAttributes[transferDataField[0][i]] != "") {
						td = document.createElement("td");
						td.appendChild(getAttributeFormat(transferDataField[0][i], transferDataField[j][i]));
					}
					tr.appendChild(td);
				}
				if (activeTable == 0) dlbody.appendChild(tr);
				else ulbody.appendChild(tr);
			}
		downloads.appendChild(dlbody);
		uploads.appendChild(ulbody);
		list.appendChild(downloads);
		list.appendChild(uploads);		
	}
}
