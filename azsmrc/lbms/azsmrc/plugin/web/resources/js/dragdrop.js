var drag_object = null;
var drag_pos = [0,0];
var mouse_pos = [0,0];
var zIndex = 0;
function init_dragdrop(dragclass, setdragbar) {
	document.onmousemove = drag_move;
	document.onmouseup = drop;
	var drags = [];
	var divs = document.getElementsByTagName("div");
	// against recollection of DOM nodes	
	for (var i in divs)
		drags[drags.length] = divs[i];
	// set dragbar if needed but dragability
	for (var i in drags) {
		if (drags[i].className == dragclass) {
			if (setdragbar)		
				set_dragbar(drags[i]);
			else
				drags[i].onmousedown = function () { drag(this); }
		}
	}
}
function changeFixState(dragbar) {
		dragbar.onmousedown = (dragbar.onmousedown == null) ? function () { drag(this.parentNode); } : null;
		dragbar.className = (dragbar.className == "fixdragbar") ? "dragbar" : "fixdragbar";		
}
function drag(element) {
	drag_object = element;
	drag_object.style.position = "absolute";
	drag_object.className = "moveTab";
	drag_pos[0] = mouse_pos[0] - drag_object.offsetLeft;
	drag_pos[1] = mouse_pos[1] - drag_object.offsetTop;
	zIndex++;
	drag_object.style.zIndex = zIndex;
}
function drag_move(e) {
	mouse_pos[0] = document.all ? window.event.clientX : e.pageX;
	mouse_pos[1] = document.all ? window.event.clientY : e.pageY;
	if (drag_object != null) {
		drag_object.style.left = (mouse_pos[0] - drag_pos[0]) + "px";
		drag_object.style.top = (mouse_pos[1] - drag_pos[1]) + "px";
	}
}
function drop() {
	if (drag_object != null) {
		drag_object.className = "tab";
		drag_object = null;
	}
}
function set_dragbar(obj) {
	if (obj.firstChild)
		if (obj.firstChild.className != "dragbar") {
			var dragbar = document.createElement("div");
			dragbar.className = "dragbar";
			dragbar.onmousedown = function () { drag(this.parentNode); }
			dragbar.ondblclick = function () { changeFixState(this); }
			obj.insertBefore(dragbar, obj.firstChild);
		}
	else {
		var dragbar = document.createElement("div");
		dragbar.className = "dragbar";
		dragbar.onmousedown = function () { drag(this.parentNode); }
		dragbar.ondblclick = function () { changeFixState(this); }
		obj.insertBefore(dragbar, obj.firstChild);
	}
	obj.style.position = "absolute";
}