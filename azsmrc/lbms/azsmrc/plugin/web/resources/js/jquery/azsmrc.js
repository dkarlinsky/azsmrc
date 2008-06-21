$(document).ready(function () {
	$("a#picmenuactivator").toggle(function () {
		$("#picmenu")
			.animate({
					height: '54px'
				}, {
					queue: "true",
					duration: "slow",
					complete: function () {
						$("#picmenu").addClass('active');
					}
				}
			);
	}, function (){
		$("#picmenu")
			.removeClass('active')
			.animate({
					height: '10px'
				}, {
					duration: "slow"
				}
			);
	});
});

// growl settings
$.growl.settings.displayTimeout = 5000;
$.growl.settings.noticeTemplate = ''
	+ '<div class="growlContainer">'
	+ '<div class="growlHeader"></div>'
	+ '<div class="growlContent">'
	+ ' <img class="growlImage" src="img/growl/%image%" />'
	+ ' <h3 class="growlTitle">%title%</h3>'
	+ ' <p class="growlMessage">%message%</p>'
	+ '</div>'
	+ '<div class="growlBottom"></div>'
	+ '</div>';
$.growl.settings.noticeCss = {
	position: 'relative'
};