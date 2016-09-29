(function($){
    $.fn.extend({
        //plugin name - animatemenu
        bra_photostream: function(options) {
 
            var defaults = {
                user: 'brankic1979',
                limit: 10,
				social_network: 'dribbble'
				
            };
            
			
			function create_html(data, container) {
				var feeds = data.feed;
				if (!feeds) {
					return false;
				}
				var html = '';		
				//html +=	'<div class="pinterest_header"><a href="'+feeds.link+'" title="'+ feeds.description +'">'+ feeds.title +'</a></div>';
				html += '<ul>';
					
				for (var i = 0; i < feeds.entries.length; i++) {
					var entry = feeds.entries[i];
					var content = entry.content;
					html += '<li>'+ content +'</li>'		
				}
					
				html += '</ul>';
					
				$(container).html(html);
			
				$(container).find("li").each(function(){
					pin_img_src = $(this).find("img").attr("src");
					pin_url = "http://www.pinterest.com" + $(this).find("a").attr("href");
					pin_desc = $(this).find("p:nth-child(2)").html();
					pin_desc = pin_desc.replace("'", "`");
					$(this).empty();
					$(this).append("<a target='_blank' href='" + pin_url + "' title='" + pin_desc + "'><img src='" + pin_img_src + "' alt=''></a>");
					var img_w = $(this).find("img").width();
					var img_h = $(this).find("img").height();
					if (img_w < img_h){
						$(this).find("img").addClass("portrait")
					}
					else {
						$(this).find("img").addClass("landscape")
					}
				});
			};
			
			
			

			
						
			
			
			
            var options = $.extend(defaults, options);
         
            return this.each(function() {
                  var o = options;
                  var obj = $(this); 
				  
				  if (o.social_network == "dribbble") {
					  obj.append("<ul></ul>")
					  $.getJSON("http://dribbble.com/" + o.user + "/shots.json?callback=?", function(data){
							$.each(data.shots, function(i,shot){
								if (i < o.limit) {
								  var img_title = shot.title;
								  img_title = img_title.replace("'", "`")
								  var image = $('<img/>').attr({src: shot.image_teaser_url, alt: img_title});
								  var url = $('<a/>').attr({href: shot.url, target: '_blank', title: img_title});
								  var url2 = $(url).append(image);
								  var li = $('<li/>').append(url2);
								  $("ul", obj).append(li);
								}
							});
							$("li img", obj).each(function(){
								var img_w = $(this).width();
								var img_h = $(this).height();
								if (img_w < img_h){
									$(this).addClass("portrait")
								}
								else {
									$(this).addClass("landscape")
								}
							});	
					   });		  
				  }
				  if (o.social_network == "pinterest") {  
					var url = 'http://pinterest.com/' + o.user + '/feed.rss'
					var api = "http://ajax.googleapis.com/ajax/services/feed/load?v=1.0&callback=?&q=" + encodeURIComponent(url);
					api += "&num=" + o.limit;
					api += "&output=json_xml"
					
					//alert(api);
				
					// Send request
					$.getJSON(api, function(data){	
						// Check for error
						if (data.responseStatus == 200) {
							// Process the feeds
							create_html(data.responseData, obj);
				
							// Optional user callback function
							if ($.isFunction(fn)) fn.call(this,$e);
							
						} else {
							alert("wrong user for pinterest");
				
						};
					});	
				  }
				  if (o.social_network == "flickr") { 
						obj.append("<ul></ul>")
						$.getJSON("http://api.flickr.com/services/rest/?method=flickr.people.findByUsername&username=" + o.user+ "&format=json&api_key=85145f20ba1864d8ff559a3971a0a033&jsoncallback=?", function(data){
							var nsid = data.user.nsid;
							$.getJSON("http://api.flickr.com/services/rest/?method=flickr.photos.search&user_id=" + nsid + "&format=json&api_key=85145f20ba1864d8ff559a3971a0a033&per_page=" + o.limit + "&page=1&extras=url_sq&jsoncallback=?", function(data){
								$.each(data.photos.photo, function(i,img){
									var img_owner = img.owner;
									var img_title = img.title;
									var img_src = img.url_sq;
									var img_id = img.id;
									var img_url = "http://www.flickr.com/photos/" + img_owner + "/" + img_id;
									var image = $('<img/>').attr({src: img_src, alt: img_title});
									var url = $('<a/>').attr({href: img_url, target: '_blank', title: img_title});
									var url2 = $(url).append(image);
									var li = $('<li/>').append(url2);
									$("ul", obj).append(li);
								})
						   });
					   });	
						

					
					
					
					
				  }
				  
            }); // return this.each
        }
    });
})(jQuery);

$(document).ready(function() {
    $('.dribbble_list').bra_photostream({user: 'brankic1979', 
										limit:10, 
										social_network: 'dribbble'});
	
	$('.pinterest_list').bra_photostream({user: 'irishmark', 
										 limit:10, 
										 social_network: 'pinterest'});
	
	$('.flickr_list').bra_photostream({user: 'aleksandra jovanic', 
									  limit:10, 
									  social_network: 'flickr'});
		
	$('.flickr_list_footer').bra_photostream({user: 'aleksandra jovanic', 
									  limit:9, 
									  social_network: 'flickr'});
});


