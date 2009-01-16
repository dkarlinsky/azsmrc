var drag_object = null;
var drag_pos = [0,0];
var mouse_pos = [0,0];
var zIndex = 0;
var snapDistance = 10;
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
function checkValidDropPosition()
{
	// check for given object
	if ( drag_object != null )
	{
		// current values
		width = drag_object.clientWidth;
		height = drag_object.clientHeight;

		// check vertical positions
		// object is above zero point (first pixel line of viewport)
		compareValue = drag_object.style.top.split( 'px' )[0];
		if ( compareValue < 0 )
		{
			drag_object.style.top = "0px";
		}
		// object is below viewport
		if ( compareValue >= window.innerHeight )
		{
			drag_object.style.top = (window.innerHeight - 50) + "px";
		}

		// check horizontal positions
		compareValue = drag_object.style.left.split( 'px' )[0];
		// outside left
		if ( (compareValue + width) < 0 )
		{
			drag_object.style.left = "0px";
		}
		// outside right
		if ( compareValue > window.innerWidth )
		{
			drag_object.style.left = (window.innerWidth - 50) + "px";
		}
	}
}
function drag(element) {
	drag_object = element;
	drag_object.style.position = "absolute";
	$(drag_object).addClass('moveTab');
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

		// only try to snap, if there are more than this tab
		if ( snapLinesCount > 1 )
		{
			// calculate rules
			currentPosition = getSnapLinesByObject( drag_object );
			ownId = drag_object.getAttribute( "tab" );

			// these rules are virtual
			// not all are really present (explained below)
			rules = [];
			// vertical rules
			rules[ 0 ] = [];
			// horizontal rules
			rules[ 1 ] = [];
			for ( var i in snapLines )
			{
				// current window don't need to set up rules
				// it won't be here anymore when dragged
				if ( i != ownId && snapLines[ i ] != null )
				{
					// in order to check only for left and top position (rules)
					// a virtual rule is created
					// which is the right edge of a window set depending on the left edge of the dragged window
					// defined by the width of the dragged window

					// x1 coordinate of any tab
					rules[ 0 ].push( snapLines[ i ][ 0 ] );
					// virtual x1 coordinate
					rules[ 0 ].push( snapLines[ i ][ 0 ] - drag_object.offsetWidth );
					// x2 coordinate of any tab
					rules[ 0 ].push( snapLines[ i ][ 2 ] );
					// virtual x2 coordinate
					rules[ 0 ].push( snapLines[ i ][ 2 ] - drag_object.offsetWidth );

					// same goes for horizontal line with bottom
					// y1 coordinate of any tab
					rules[ 1 ].push( snapLines[ i ][ 1 ] );
					// virtual y2 coordinate
					rules[ 1 ].push( snapLines[ i ][ 1 ] - drag_object.offsetHeight );
					// y2 coordinate of any tab
					rules[ 1 ].push( snapLines[ i ][ 3 ] );
					// virtual y2 coordinate
					rules[ 1 ].push( snapLines[ i ][ 3 ] - drag_object.offsetHeight );
				}
			}

			// check for vertical snap
			for ( var snapLine in rules[ 0 ] )
			{
				// snapLine is an x value of a guide line (vertical rule)
				snapLine = rules[ 0 ][ snapLine ];

				// define distance from current guide line
				if ( snapLine > currentPosition[ 0 ] )
				{
					distance = snapLine - currentPosition[ 0 ];
				}
				else
				{
					distance = currentPosition[ 0 ] - snapLine;
				}

				// check for snap range
				if ( distance <= snapDistance )
				{
					drag_object.style.left = snapLine + "px";
				}
			}

			// check for horizontal snap
			for ( var snapLine in rules[ 1 ] )
			{
				// snapLine is an x value of a guide line (vertical rule)
				snapLine = rules[ 1 ][ snapLine ];

				// define distance from current guide line
				if ( snapLine > currentPosition[ 1 ] )
				{
					distance = snapLine - currentPosition[ 1 ];
				}
				else
				{
					distance = currentPosition[ 1 ] - snapLine;
				}

				// check for snap range
				if ( distance <= snapDistance )
				{
					drag_object.style.top = snapLine + "px";
				}
			}
		}
	}
}
function drop() {
	if (drag_object != null) {
		// check for valid position
		checkValidDropPosition();

		// after validation of drop position, save the guide lines for snapping
		tabId = drag_object.getAttribute("tab");
		// save the guide lines
		snapLines[ tabId ] = getSnapLinesByObject( drag_object );

		$(drag_object).removeClass('moveTab');
		$(drag_object).addClass('tab')
		drag_object = null;
	}
	if (optionSet("tabposonthefly")) {
		saveTabPosCookie();
		loadTabPos();
	}
}
function set_dragbar(obj) {
	if (obj.firstChild) {
		if (obj.firstChild.className != "dragbar") {
			var dragbar = document.createElement("div");
			dragbar.className = "dragbar";
			dragbar.onmousedown = function () { drag(this.parentNode); }
			dragbar.ondblclick = function () { changeFixState(this); }
			obj.insertBefore(dragbar, obj.firstChild);
		}
	} else {
		var dragbar = document.createElement("div");
		dragbar.className = "dragbar";
		dragbar.onmousedown = function () { drag(this.parentNode); }
		dragbar.ondblclick = function () { changeFixState(this); }
		obj.insertBefore(dragbar, obj.firstChild);
	}
	obj.style.position = "absolute";
}