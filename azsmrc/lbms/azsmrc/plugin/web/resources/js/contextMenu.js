var contextMenu;
var contextDraw = false;
var contextCallObj = null;
function checkContextMenu(event) {
	contextDraw = (event.button == 2) ? true : false;
	contextCallObj = event.target;	
	if ((event.shiftKey) && (event.button == 0)) {
		contextDraw = true;
		addDebugEntry("valid");
		showContextMenu(event);
	}
}
function closeContextMenu(event) {
	contextMenu.style.display = "none";
}
function initContextMenu() {
	contextMenu = document.getElementById("contextmenu");
	document.body.onmousedown = checkContextMenu;
	document.body.oncontextmenu = showContextMenu;
	contextMenu.style.display = "none";
	contextMenu.style.position = "absolute";
}
function renderContextMenuContent(event) {
	while (contextMenu.firstChild) contextMenu.removeChild(contextMenu.lastChild);
	addDebugEntry("contextCallObj: "+contextCallObj.tagName.toLowerCase());
	var menulist, menuitem, link;
	var head = document.createElement("h2");
	switch (contextCallObj.tagName.toLowerCase()) {
		case "td":
			// is TC
			if (contextCallObj.parentNode.getAttribute("hash") != "") {
				hash = contextCallObj.parentNode.getAttribute("hash");
				addDebugEntry("hash: "+hash);
				head.appendChild(document.createTextNode("Torrent Control"));
				contextMenu.appendChild(head);
				menulist = document.createElement("ul");
				menuitem = document.createElement("li");
				link = document.createElement("a");
				link.onclick = function () { SendRequestToServer(7, hash); }
				link.appendChild(document.createTextNode("start torrent"));
				menuitem.appendChild(link);
				menulist.appendChild(menuitem);
				menuitem = document.createElement("li");
				link = document.createElement("a");
				link.onclick = function () { SendRequestToServer(13, hash); }
				link.appendChild(document.createTextNode("force start torrent"));
				menuitem.appendChild(link);
				//menulist.appendChild(menuitem);
				menuitem = document.createElement("li");
				link = document.createElement("a");
				link.onclick = function() { SendRequestToServer(5, hash); }
				link.appendChild(document.createTextNode("stop torrent"));
				menuitem.appendChild(link);
				menulist.appendChild(menuitem);
				menuitem = document.createElement("li");
				link = document.createElement("a");
				link.onclick = function () { SendRequestToServer(4, hash); }
				link.appendChild(document.createTextNode("delete torrent"));
				menuitem.appendChild(link);
				menulist.appendChild(menuitem);
				contextMenu.appendChild(menulist);
			}
		break;
		default:
			return false;
		break;
	}
	var input = document.createElement("input");
	input.setAttribute("type", "button");
	input.setAttribute("value", "Close");
	input.onclick = closeContextMenu;
	contextMenu.appendChild(input);
	return true;
}
function showContextMenu(event) {
	if (contextDraw) {
		contextMenu.style.display = "none";
		scrollLeft = document.body.scrollLeft;
		scrollTop = document.body.scrollTop;
		contextMenu.style.left = event.clientX + scrollLeft + "px";
		contextMenu.style.top = event.clientY + scrollTop + "px";
		if (renderContextMenuContent(event)) {
			contextMenu.style.display = "block";
			window.setTimeout("zIndex++; contextMenu.style.zIndex = zIndex", 200);
			return false;
		} else return true;
	}
}