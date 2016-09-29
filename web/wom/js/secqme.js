/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var slideShowTimer;
var slideShowSpeed;

function MM_swapImgRestore() { //v3.0
    var i,x,a=document.MM_sr;
    for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() { //v3.0
    var d=document;
    if(d.images){
        if(!d.MM_p) d.MM_p=new Array();
        var i,j=d.MM_p.length,a=MM_preloadImages.arguments;
        for(i=0; i<a.length; i++)
            if (a[i].indexOf("#")!=0){
                d.MM_p[j]=new Image;
                d.MM_p[j++].src=a[i];
            }
    }
}

function MM_findObj(n, d) { //v4.01
    var p,i,x;
    if(!d) d=document;
    if((p=n.indexOf("?"))>0&&parent.frames.length) {
        d=parent.frames[n.substring(p+1)].document;
        n=n.substring(0,p);
    }
    if(!(x=d[n])&&d.all) x=d.all[n];
    for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
    for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
    if(!x && d.getElementById) x=d.getElementById(n);
    return x;
}

function MM_swapImage() { //v3.0
    var i,j=0,x,a=MM_swapImage.arguments;
    document.MM_sr=new Array;
    for(i=0;i<(a.length-2);i+=3)
        if ((x=MM_findObj(a[i]))!=null){
            document.MM_sr[j++]=x;
            if(!x.oSrc) x.oSrc=x.src;
            x.src=a[i+2];
        }
}

function open_tracking_win(trackingPin) {
    var centerWidth = (window.screen.width - 800) / 2;
    var centerHeight = (window.screen.height - 500) / 2;

    newWindow = window.open('http://secq.me/mainportal/tracking.jsf?trackingpin=' + trackingPin,
        'Tracking Detail', 'resizable=0,width=' + 800 +
        ',height=' + 500 +
        ',left=' + centerWidth +
        ',top=' + centerHeight +
        ',scrollbars=yes');

    newWindow.focus();
    return newWindow.name;

}


function open_news_win(newURL, windowsTitle) {
    var centerWidth = (window.screen.width - 800) / 2;
    var centerHeight = (window.screen.height - 500) / 2;

    newWindow = window.open(newURL,
        windowsTitle, 'resizable=0,width=' + 800 +
        ',height=' + 500 +
        ',dependent=yes' +
        ',left=' + centerWidth +
        ',top=' + centerHeight +
        ',scrollbars=yes');

    newWindow.focus();
    return newWindow.name;

}


function slideShow(speed) {
   slideShowSpeed = speed;

   //append a LI item to the UL list for displaying caption
    $('ul.slideshow').append('<li id="slideshow-caption" class="caption"><div class="slideshow-caption-container"><h3></h3><p></p></div></li>');
    //Set the opacity of all images to 0
    showFirstSlide();

    //Call the gallery function to run the slideshow
    timer = setInterval('gallery()',speed);
    slideShowTimer = timer;

    //pause the slideshow on mouse over
    $('ul.slideshow').hover(
        function () {
            clearInterval(timer);
        },
        function () {
            timer = setInterval('gallery()',speed);
        }
        );

}

function stopSlideShow() {
    clearInterval(slideShowTimer);
}

function startSlideShow() {
    slideShowTimer = setInterval('gallery()', slideShowSpeed);
}

function showFirstSlide() {
    $('ul.slideshow li').css({
        opacity: 0.0
    });

    //Get the first image and display it (set it to full opacity)
    $('ul.slideshow li:first').css({
        opacity: 1.0
    });

    //Get the caption of the first image from REL attribute and display it
    $('#slideshow-caption h3').html($('ul.slideshow a:first').find('img').attr('title'));
    $('#slideshow-caption p').html($('ul.slideshow a:first').find('img').attr('alt'));

    //Display the caption
    $('#slideshow-caption').css({
        opacity: 0.7,
        bottom:0
    });
}

function gallery() {

    //if no IMGs have the show class, grab the first image
     var current = ($('ul.slideshow li.show')?  $('ul.slideshow li.show') : $('#ul.slideshow li:first'));

    //Get next image, if it reached the end of the slideshow, rotate it back to the first image
    var next = ((current.next().length) ? ((current.next().attr('id') == 'slideshow-caption')? $('ul.slideshow li:first') :current.next()) : $('ul.slideshow li:first'));
    // var next = current.next();
    
    //Get next image caption
    var title = next.find('img').attr('title');
    var desc = next.find('img').attr('alt');

    //Set the fade in effect for the next image, show class has higher z-index
    next.css({
        opacity: 0.0
    }).addClass('show').animate({
        opacity: 1.0
    }, 1000);

    //Hide the caption first, and then set and display the caption
    $('#slideshow-caption').slideToggle(300, function () {
        $('#slideshow-caption h3').html(title);
        $('#slideshow-caption p').html(desc);
        $('#slideshow-caption').slideToggle(500);
    });

    //Hide the current image
    current.animate({
        opacity: 0.0
    }, 1000).removeClass('show');

}

function startslideShow() {
    $('#mycomic').toggle();
    if (!$('#mycomic').is(':visible')) {
        stopSlideShow();
    } else {
        startSlideShow();
    }

}

function highlightCurrentNavigation(element){
	var inputs, index;

	inputs = document.getElementsByTagName('li');
	for (index = 0; index < inputs.length; ++index) {
	    inputs[index].className = '';
	}
	
	var newObject = MM_findObj(element, null);
	newObject.className = 'active';
	return true;
}

