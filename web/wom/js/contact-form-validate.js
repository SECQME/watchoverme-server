
jQuery(document).ready(function($){
	$('form#contact-form').submit(function() {
		$('form#contact-form .contact-error').remove();
		var hasError = false;
		$('form#contact-form .requiredField').each(function() {
			if(jQuery.trim($(this).val()) == '') {
            	var labelText = $(this).prev('label').text();
            	$(this).parent().append('<span class="contact-error">Required</span>');
            	$(this).addClass('inputError');
            	hasError = true;
            } else if($(this).hasClass('email')) {
            	var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
            	if(!emailReg.test(jQuery.trim($(this).val()))) {
            		var labelText = $(this).prev('label').text();
            		$(this).parent().append('<span class="contact-error">Invalid email address</span>');
            		$(this).addClass('inputError');
            		hasError = true;
            	}
            }
		});
		if(!hasError) {
			var formInput = $(this).serialize();
			$.post($(this).attr('action'),formInput, function(data){
				$("form#contact-form").before('<div class="contact-success"><strong>THANK YOU!</strong><p>Your email was successfully sent. We will contact you as soon as possible.</p></div>');
			});
		}


		return false;

	});
});