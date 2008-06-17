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
	var img;
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
				link.onclick = function () { SendRequestToServer(4, hash); }
				img = document.createElement("img");
				img.src = "img/crystalClear/torrentControl/delete.png";
				img.setAttribute("alt", "Delete Torrent");
				img.setAttribute("title", "Delete Torrent");
				link.appendChild(img);
				menuitem.appendChild(link);
				menulist.appendChild(menuitem);

				menuitem = document.createElement("li");
				link = document.createElement("a");
				link.onclick = function() { SendRequestToServer(5, hash); }
				img = document.createElement("img");
				img.src = "img/crystalClear/torrentControl/stop.png";
				img.setAttribute("alt", "Stop Torrent");
				img.setAttribute("title", "Stop Torrent");
				link.appendChild(img);
				menuitem.appendChild(link);
				menulist.appendChild(menuitem);

				menuitem = document.createElement("li");
				link = document.createElement("a");
				link.onclick = function () { SendRequestToServer(7, hash); }
				img = document.createElement("img");
				img.src = "img/crystalClear/torrentControl/start.png";
				img.setAttribute("alt", "Start Torrent");
				img.setAttribute("title", "Start Torrent");
				link.appendChild(img);
				menuitem.appendChild(link);
				menulist.appendChild(menuitem);

				menuitem = document.createElement("li");
				link = document.createElement("a");
				link.onclick = function () { SendRequestToServer(13, hash); }
				img = document.createElement("img");
				img.src = "img/crystalClear/torrentControl/force_start.png";
				img.setAttribute("alt", "Force Start Torrent");
				img.setAttribute("title", "Force Start Torrent");
				link.appendChild(img);
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