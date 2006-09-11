var attributes = ["health", "position", "name", "state", "status", "downloaded", "uploaded"];
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
						if (transfer.getAttribute(attributes[j])) {
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
		// static until tabControl is finished
		var list = document.getElementById("tab_1");
		while (list.firstChild) list.removeChild(list.firstChild);		
		var downloads = document.createElement("table");
		var uploads = document.createElement("table");
		var caption, tbody, tr, td, img;
		var HeadExists = false;
		var isHealthCol = -1;
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
		for (j in transferDataField) {
			if (!HeadExists) tbody = document.createElement("thead");
			else if (tbody.nodeName != "TBODY") tbody = document.createElement("tbody");
			tr = document.createElement("tr");
			for (i in transferDataField[j]) {
				if (!HeadExists) {
					td = document.createElement("th");
					td.appendChild(document.createTextNode(attributes[transferDataField[j][i]]));
					if (attributes[transferDataField[j][i]] == "health") {			
						isHealthCol = i;
					}			
				} else {
					td = document.createElement("td");
					if (i == isHealthCol) {
						img = document.createElement("img");
						img.src = "img/Health_"+HealthStates[transferDataField[j][i]]+".png";
						td.appendChild(img);
					} else {
						td.appendChild(document.createTextNode(transferDataField[j][i]));
					}
				}
				tr.appendChild(td);
			}
			if (!HeadExists) {
				HeadExists = true;
				uploads.appendChild(tbody);
			}
			tbody.appendChild(tr);
		}
		uploads.appendChild(tbody);
		list.appendChild(uploads);		
	}
}
