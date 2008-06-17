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