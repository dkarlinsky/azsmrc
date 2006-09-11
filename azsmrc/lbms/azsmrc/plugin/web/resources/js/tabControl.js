var activeTab = 1;
var tabCount = 2;
var registeredTabs = [];
// a example tab (tabbar is list of tabs)
// <li><span onclick="SendRequestToServer(1);">ALL Torrents</span><img src="img/delete.png" alt="Close Tab" title="Close Tab" /></li>
function addTab() {
}
function closeTab(tabObj) {
	tabID = tabObj.getAttribute("tab");
	if (tabID == activeTab) {
		var tabbar = document.getElementById("tabbar");
		ShowTab(tabbar.firstChild.getAttribute("tab_control"));
	}
	tabControl = tabObj.parentNode;
	tabControl.parentNode.removeChild(tabControl);
	removeTab = document.getElementById("tab_"+tabID);
	removeTab.parentNode.removeChild(removeTab);
	tabCount--;
}
function initTabControl() {
	// requirements
	// element with id: tabcontents
	// count of childnodes tabs with id: tab_1, tab_2 ...
	var tabbar = document.getElementById("tabbar");
	if (tabbar.hasChildNodes) {
		var i = 1;
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
