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
// rules are offsetLeft/Top and offsetWidth/Height
// 1: left top
// 2: right bottom
// [x1, y1, x2, y2]
var snapLines = [];
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
var savedMaxTab = null;
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
		var tabContentFrame = document.createElement("div");
		tabContentFrame.className = "tab";
		tabContentFrame.setAttribute("id", "tab_"+tabCount);
		tabContentFrame.setAttribute("tab", tabCount);
		var tabContent = document.createElement("div");
		tabContent.className = "tabContent";
		switch (contentElement) {
			// simple tabs can be created here
			// advanced tabs should use own function
			case "about":
				var head = document.createElement("h2");
				head.appendChild(document.createTextNode("AzSMRC Webinterface"));
				var p = document.createElement("p");
				var img = document.getElementById('splashscreen');
				if (img) {
					img.setAttribute('id', 'aboutimg');
					img.style.display = '';
					p.appendChild(img);
				}
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
		tabContentFrame.appendChild(tabContent);

		// overwrite tabContent with new frame
		tabContent = tabContentFrame;

		contentList.appendChild(tabContent);
		tabs[tabCount] = contentElement;
		window.setTimeout("ShowTab(tabCount)", 200);
		refreshView(contentElement);
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
		// if current tab is savedMaxTab, max this tab!
		if (labelID == savedMaxTab) {
			maximizeTab(tabContent);
			savedMaxTab = null;
		// otherwise it might be necessary to mark oversized
		} else {
			// small timeout, so the tab is
			window.setTimeout("checkTabOversize('"+contentElement+"')", 400);
		}
	} else {
		window.setTimeout("ShowTab(getTabIdByContent('"+contentElement+"'))", 200);
	}
}
function checkTabOversize( contentElement )
{
	tabObj = getTabByContent( contentElement );
	if ( tabObj )
	{
		if ( tabObj.clientHeight >= oversizeHeight )
		{
			markTabOversized( tabObj );
		}
		else
		{
			unmarkTabOversized( tabObj );
		}
	}
}
function closeTab(tabObj) {
	tabID = tabObj.getAttribute("tab");
	if (getTabIdByContent("listTransfers") != tabID) {
		// reset maximize tab, if maxTab gets closed
		if (tabID == maxTab)
			resetMaxTab();
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

		// remove snaplines
		snapLines[ tabID ] = null
	}
}
function getContentFrameByTab(tabObj)
{
	if ( tabObj != null)
	{
		return tabObj.lastChild;
	}
	else
	{
		return null;
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
function getSnapLinesByObject(obj)
{
	x1 = obj.offsetLeft;
	y1 = obj.offsetTop;
	x2 = Number( obj.offsetLeft + obj.offsetWidth )
	y2 = Number( obj.offsetTop + obj.offsetHeight )
	return [x1, y1, x2, y2]
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
function loadMaxTab() {
	savedMaxTab = getCookie("maxTab");
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
function markTabOversized( tabObj )
{
	$(tabObj).addClass( 'oversizeTab' )
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
		checkTabOversize( tabs[ maxTab ] );
	}

	// only set new if an other tab was chosen
	if (tabObj != oldTab) {
		// set new max tab
		maxTab = tabID;
		maxzIndex = tabObj.style.zIndex;
		checkTabOversize( tabObj )
		$(tabObj).addClass( 'maximizedTab' );
		changeFixState(tabObj.firstChild);
		tabObj.style.zIndex = 5000;
		tabObj.style.height = maxHeight+'px';
		tabObj.style.width = maxWidth+'px';

	// on same tab, return to normal state (done above)
	// and reset default values
	} else resetMaxTab();

	if (optionSet('maxtab'))
		saveMaxTabCookie();
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
function resetMaxTab() {
	// if maxTab is the saved max tab, delete its flag
	if (maxTab == savedMaxTab) {
		savedMaxTab = null;
		saveMaxTabCookie();
	}

	maxTab = null;
	maxzIndex = null;
}
function saveMaxTabCookie() {
	if (maxTab) {
		var now = new Date();
		fixDate(now);
		// expires after one year
		now.setTime(now.getTime() + 365 * 24 * 60 * 60 * 1000);
		var value = getRegTabById(maxTab);
		setCookie("maxTab", value, now);
	// if no max tab given, clear cookie
	} else deleteCookie("maxTab");
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
		zIndex++;
		toActivate.style.zIndex = zIndex;
		activeTab = tab;
		reindexStatusbar();
		refreshTabbar();

		// save snaplines for this new tab
		snapLines[ tab ] = getSnapLinesByObject( toActivate );
	}
}
function toggleTabOversized(tabObj)
{
	if ( $(tabObj).hasClass( 'oversizeTab' ) )
	{
		$(tabObj).removeClass( 'oversizeTab' );
	}
	else
	{
		$(tabObj).addClass( 'oversizeTab' );
	}
}
function unmarkTabOversized(tabObj)
{
	if ( $(tabObj).hasClass( 'oversizeTab' ) )
	{
		$(tabObj).removeClass( 'oversizeTab' )
	}
}
