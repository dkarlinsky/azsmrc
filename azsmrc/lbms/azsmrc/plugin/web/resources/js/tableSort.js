var tableCount = 0;
function initTableSort() {
	var tables = document.getElementsByTagName("table");
	var thead;
	for (var i in tables)
		if (i < tables.length) {
			thead = tables[i].getElementsByTagName("thead")[0].getElementsByTagName("tr")[0].getElementsByTagName("th");
			for (var j in thead)
				if (j < thead.length) {
					thead[j].setAttribute("tab",i);
					thead[j].setAttribute("col",j);
                    thead[j].onclick = function() { sortTab(this.parentNode.parentNode.parentNode, this.getAttribute("col")); };
				}
			tableCount++;
		}
}
function addTableToSort(obj) {
	if (obj.nodeName == "TABLE") {
		thead = obj.getElementsByTagName("thead")[0].getElementsByTagName("tr")[0].getElementsByTagName("th");
		for (var j in thead)
			if (j < thead.length) {
				thead[j].setAttribute("tab", tableCount);
				thead[j].setAttribute("col",j);
				thead[j].onclick = function() { sortTab(this.parentNode.parentNode.parentNode, this.getAttribute("col")); };
			}
		tableCount++;
	}
}
function sortTab(tab, col) {
	// sorting table content
	function colSort(a, b) {
		var result;
		result = (a[col] > b[col]) ? 1 : (a[col] == b[col]) ? 0 : -1;
		return result;
	}
	// reading table content
	var table = tab.getElementsByTagName("tbody")[0];
	var rows = table.getElementsByTagName("tr");
	var sorting = new Array(rows.length-1);
	var cols;
	for (var i in rows)
		if (i < rows.length) {
            cols = rows[i].getElementsByTagName("td");
	        if (cols.length > 0) {
				sorting[i] = new Array();
				for (var j in cols)
					if ((j < cols.length) && (cols.length > 0))
						sorting[i][j] = cols[j].firstChild.data;
			}
		}
	sorting.sort(colSort);
	//rewriting table content
	var tr, td;
	for (i in rows)
		if (i < rows.length) {
            cols = rows[i].getElementsByTagName("td");
	        if (cols.length > 0)
				for (var j in cols)
					if (j < cols.length) {
                        while (cols[j].firstChild) cols[j].removeChild(cols[j].firstChild);
                        cols[j].appendChild(document.createTextNode(sorting[i][j]));
					}
		}
}