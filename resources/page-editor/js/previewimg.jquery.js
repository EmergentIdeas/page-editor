/**
 * This jquery plugin takes a div which contains an image, input, and a link
 * and gives the user the ability to:
 * 1. Drop a pic onto the image and have it uploaded to the server
 * or
 * 2. Select a new image from the server
 * 
 * It assumes the server has the ability to:
 * 1. Provide a list of img urls that can be selected
 * 2. Take base 64 encoded data and write it to disk for storage
 * 
 * In whatever way the user selects a new img, this plugin attempts
 * to find an input element and set its value to be the new url.
 * 
 * Each element that uses this plugin probably will want to specify
 * the attribute "data-img-path". This attribute gives the absolute
 * path of the images to show as previews and save new images for.
 * This allows you to subset the images available by folder giving a
 * cleaner interface.
 * 
 * Requires:
 * jquery
 * jquery ui (for the dialog)
 * Tripartite templates
 * @param $
 */

(function($) {
	
	var defaults = {
			inputSelector: "input",
			imgSelector: 'img',
			serverDirectoryAttribute: 'data-img-path',
			previewDialogLinkSelector: 'a, button',
			instructionRemovalSelector: '.instructions',
			urlsListPath: "/files/thumbnails/urls/",
			thumbImageStyles: 'width: 50px; cursor: pointer;',
			fileUploadUrlPrefix: '/files/upload/',
			defaultImgDir: '/img',
			thumbnailsPathPrefix: '/files/thumbnails/thumb'
		};
		
	var templates = "##pickImgDialog##\n" + 
			"<div class=\"pick-img-dialog\" title=\"Choose an Image\">\n" + 
			"	<div class=\"images\">\n" + 
			"		__images::picImg__\n" + 
			"	</div>\n" + 
			"</div>\n" + 
			"\n" + 
			"##picImg##\n" + 
			"<img class=\"thumb\" src=\"__thumbnailsPathPrefix____url__\" data-resource=\"__url__\" />\n" + 
			"\n" + 
			"";
	
	Tripartite.parseTemplateScript(templates);
	var createdStyles = false;
	
	
	$.fn.previewimg = function(options) {
		if(typeof options == 'undefined') {
			options = {};
		}
		var options = $.extend({}, defaults, options);

		var theselector = this.selector;
		if(!createdStyles) {
			createdStyles = true;
			
			$('head').append('<style type="text/css">.pick-img-dialog .thumb { ' + options.thumbImageStyles + '}</style>');
		}
		
		
		if(typeof options == 'string') {
			
		}
		else {
			$(theselector).each(function() {
				var $currentitem = $(this);
				var url;
				if(typeof $currentitem.attr(options.serverDirectoryAttribute) != 'undefined') {
					 url = $currentitem.attr(options.serverDirectoryAttribute);
				}
				if(!url || 0 === url.length) {
					url = options.defaultImgDir; 
				}
				if(url.charAt(0) == '/') {
					url = url.substring(1);
				}
				
				var $field = $currentitem.find(options.inputSelector);
				
				$currentitem.on('dragenter', function (e) {
					e.stopPropagation();
					e.preventDefault();
					$(this).css('outline', 'dashed 1px #66AFE9');
				});
				$currentitem.on('dragover', function (e) {
					e.stopPropagation();
					e.preventDefault();
				});
				$currentitem.on('dragstop dragend dragleave', function (e) {
					e.stopPropagation();
					e.preventDefault();
					$(this).css('outline', '');
				});
				$currentitem.on('drop', function(evt) {
					evt.stopPropagation();
					evt.preventDefault();
					$(this).css('outline', '');
					var $img = $currentitem.find(options.imgSelector);
					
					var files = evt.originalEvent.target.files
							|| evt.originalEvent.dataTransfer.files;
					if(files.length > 0) {
						var file = files[0];
						var reader  = new FileReader();
						
						reader.onloadend = function () {
							$.ajax({
								url: options.fileUploadUrlPrefix + url,
								type: 'POST',
								cache: false,
								data: {
									dataUrl: reader.result,
									dataFilename: file.name
								}
							});
							$field.val('/' + url + '/' + file.name);
							$img.attr('src', reader.result);
							$currentitem.find(options.instructionRemovalSelector).remove();
						}
						reader.readAsDataURL(file);

					}
				});
				$currentitem.find(options.previewDialogLinkSelector).on('click', function(evt) {
					evt.preventDefault();
					if($(this).hasClass('delete')) {
						$field.val(null);
						var $img = $currentitem.find(options.imgSelector);
						$img.remove();
						return;
					}
					$.get(options.urlsListPath + url + "?fileType=image", function(data) {
						var imgObjects = [];
						$.each(data, function() {
							imgObjects.push({url: this, thumbnailsPathPrefix: options.thumbnailsPathPrefix});
						});
						var h = pickImgDialog({images: imgObjects});
						var $dialogContent = $(h);
						$dialogContent.dialog({
							width: "600px",
							open: function(event, ui) {
								$(this).find('.thumb').on('click', function(evt) {
									evt.preventDefault();
									var $img = $currentitem.find(options.imgSelector);
									var imgUrl = $(this).attr('data-resource');
									$field.val(imgUrl);
									$img.replaceWith('<img class="chosen-img-preview" src="' + imgUrl + '" />');
									$currentitem.find(options.instructionRemovalSelector).remove();
									$('.ui-dialog-content').dialog('close');
									$('.ui-dialog').remove();
								});
							}
						});
	
					});
				});
			});
		}
	}
	
})(jQuery);