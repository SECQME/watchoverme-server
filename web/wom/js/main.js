$(function(){

  $('#general-list').on('shown.bs.collapse', function(){
    $('#general-head')
      .find('small')
      .removeClass('glyphicon-plus')
      .addClass('glyphicon-minus');
  }).on('hidden.bs.collapse', function(){
    $('#general-head')
      .find('small')
      .removeClass('glyphicon-minus')
      .addClass('glyphicon-plus');
  });

  $('#privacy-list').on('shown.bs.collapse', function(){
    $('#privacy-head')
      .find('small')
      .removeClass('glyphicon-plus')
      .addClass('glyphicon-minus');
  }).on('hidden.bs.collapse', function(){
    $('#privacy-head')
      .find('small')
      .removeClass('glyphicon-minus')
      .addClass('glyphicon-plus');
  });

  $('#contact-verify').keyup(function(){
    if ($(this).val() == '5') {
      $('#contact-submit').removeAttr('disabled').removeClass('disabled');
    } else {
      $('#contact-submit').attr('disabled', 'disabled').addClass('disabled');
    }
  });

  $('#contact-form').submit(function(e){
    var verified = 0;

    $(this).find('input[name=name], input[name=_replyto], textarea').each(function(){
      if ($(this).val().length > 0) verified++;
    });

    if ($('#contact-verify').val() == "5") verified++;

    if (verified == 4) {
      return true;
    }

    e.preventDefault();
  });

  $(window).scroll(function() {
      if($(this).scrollTop() > 80) {
          $('.navbar').addClass('opaque');
      } else {
          $('.navbar').removeClass('opaque');
      }
  });

});