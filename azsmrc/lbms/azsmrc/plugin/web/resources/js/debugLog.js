var debugLog;
function addDebugEntry(debugInfo) {
	debugLog = getTabByContent("debug")
	if (debugLog) {
		debugLog = debugLog.lastChild;
		var newEntry = document.createElement("li");
		newEntry.appendChild(document.createTextNode(debugInfo));
		debugLog.appendChild(newEntry);
	}
}
function clearDebugLog() {
	debugLog = getTabByContent("debug")
	if (debugLog) {
		debugLog = debugLog.lastChild;
		while (debugLog.firstChild)
			debugLog.removeChild(debugLog.firstChild);
	}
}
function initDebugLog() {
	debugLog = getTabByContent("debug");
	if (debugLog) {
		debugLog.removeChild(debugLog.lastChild);
		debugLog.appendChild(document.createElement("ul"));
		debugLog = debugLog.lastChild;
		debugLog.className = "debuglist";
	}
}