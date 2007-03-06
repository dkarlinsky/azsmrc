// default settings
var activeTab = -1;
// for easy usage it is a simple counter, do NOT decrease this counter anytime
var tabCount = -1;
// available tabs
var registeredTabs = ["listTransfers", "about", "debug", "userManagement", "torrentControl", "preferences", "advanced_interaction"];
// shown labels for tabs
var tabLabels = ["ALL Torrents", "About", "Debug Log", "Users", "Add Torrents", "Preferences", "Interaction"];
// auto refresh for registered tabs (standard refresh time in ms)
var autoRefresh = [5000, 0, 0, 0, 0, 0, 0];
// requests used by registeredTabs (-1 = none)
var refreshRequests = [1, -1, -1, 29, -1, -1, -1];
// objects for deactivating autorefresh
var autoRefreshObjs = [null, null, null, null, null, null];
// open tabs at position (default is set below)
var tabs = [];
var startupTabs = [false, true, false, false, false, false, false];
// an example tab (tabbar is list of tabs)
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
		if (contentElement != "listTransfers") {
			img.src = "img/delete.png";
			img.setAttribute("alt", "Close Tab");
			img.setAttribute("title", "Close Tab");
			img.setAttribute("tab", tabCount);
			img.onclick = function() { closeTab(this); };
		}
		switch (contentElement) {
			case "about":
				label = document.createTextNode(tabLabels[1]);
			break;
			case "debug":
				label = document.createTextNode(tabLabels[2]);
			break;
			case "listTransfers":
				label = document.createTextNode(tabLabels[0]);			
			break;
			case "userManagement":
				label = document.createTextNode(tabLabels[3]);
			break;
			case "torrentControl":
				label = document.createTextNode(tabLabels[4]);
			break;
			case "preferences":
				label = document.createTextNode(tabLabels[5]);
			break;
			case "advanced_interaction":
				label = document.createTextNode(tabLabels[6]);
			break;
			default:
				label = document.createTextNode("empty Tab");
			break;
		}
		tabLabel.appendChild(label);
		tabLabel.setAttribute("tab", tabCount);
		tabLabel.onclick = function() { ShowTab(this.getAttribute("tab")); };
		tab.appendChild(tabLabel);
		if (contentElement != "listTransfers")
			tab.appendChild(img);
		tab.setAttribute("tab_control", tabCount);
		tabbar.appendChild(tab);
		
		// tabcontent
		var contentList = document.getElementById("tabcontents");
		var tabContent = document.createElement("div");
		tabContent.className = "tab";
		tabContent.setAttribute("id", "tab_"+tabCount);
		tabContent.setAttribute("tab", tabCount);
		switch (contentElement) {
			// simple tabs can be created here
			// advanced tabs should use own function
			case "about":
				var head = document.createElement("h1");
				head.appendChild(document.createTextNode("AzSMRC Webinterface"));
				var p = document.createElement("p");
				p.appendChild(document.createTextNode("AzSMRC is a remotecontrol for Azureus Bittorrent Client. This webinterface needs activated Javascript."));
				tabContent.appendChild(head);
				tabContent.appendChild(p);
			break;
			case "debug":
				var head = document.createElement("h2");
				head.appendChild(document.createTextNode("Debug Log"));
				var ul = document.createElement("ul");
				ul.className = "debuglist";				
				tabContent.appendChild(head);
				tabContent.appendChild(ul);
			break;
			case "listTransfers":
				tabContent.appendChild(addlistTransfersInteraction());
			break;
			case "userManagement":
				tabContent.appendChild(addUserManagement());
			break;
			case "torrentControl":
				tabContent.appendChild(addTorrentContent());
			break;
			case "preferences":
				tabContent.appendChild(addPreferences());
			break;
			case "advanced_interaction":
				tabContent.appendChild(addAdvInteraction());
			break;
			default:
				tabContent.appendChild(document.createTextNode("This tab is empty!"));
			break;
		}
		tabContent.onclick = function () {
			ShowTab(this.getAttribute("tab"));
		}
		contentList.appendChild(tabContent);
		tabs[tabCount] = contentElement;
		ShowTab(tabCount);
		refreshView();
		configAutoRefresh();
		if (contentElement != "listTransfers") {
			set_dragbar(tabContent);
			var dragbar = tabContent.firstChild;
			var close = document.createElement("img");
			close.src = "img/icon-close.png";
			close.setAttribute("title", "Close");
			close.setAttribute("alt", "Close");
			close.setAttribute("tab", tabCount);
			close.onclick = function () {
				closeTab(this);
			}
			dragbar.appendChild(close);
		}		
	} else { 
		ShowTab(getTabIdByContent(contentElement));
	}
}
function closeTab(tabObj) {
	tabID = tabObj.getAttribute("tab");
	if (getTabIdByContent("listTransfers") != tabID) {
		//addDebugEntry("tabID: "+tabID);
		var tabControl = tabObj.parentNode;
		tabControl.parentNode.removeChild(tabControl);
		removeTab = document.getElementById("tab_"+tabID);
		if (removeTab)
			removeTab.parentNode.removeChild(removeTab);
		if (tabID == activeTab) {
			//addDebugEntry("closing active tab");
			var tabbar = document.getElementById("tabbar");
			if (tabbar.hasChildNodes()) {
				activeTab = tabbar.firstChild.getAttribute("tab_control");
				ShowTab(activeTab);
			} else activeTab = -1;
		}
		tabs[tabID] = "";
		configAutoRefresh();
	}
}
function getRegTabById(tabID) {
	var tab = null;
	for (var i in registeredTabs)
		if (registeredTabs[i] == tabs[tabID]) {
			tab = i;
			break;
		}
	return i;
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
function getTabIdByContent(contentElement) {
	var tab = getTabByContent(contentElement);
	var tabID = tab.getAttribute("id");
	tabID = tabID.substring(4, tabID.length);
	return tabID;
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
	// add startup tabs
	for (var i in startupTabs)
		if (startupTabs[i])
			addTab(registeredTabs[i]);
	ShowTab(0);
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
	if (toActivate != null) {
		/*
		if (activeTab > -1)
			document.getElementById("tab_"+activeTab).style.display = "none";
		toActivate.style.display = "block";
		*/
		zIndex++;
		toActivate.style.zIndex = zIndex;
		activeTab = tab;
		refreshTabbar();
	}
}
