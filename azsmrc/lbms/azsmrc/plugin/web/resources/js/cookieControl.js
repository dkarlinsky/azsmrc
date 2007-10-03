var registeredCookies = ["autoRefresh", "startupTabs", "selectedDetails"];
function clearCookies() {
	for (var i in registeredCookies)
		deleteCookie(registeredCookies[i]);
}
function deleteCookie(name, path, domain) {
	if (getCookie(name)) {
		document.cookie = name + "=" +
		((path) ? "; path=" + path : "") +
		((domain) ? "; domain=" + domain : "") +
		// simply set expires to a time in past
		"; expires=Thu, 01-Jan-70 00:00:01 GMT";
	}
}
function fixDate(date) {
	// date fix for netscape
	var base = new Date(0);
	var skew = base.getTime();
	if (skew > 0)
		date.setTime(date.getTime() - skew);
}
function getCookie(name) {
	var dc = document.cookie;
	var prefix = name + "=";
	var begin = dc.indexOf("; " + prefix);
	if (begin == -1) {
		begin = dc.indexOf(prefix);
		if (begin != 0) return null;
	} else
		begin += 2;
	var end = document.cookie.indexOf(";", begin);
	if (end == -1)
		end = dc.length;
	return unescape(dc.substring(begin + prefix.length, end));
}
function initCookies() {
	var value, i;
	value = getCookie("autoRefresh");
	if (value)
		autoRefresh = value.split(",");
	value = getCookie("startupTabs");
	if (value) {
		value = value.split(",");
		for (i in value)
			if (value[i] == "true")
				startupTabs[i] = true;
			else
				startupTabs[i] = false;
	}
	value = getCookie("selectedDetails");
	if (value)
		selectedDetails = value.split(",");
	value = getCookie("tabPositions");
	if (value) {
		value = value.split(";");
		for (i in value) {
			pos = value[i].split(",");
			tabPositions[pos[0]] = [pos[1], pos[2], pos[3]];
		}		
	}		
}
function setCookie(name, value, expires, path, domain, secure) {
	var curCookie = name + "=" + escape(value) +
		((expires) ? "; expires=" + expires.toGMTString() : "") +
		((path) ? "; path=" + path : "") +
		((domain) ? "; domain=" + domain : "") +
		((secure) ? "; secure" : "");
	document.cookie = curCookie;
}