var debugLog;
function addDebugEntry(debugInfo) {
	var newEntry = document.createElement("li");
	newEntry.appendChild(document.createTextNode(debugInfo));
	debugLog.appendChild(newEntry);
}
function clearDebugLog() {
	while (debugLog.firstChild)
		debugLog.removeChild(debugLog.firstChild);
}
function initDebugLog() {
	debugLog = getTabByContent("debug");
	debugLog.removeChild(debugLog.lastChild);
	debugLog.appendChild(document.createElement("ul"));
	debugLog = debugLog.lastChild;
	debugLog.className = "debuglist";
}
