// default settings
var activeTab = 0;
// for easy usage it is a simple counter, do NOT decrease this counter anytime
var tabCount = 2;
// available tabs
var registeredTabs = ["listTransfers", "about", "debug"];
// open tabs at position
var tabs = ["listTransfers", "about", "debug"];
// a example tab (tabbar is list of tabs)
// <li><span onclick="SendRequestToServer(1);">ALL Torrents</span><img src="img/delete.png" alt="Close Tab" title="Close Tab" onclick="closeTab(this);" /></li>
function addTab(contentElement) {
	// only create new tab, if no tab with contentElement exists
	var tabExists = getTabByContent(contentElement);
	if (tabExists == null) {
		// tabbar entry
		var tabbar = document.getElementById("tabbar");
		var tab = document.createElement("li");
		var tabLabel = document.createElement("span");
		var img = document.createElement("img");
		var label;
		tabCount++;
		img.src = "img/delete.png";
		img.setAttribute("alt", "Close Tab");
		img.setAttribute("title", "Close Tab");
		img.setAttribute("tab", tabCount);
		img.onclick = function() { closeTab(this); };
		switch (contentElement) {
			case "about":
				label = document.createTextNode("About");
			break;
			case "listTransfers":
				label = document.createTextNode("ALL Torrents");			
			break;
			default:
				label = document.createTextNode("empty Tab");
			break;
		}
		tabLabel.appendChild(label);
		tabLabel.setAttribute("tab", tabCount);
		tabLabel.onclick = function() { ShowTab(this.getAttribute("tab")); };
		tab.appendChild(tabLabel);
		tab.appendChild(img);
		tab.setAttribute("tab_control", tabCount);
		tabbar.appendChild(tab);
		// tabcontent
		var contentList = document.getElementById("tabcontents");
		var tabContent = document.createElement("div");
		tabContent.className = "tab";
		tabContent.setAttribute("id", "tab_"+tabCount);
		switch (contentElement) {
			case "about":
				var head = document.createElement("h1");
				head.appendChild(document.createTextNode("AzSMRC Webinterface"));
				var p = document.createElement("p");
				p.appendChild(document.createTextNode("AzSMRC is a remotecontrol for Azureus Bittorrent Client."));
				tabContent.appendChild(head);
				tabContent.appendChild(p);
			break;
			case "listTransfers":
				var p = document.createElement("p");
				p.appendChild(document.createTextNode("Detailsselection coming soon!"));
				tabContent.appendChild(p);
			break;
			default:
				tabContent.appendChild(document.createTextNode("This tab is empty!"));
			break;
		}
		contentList.appendChild(tabContent);
		tabs[tabCount] = contentElement;
		ShowTab(tabCount);
	}
}
function closeTab(tabObj) {
	tabID = tabObj.getAttribute("tab");
	//addDebugEntry("tabID: "+tabID);
	var tabControl = tabObj.parentNode;
	tabControl.parentNode.removeChild(tabControl);
	removeTab = document.getElementById("tab_"+tabID);
	if (removeTab)
		removeTab.parentNode.removeChild(removeTab);
	if (tabID == activeTab) {
		//addDebugEntry("closing active tab");
		var tabbar = document.getElementById("tabbar");
		activeTab = tabbar.firstChild.getAttribute("tab_control");
		ShowTab(activeTab);
	}
	tabs[tabID] = "";
}
function getTabByContent(contentElement) {
	var list = null;
	for (var i in tabs)
		if (tabs[i] == contentElement) {
			list = document.getElementById("tab_"+i);				
			break; // exit for ()
		}
	return list;
}
function initTabControl() {
	// requirements
	// element with id: tabcontents
	// count of childnodes tabs with id: tab_1, tab_2 ...
	var tabbar = document.getElementById("tabbar");
	if (tabbar.hasChildNodes) {
		var i = 0;
		var tab = tabbar.firstChild;
		if (tab.nodeName == "LI") {
			tab.setAttribute("tab_control", i);
			tab.firstChild.setAttribute("tab", i);
			tab.lastChild.setAttribute("tab", i);
			i++;
		}
		while (tab.nextSibling) {
			tab = tab.nextSibling;
			tab.setAttribute("tab_control", i);
			tab.firstChild.setAttribute("tab", i);
			tab.lastChild.setAttribute("tab", i);
			i++;
		}
	}	
	ShowTab(activeTab);
}
function refreshTabbar() {
	var tabbar = document.getElementById("tabbar");
	if (tabbar.hasChildNodes) {
		var tab = tabbar.firstChild;
		if (tab.nodeName == "LI") {
			if (tab.getAttribute("tab_control") == activeTab)
				tab.className = "active";
			else
				tab.className = "";
		}
		while (tab.nextSibling) {
			tab = tab.nextSibling;
			if (tab.getAttribute("tab_control") == activeTab)
				tab.className = "active";
			else
				tab.className = "";
		}		
	}
}
function ShowTab(tab) {
	var toActivate = document.getElementById("tab_"+tab);
	document.getElementById("tab_"+activeTab).style.display = "none";
	toActivate.style.display = "block";
	activeTab = tab;
	refreshTabbar();
}