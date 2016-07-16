/*
 * jQuery Growl plugin
 * Version 1.0.0-b1 (04/18/2008)
 * @requires jQuery v1.2.3 or later
 *
 * Examples at: http://fragmentedcode.com/jquery-growl
 * Copyright (c) 2008 David Higgins
 * 
 * Special thanks to Daniel Mota for inspiration:
 * http://icebeat.bitacoras.com/mootools/growl/
 */

/*

USAGE:

	$.growl(title, msg);
	$.growl(title, msg, image);
	$.growl(title, msg, image, priority);

THEME/SKIN:

You can override the default look and feel by updating these objects:
$.growl.settings.displayTimeout = 4000;
$.growl.settings.noticeTemplate = ''
  + '<div>'
  + '<div style="float: right; background-image: url(my.growlTheme/normalTop.png); position: relative; width: 259px; height: 16px; margin: 0pt;"></div>'
  + '<div style="float: right; background-image: url(my.growlTheme/normalBackground.png); position: relative; display: block; color: #ffffff; font-family: Arial; font-size: 12px; line-height: 14px; width: 259px; margin: 0pt;">' 
  + '  <img style="margin: 14px; margin-top: 0px; float: left;" src="%image%" />'
  + '  <h3 style="margin: 0pt; margin-left: 77px; padding-bottom: 10px; font-size: 13px;">%title%</h3>'
  + '  <p style="margin: 0pt 14px; margin-left: 77px; font-size: 12px;">%message%</p>'
  + '</div>'
  + '<div style="float: right; background-image: url(my.growlTheme/normalBottom.png); position: relative; width: 259px; height: 16px; margin-bottom: 10px;"></div>'
  + '</div>';
$.growl.settings.noticeCss = {
  position: 'relative'
};


To change the 'dock' look, and position: 

$.growl.settings.dockTemplate = '<div></div>';
$.growl.settings.dockCss = {
    position: 'absolute',
    top: '10px',
    right: '10px',
    width: '300px'
  };
  
The dockCss will allow you to 'dock' the notifications to a specific area
on the page, such as TopRight (the default) or TopLeft, perhaps even in a
smaller area with "overflow: scroll" enabled?
*/

(function($) {
if (/1\.(0|1|2)\.(0|1|2)/.test($.fn.jquery)) {
    alert('jQuery Growl requires jQuery v1.2.3 or later!  You are using v' + $.fn.jquery);
    return;
}

$.growl = function(title,message,image,priority) { notify(title,message,image,priority); }
$.growl.version = "1.0.0-b1";

function create(rebuild) {
	var instance = document.getElementById('growlDock');
	if(!instance || rebuild) {
	  instance = $(jQuery.growl.settings.dockTemplate).attr('id', 'growlDock').addClass('growl');
	  
	  if(jQuery.growl.settings.defaultStylesheet) {
	    $('head').append('<link rel="stylesheet" type="text/css" href="' + jQuery.growl.settings.defaultStylesheet + '" />');
	  }
	  
	} else {
	  instance = $(instance);
	}
	instance.css(jQuery.growl.settings.dockCss);
	
	$('body').append(instance);
	
	return instance;
};
  
function r(text, expr, val) {
    while(expr.test(text)) {
      text = text.replace(expr, val);
    }
    return text;
};
  
function notify(title,message,image,priority) {
    var instance = create();
    var html = jQuery.growl.settings.noticeTemplate;
	if(typeof(html) == 'object') {
		// assume jQuery object, or DOM Element
		html = $(html).html();
	}
    html = r(html, /%message%/, (message?message:''));
    html = r(html, /%title%/, (title?title:''));
    html = r(html, /%image%/, (image?image:jQuery.growl.settings.defaultImage));
    html = r(html, /%priority%/, (priority?priority:'normal'));
    var notice = $(html);
    notice.css(jQuery.growl.settings.noticeCss);
    
	notice.hide();
	notice.fadeIn(jQuery.growl.settings.notice);
    instance.append(notice);
    setTimeout(function() {
	  jQuery.growl.settings.noticeRemove(notice, function() {
        this.parentNode.removeChild(this);
      });
    }, jQuery.growl.settings.displayTimeout);
};

  
// default settings
$.growl.settings = {
  dockTemplate: '<div></div>',
  dockCss: {
    position: 'fixed',
    top: '10px',
    right: '10px',
    width: '300px',
	zIndex: 50000
  },
  noticeTemplate: 
    '<div class="notice">' +
    ' <h3 style="margin-top: 15px">%title%</h3>' +
    ' <p>%message%</p>' +
    '</div>',
  noticeCss: {
    opacity: .75,
    backgroundColor: '#333333',
    color: '#ffffff'
  },
  noticeDisplay: function(notice) {
  	notice.fadeIn(jQuery.growl.settings.noticeFadeTimeout);
  },
  noticeRemove: function(notice, callback) {
  	notice.animate({opacity: '0', height: '0px'}, {duration:jQuery.growl.settings.noticeFadeTimeout, complete: callback});
  },
  noticeFadeTimeout: 'slow',
  displayTimeout: 3500,
  defaultImage: 'growl.jpg',
  defaultStylesheet: null,
  noticeElement: function(el) {
  	$.growl.settings.noticeTemplate = $(el);
  }
};
})(jQuery);