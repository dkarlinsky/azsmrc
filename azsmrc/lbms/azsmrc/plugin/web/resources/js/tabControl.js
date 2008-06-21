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
// current tab positions and layer (zIndex) in viewport
var tabPositions = [];
// tabs which are able to maximize
var tabMax = [1, 0, 1, 1, 0, 0, 0];
// currently maximized tab
var maxTab = null;
var maxzIndex = null;
// an example tab (tabbar is list of tabs)
// <li><span onclick="SendRequestToServer(1);">ALL Torrents</span><img src="img/delete.png" alt="Close Tab" title="Close Tab" onclick="closeTab(this);" /></li>
function addTab(contentElement, isStartup) {
	// only create new tab, if no tab with contentElement exists
	var tabExists = getTabByContent(contentElement);
	var setTabPos = optionSet("tabposonthefly");
	var i = 0;
	if (tabExists == null) {
		var label, labelID = null;
		label = document.createTextNode("Empty Tab");
		for (i in registeredTabs)
			if (registeredTabs[i] == contentElement) {
				labelID = i;
				break;
			}
		// tabbar entry
		var tabbar = document.getElementById("tabbar");
		var tab = document.createElement("li");
		var tabLabel = document.createElement("span");
		var img = document.createElement("img");
		tabCount++;
		if (contentElement != "listTransfers") {
			img.src = "img/crystalClear/close.png";
			img.setAttribute("alt", "Close Tab");
			img.setAttribute("title", "Close Tab");
			img.setAttribute("tab", tabCount);
			img.onclick = function() { closeTab(this); };
		}
		if (labelID != null)
			label = document.createTextNode(tabLabels[labelID]);
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
				var head = document.createElement("h2");
				head.appendChild(document.createTextNode("AzSMRC Webinterface"));
				var p = document.createElement("p");
				var img = document.getElementById('splashscreen');
				img.setAttribute('id', 'aboutimg');
				img.style.display = '';
				p.appendChild(img);
				p.appendChild(document.createTextNode("AzSMRC is a remotecontrol for Azureus Bittorrent Client. This webinterface needs activated Javascript."));
				tabContent.appendChild(head);
				tabContent.appendChild(p);
				var html = 'You can find the <a href="http://azsmrc.sourceforge.net">project homepage on sourceforge.net</a>. More details about the developers and what AzSMRC is about can be found there!';
				var p = document.createElement("p");
				p.innerHTML = html;
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
		window.setTimeout("ShowTab(tabCount)", 200);
		refreshView();
		configAutoRefresh();
		set_dragbar(tabContent);
		var dragbar = tabContent.firstChild;
		if (contentElement != "listTransfers") {
			var frameLabel = document.createElement("h2");
			frameLabel.appendChild(document.createTextNode(tabLabels[labelID]));
			dragbar.appendChild(frameLabel);
			var close = document.createElement("img");
			close.src = "img/icon-close.png";
			close.setAttribute("title", "Close");
			close.setAttribute("alt", "Close");
			close.setAttribute("tab", tabCount);
			close.onclick = function () {
				var tabID = this.getAttribute("tab");
				var list = document.getElementById("tabbar");
				var tab = 0;
				for (i = 0; i < list.childNodes.length; i++) {
					tab = list.childNodes[i];
					if (tab.getAttribute("tab_control") == tabID)
						tab.lastChild.onclick();
				}
			}
		}
		if (tabMax[labelID]) {
			var maximize = document.createElement("img");
			maximize.src = "img/icon-max.png";
			maximize.setAttribute("title", "to 'Fullscreen'");
			maximize.setAttribute("alt", "Max");
			maximize.setAttribute("tab", tabCount);
			maximize.onclick = function () {
				maximizeTab(this.parentNode.parentNode);
			}
			dragbar.appendChild(maximize);
		}
		if (contentElement != "listTransfers") {
			dragbar.appendChild(close);
		}
		if (isStartup || setTabPos) {
			var tabPos = tabPositions[labelID];
			if (tabPos) {
				tabContent.style.position = "absolute";
				tabContent.style.left = tabPos[0];
				tabContent.style.top = tabPos[1];
				tabContent.style.zIndex = tabPos[2];
			}
		}
	} else {
		window.setTimeout("ShowTab(getTabIdByContent('"+contentElement+"'))", 200);
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
	return tab;
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
		if (tab) {
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
	}
	// add startup tabs
	for (var i in startupTabs)
		if (startupTabs[i])
			addTab(registeredTabs[i], true);
	ShowTab(0);
}
function loadTabPos() {
	var value = getCookie("tabPositions");
	var pos;
	if (value) {
		value = value.split(";");
		for (i in value) {
			pos = value[i].split(",");
			tabPositions[pos[0]] = [pos[1], pos[2], pos[3]];
		}
	}
}
function maximizeTab(tabObj) {
	// make tab "fullscreen"
	var tabID = tabObj.getAttribute("tab");

	if (maxTab != null) {
		var oldTab = document.getElementById('tab_'+maxTab);
		// revoke old max tab
		$('#tab_'+maxTab).removeClass('maximizedTab');
		changeFixState(oldTab.firstChild);
		oldTab.style.zIndex = maxzIndex;
		oldTab.style.height = 'auto';
		oldTab.style.width = 'auto';
	}

	// set new max tab
	maxTab = tabID;
	maxzIndex = tabObj.style.zIndex;
	$(tabObj).addClass('maximizedTab');
	changeFixState(tabObj.firstChild);
	tabObj.style.zIndex = 5000;
	tabObj.style.height = maxHeight+'px';
	tabObj.style.width = maxWidth+'px';
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
function reindexStatusbar() {
	zIndex++;
	document.getElementById("statusbar").style.zIndex = zIndex;
}
function saveTabPosCookie() {
	var tabPos = [];
	for (var i in registeredTabs) {
		tab = getTabByContent(registeredTabs[i]);
		if (tab && tab.style.left) {
			tabPos.push([i, tab.style.left, tab.style.top, tab.style.zIndex]);
		}
	}
	var now = new Date();
	fixDate(now);
	// expires after one year
	now.setTime(now.getTime() + 365 * 24 * 60 * 60 * 1000);
	var value = tabPos.join(";");
	setCookie("tabPositions", value, now);
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
		reindexStatusbar();
		refreshTabbar();
	}
}
