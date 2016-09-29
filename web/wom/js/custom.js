function is_tablet(){
		if (navigator.userAgent.match(/Android/i) ||
	    navigator.userAgent.match(/webOS/i) ||
	    navigator.userAgent.match(/iPhone/i) ||
	    navigator.userAgent.match(/iPod/i) ||
	    navigator.userAgent.match(/iPad/i)) return true; else return false;
}

/*--------------------------------------------------
	 ADDITIONAL FUNCTIONS SCROLL TO TOP
---------------------------------------------------*/
$(document).ready(function(){

	// hide #back-top first
	$("#back-top").hide();
	
	// fade in #back-top
	$(function () {
		$(window).scroll(function () {
			if ($(this).scrollTop() > 100) {
				$('#back-top').fadeIn();
			} else {
				$('#back-top').fadeOut();
			}
		});

		// scroll body to 0px on click
		$('#back-top a').click(function () {
			$('body,html').animate({
				scrollTop: 0
			}, 800);
			return false;
		});
	});

});

  

/*--------------------------------------------------
		  DROPDOWN MENU
---------------------------------------------------*/
/*
 * Superfish v1.4.8 - jQuery menu widget
 * Copyright (c) 2008 Joel Birch
 *
 * Dual licensed under the MIT and GPL licenses:
 * 	http://www.opensource.org/licenses/mit-license.php
 * 	http://www.gnu.org/licenses/gpl.html
 *
 * CHANGELOG: http://users.tpg.com.au/j_birch/plugins/superfish/changelog.txt
 */

(function($){$.fn.superfish=function(op){var sf=$.fn.superfish,c=sf.c,$arrow=$(['<span class="',c.arrowClass,'"> &#187;</span>'].join("")),over=function(){var $$=$(this),menu=getMenu($$);clearTimeout(menu.sfTimer);$$.showSuperfishUl().siblings().hideSuperfishUl();},out=function(){var $$=$(this),menu=getMenu($$),o=sf.op;clearTimeout(menu.sfTimer);menu.sfTimer=setTimeout(function(){o.retainPath=($.inArray($$[0],o.$path)>-1);$$.hideSuperfishUl();if(o.$path.length&&$$.parents(["li.",o.hoverClass].join("")).length<1){over.call(o.$path);}},o.delay);},getMenu=function($menu){var menu=$menu.parents(["ul.",c.menuClass,":first"].join(""))[0];sf.op=sf.o[menu.serial];return menu;},addArrow=function($a){$a.addClass(c.anchorClass).append($arrow.clone());};return this.each(function(){var s=this.serial=sf.o.length;var o=$.extend({},sf.defaults,op);o.$path=$("li."+o.pathClass,this).slice(0,o.pathLevels).each(function(){$(this).addClass([o.hoverClass,c.bcClass].join(" ")).filter("li:has(ul)").removeClass(o.pathClass);});sf.o[s]=sf.op=o;$("li:has(ul)",this)[($.fn.hoverIntent&&!o.disableHI)?"hoverIntent":"hover"](over,out).each(function(){if(o.autoArrows){addArrow($(">a:first-child",this));}}).not("."+c.bcClass).hideSuperfishUl();var $a=$("a",this);$a.each(function(i){var $li=$a.eq(i).parents("li");$a.eq(i).focus(function(){over.call($li);}).blur(function(){out.call($li);});});o.onInit.call(this);}).each(function(){var menuClasses=[c.menuClass];if(sf.op.dropShadows&&!($.browser.msie&&$.browser.version<7)){menuClasses.push(c.shadowClass);}$(this).addClass(menuClasses.join(" "));});};var sf=$.fn.superfish;sf.o=[];sf.op={};sf.IE7fix=function(){var o=sf.op;if($.browser.msie&&$.browser.version>6&&o.dropShadows&&o.animation.opacity!=undefined){this.toggleClass(sf.c.shadowClass+"-off");}};sf.c={bcClass:"sf-breadcrumb",menuClass:"sf-js-enabled",anchorClass:"sf-with-ul",arrowClass:"sf-sub-indicator",shadowClass:"sf-shadow"};sf.defaults={hoverClass:"sfHover",pathClass:"overideThisToUse",pathLevels:1,delay:800,animation:{opacity:"show"},speed:"normal",autoArrows:true,dropShadows:true,disableHI:false,onInit:function(){},onBeforeShow:function(){},onShow:function(){},onHide:function(){}};$.fn.extend({hideSuperfishUl:function(){var o=sf.op,not=(o.retainPath===true)?o.$path:"";o.retainPath=false;var $ul=$(["li.",o.hoverClass].join(""),this).add(this).not(not).removeClass(o.hoverClass).find(">ul").hide().css("visibility","hidden");o.onHide.call($ul);return this;},showSuperfishUl:function(){var o=sf.op,sh=sf.c.shadowClass+"-off",$ul=this.addClass(o.hoverClass).find(">ul:hidden").css("visibility","visible");sf.IE7fix.call($ul);o.onBeforeShow.call($ul);$ul.animate(o.animation,o.speed,function(){sf.IE7fix.call($ul);o.onShow.call($ul);});return this;}});})(jQuery);

/*--------------------------------------------------
	     ADDITIONAL CODE FOR DROPDOWN MENU
---------------------------------------------------*/
    jQuery(document).ready(function($) { 
        $('ul.menu').superfish({ 
            delay:       100,                            // one second delay on mouseout 
            animation:   {opacity:'show',height:'show'},  // fade-in and slide-down animation 
            speed:       'fast',                          // faster animation speed 
            autoArrows:  false                           // disable generation of arrow mark-up 
        });
	}); 




/*--------------------------------------------------
	  			SLIDING GRAPH
---------------------------------------------------*/
(function($){
    $.fn.extend({
        //plugin name - animatemenu
        bra_sliding_graph: function(options) {
 
            var defaults = {
                speed: 1000
           };
            
			function isScrolledIntoView(id)
			{
				var elem = "#" + id;
				var docViewTop = $(window).scrollTop();
				var docViewBottom = docViewTop + $(window).height();
			
				if ($(elem).length > 0){
					var elemTop = $(elem).offset().top;
					var elemBottom = elemTop + $(elem).height();
				}
		
				return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom)
				  && (elemBottom <= docViewBottom) &&  (elemTop >= docViewTop) );
			}
		
			function sliding_horizontal_graph(id, speed){
				//alert(id);
				$("#" + id + " li span").each(function(i){
					var j = i + 1; 										  
					var cur_li = $("#" + id + " li:nth-child(" + j + ") span");
					var w = cur_li.attr("title");
					cur_li.animate({width: w + "%"}, speed);
				})
			}
			
			function graph_init(id, speed){
				$(window).scroll(function(){
					if (isScrolledIntoView(id)){
						sliding_horizontal_graph(id, speed);
					}
					else{
						//$("#" + id + " li span").css("width", "0");
					}
				})
				
				if (isScrolledIntoView(id)){
					sliding_horizontal_graph(id, speed);
				}
			}			

            var options = $.extend(defaults, options);
         
            return this.each(function() {
                  var o = options;
                  var obj = $(this); 
				  graph_init(obj.attr("id"), o.speed);

				  
            }); // return this.each
        }
    });
})(jQuery);

$(document).ready(function() {
    $('#services-graph').bra_sliding_graph({speed: 1000});
});




/*--------------------------------------------------
	     TOGGLE STYLE
---------------------------------------------------*/
jQuery(document).ready(function($) {								
	$(".toggle-container").hide(); 
	$(".trigger").toggle(function(){
		$(this).addClass("active");
		}, function () {
		$(this).removeClass("active");
	});
	$(".trigger").click(function(){
		$(this).next(".toggle-container").slideToggle();
	});
});




/*--------------------------------------------------
	     ACCORDION
---------------------------------------------------*/
$(document).ready(function(){	
	$('.trigger-button').click(function() {
		$(".trigger-button").removeClass("active")
	 	$('.accordion').slideUp('normal');
		if($(this).next().is(':hidden') == true) {
			$(this).next().slideDown('normal');
			$(this).addClass("active");
		 } 
	 });
	$('.accordion').hide();
});




/*--------------------------------------------------
	    FLICKR, SOCIALIZE AND PORTFOLIO IMAGE HOVER
---------------------------------------------------*/
$(function() {
$('.social-bookmarks li a, .social-personal li a').animate({ opacity: 0.5}, 0) ;
$('.social-bookmarks li a, .social-personal li a').each(function() {
$(this).hover(
function() {
$(this).stop().animate({ opacity: 1.0 }, 50);
},
function() {
$(this).stop().animate({ opacity: 0.5 }, 50);
})
});
});


/*--------------------------------------------------
	   HIDDEN FOOTER CONTENT ADDITIONAL CODE
---------------------------------------------------*/
jQuery(function($) {
    $('#trigger').hide();
    var footerHeight = $('#footer-content').outerHeight(true);
    $('.footer-trigger').click(function(){
        $('#trigger').animate({
            'height':'toggle'
        }, 500);
        $(this).toggleClass('expanded');
        return false;
    });
});


/*--------------------------------------------------
	   FOOTER CENTERING ADDITIONAL CODE
---------------------------------------------------*/
jQuery(function($) {
    var left_space = parseInt($("#header").offset().left);
	$("#trigger").css("left", left_space + "px");
	$(".footer-trigger").css("left", left_space + "px");
	
	$(window).resize(function() {
		var left_space = parseInt($("#header").offset().left);
	    $("#trigger").css("left", left_space + "px");
		$(".footer-trigger").css("left", left_space + "px");
	
	})
});



/*--------------------------------------------------
	   TOP NOTIFICATION ADDITIONAL CODE
---------------------------------------------------*/
$(window).load(function() {
   var notification_wrapper_visible = true;
   if (notification_wrapper_visible) {
    $(".notification").hide();
    $(".notification").slideDown(700, 'easeOutBounce');
    $("#notification-trigger").addClass("active");
   } else {
    $(".notification").css("display", "none");
    $("#notification-trigger").removeClass("active");
   }
   $('#notification-trigger').click(function() {
    $(".notification").slideToggle(700, 'easeOutBounce', function(){
     if ($(".notification").is(":visible")) {
      $("#notification-trigger").addClass("active"); 
     } else {
      $("#notification-trigger").removeClass("active");
     }
    });
   });
});

