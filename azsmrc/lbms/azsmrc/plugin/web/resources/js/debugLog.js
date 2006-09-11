var debugLog;
function addDebugEntry(debugInfo) {
	var newEntry = document.createElement("li");
	newEntry.appendChild(document.createTextNode(debugInfo));
	debugLog.appendChild(newEntry);
}
function initDebugLog() {
	debugLog = document.getElementById("tab_3");
	debugLog.removeChild(debugLog.lastChild);
	debugLog.appendChild(document.createElement("ul"));
	debugLog = debugLog.lastChild;
	debugLog.className = "debuglist";
}
