/***************************************************
	  		   PAGE TRANSITIONS
***************************************************/

(function($){
    $.fn.extend({
        bra_page_transitions: function(options) {
 

            var defaults = {
                static_container: ".static-content-wrapper",
				dynamic_container: ".dynamic-content-wrapper",
				transition_effect: "slide_fade", //slide_fade, fade_fade slide_slide
				init_open : "#home"
           };
            
function case_url(menu_url) {
	var menu_url_1 = "";
	var menu_url_2 = "";
	
	// case 1 url + #section
	// case 2 #section
	// case 3 url
	// case 4 #
	
	var is_hash = menu_url.indexOf("#");
	
	if (is_hash == "-1") menu_url_1 = menu_url;
	else {
		menu_url_1 = menu_url.substr(0, menu_url.indexOf("#"));
		menu_url_2 = menu_url.substr(menu_url.indexOf("#"));
		
	}
	
	if (menu_url_1 != "" && menu_url_2 != "") {
		//alert("case 1")
		return "case 1";
	} 
	if (menu_url_1 == "" && menu_url_2 != "" && menu_url_2 != "#") {
		//alert("case 2")
		return "case 2";
	}
	if (menu_url_1 != "" && menu_url_2 == "") {
		//alert("case 3")
		return "case 3";
	}
	if (menu_url_1 == "" && menu_url_2 == "#") {
		//alert("case 4")
		return "case 4";
	}	
}
function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function checkExt(e) {//use in a form event or ina input
       value=e;
	if( !value.match(/(jpg)|(gif)|(png)/) ){//here your extensions
		return "no";	//actions like focus, not validate...
	}
	else {//right extension
		return "yes";	//actions
	}
}
function bg_trans(bg_color) {
	var default_bg_color = $("body").css("backgroundColor");
	if (checkExt(bg_color) == "yes") { // it's a image
		$("body").animate({ backgroundColor: default_bg_color }, 1000);
		if (bg_color.indexOf("?tiled") > 0) { // it's a tiled, not backstretch					
			bg_color = bg_color.substring(0, bg_color.indexOf("?tiled"));
			$("body").css("background-image", "url(" + bg_color + ")");
			$.backstretch("images/blank.gif");
			$("#backstretch img").fadeOut(1000, function() {
				//$("#backstretch").remove();
			});			
		} else { // it's backstretch
			$.backstretch(bg_color, {speed: 1000}); 
		}
	} else{	

		if (bg_color == "") bg_color = default_bg_color;
		$("body").css("background-image", "none");
		$("body").animate({ backgroundColor: bg_color }, 1000);	
		$.backstretch("images/blank.gif");
		$("#backstretch img").fadeOut(1000, function() {
			//$("#backstretch").remove();
		});
	}
}		

            var options = $.extend(defaults, options);
         

                  var o = options;
                  var obj = $(this); 
				  
				  var default_bg_color = $("body").css("backgroundColor");

				  var hash_section = "";								
					$("ul.menu li a").each(function(e) {
						menu_url = $(this).attr("href");
						if (case_url(menu_url) == "case 1"){
							menu_url_1 = menu_url.substr(0, menu_url.indexOf("#"));
							menu_url_2 = menu_url.substr(menu_url.indexOf("#") + 1);
							new_menu_url = menu_url_1 + "?section=" + menu_url_2;
							$(this).attr("href", new_menu_url);
						}
					})
					
					var get_section = getUrlVars()["section"];

					if (get_section != undefined) {
						o.init_open = "#" + get_section;
						$(o.dynamic_container).children().hide();
						$(o.dynamic_container).children(o.init_open).slideDown("slow");
						var bg_color = $(".menu a[href$='"+ get_section +"']").attr("data-rel");
						bg_trans(bg_color);
						//bg_trans("images/blank.gif")
					
					} else {
						if ($("body").hasClass("home")){
							$(o.dynamic_container).children().hide();
							$(o.dynamic_container).children(o.init_open).slideDown("slow");	
							var bg_color = $(".menu a[href='"+ o.init_open +"']").attr("data-rel");
							bg_trans(bg_color);
							//bg_trans("images/blank.gif")
						} else {
							bg_trans("images/blank.gif")
						}
					}
					
					$("ul.menu li a").click(function(){								  
								  
						
						var menu_url = $(this).attr("href");
						
					
						if (case_url(menu_url) == "case 2") {
							$("ul.menu li a").removeClass("current");
							$(this).addClass("current");
							var section_url = menu_url;
							
							if (o.transition_effect == "slide_slide") {
								$("#footer").fadeOut("slow");
								var bg_color = $(this).attr("data-rel");
								bg_trans(bg_color);
								//bg_trans("images/blank.gif")
								$(o.static_container).fadeOut("slow", function(){
									$(o.dynamic_container).children("*:visible").slideUp("slow", function(){
										$(o.dynamic_container).children(section_url).slideDown("slow", function(){
											$(o.static_container).fadeIn("slow");
											$("#footer").fadeIn("slow");
										});
									});
								})
							}
							
							if (o.transition_effect == "fade_fade") {
								$("#footer").fadeOut("slow");
								
								var bg_color = $(this).attr("data-rel");
								bg_trans(bg_color);
								//bg_trans("images/blank.gif")
								$(o.static_container).fadeOut("slow")
								$(o.dynamic_container).children("*:visible").fadeOut("slow", function(){
									$(o.dynamic_container).children(section_url).fadeIn("slow");
									$(o.static_container).fadeIn("slow");
									$("#footer").fadeIn("slow");
								});
							}
							
							if (o.transition_effect == "slide_fade") {
								$("#footer").fadeOut("slow");
								
								var bg_color = $(this).attr("data-rel");
								bg_trans(bg_color);
								//bg_trans("images/blank.gif")
								$(o.static_container).fadeOut("slow")
								$(o.dynamic_container).children("*:visible").slideUp("slow", function(){
									$(o.dynamic_container).children(section_url).fadeIn("slow");
									$(o.static_container).fadeIn("slow");
									$("#footer").fadeIn("slow");
								});
							}
					
					
							return false;
						
						}
					
					})
					

        }
    });
})(jQuery);

$(document).ready(function() {
    $().bra_page_transitions({transition_effect: "slide_fade"});
});
