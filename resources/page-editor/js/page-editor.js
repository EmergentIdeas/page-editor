CKEDITOR.disableAutoInline = true;
CKEDITOR.config.extraPlugins = 'sourcedialog,justify,font';
CKEDITOR.config.allowedContent = true;
CKEDITOR.config.disableNativeSpellChecker = false;
pageEditingEnabled = false;
CKEDITOR.config.filebrowserBrowseUrl = '/files/browse/type/all';
CKEDITOR.config.filebrowserImageBrowseUrl = '/files/browse/type/image';
CKEDITOR.config.filebrowserUploadUrl = '/files/upload-file';

$(function() {
	$('.page-properties-inner input, .page-properties-inner textarea, .page-properties-inner select').on('keyup keypress blur change', function() {
		enablePageSave();
	});
	
	var checkEditorDirtyAndSetContent = function(editorObject) {
    	var inputIdentifier = $(editorObject.element).attr('data-input-identifier')
    	
    	if(inputIdentifier && "" != inputIdentifier) {
    		if(editorObject.checkDirty()) {
    			$(inputIdentifier).val(editorObject.getData());
    			enablePageSave();
    		}
    	}
    }
	
	var turnOnEditors = function() {
		$('.edit-content-inline').attr("contenteditable", "true").ckeditor(
				{
					on: {
						change: function(event) {
							checkEditorDirtyAndSetContent(this);
						},
		                blur: function( event ) {
							checkEditorDirtyAndSetContent(this);
		                }
		            },
		            filebrowserBrowseUrl : '/files/browse/type/all',
		            filebrowserImageBrowseUrl : '/files/browse/type/image',
		            filebrowserUploadUrl : '/files/upload-file'
				}
		);
	}
	
	$('#show-page-properties').on('click', function() {
		pageEditingEnabled = true;
		var panelId = '.page-properties-inner';
		if($(panelId).css('display') == 'none') {
			$(panelId).show();
		}
		$(this).hide();
		turnOnEditors();
		$('.edit-content-inline').addClass('edit-content-inline-show-is-editable');
		$('body').addClass('now-editing-page');
		
		var $pageprops = $('#page-properties');
		var height = $pageprops.height();
		
		$pageprops.css(
				{
					'position': 'fixed',
					'top': '0',
					'left': '0',
					'right': '0'
				}
		);
		$('body').prepend('<div style="height: ' + height + 'px;">&nbsp;</div>');
		
		return false;
	});
	
	$('.slug-source').on('change', function(event) {
		var $slug = $('input[name="slug"]');
		if(!$slug.val() || $slug().val() === '') {
			$slug.val(transformToSlug($(this).val()));
		}
	});
	
	$('.attachment-item .delete').on('click', function(event) {
		event.preventDefault();
		var $attachment = $(this).closest('.attachment-item');
		$.post($attachment.attr('data-delete-url'));
		$attachment.remove();
	});
	
	
	
	$('.blog-post-form').on('change', 'input.blog-item-attachment-file', function(event) { 
		if(typeof(fileNum) === 'undefined') {
			fileNum = 2;
		}
		else {
			fileNum++;
		}
		if($(this).val() != '') {
			$(this).after('<br/><input type="file" class="form-control blog-item-attachment-file" name="file' + fileNum + '" />');
		}
	});
	
	/* Open the pane for the special editing properties */
	$('.page-details-expander').on('click', function(evt) {
		evt.preventDefault();
		openPageDetails(this);
	});
});


function openPageDetails(pageDetailsLink) {
	var $detailsPaneToChange = $($(pageDetailsLink).attr('data-details-pane'));
	if($detailsPaneToChange.hasClass('hidden-details')) {
		// the currently select pane is closed, so make sure all the others are closed before we open it
		$('.page-details').addClass('hidden-details');
	}
	$detailsPaneToChange.toggleClass('hidden-details');
	
}

function enablePageSave() {
	$('.page-properties-save').removeAttr('disabled');	
}

function transformToSlug(title) {
	title = title.toLowerCase();
	title = title.replace(/\s+/g, '-');
	return title;
}


function sdbmCode(str){
    var hash = 0;
    for (i = 0; i < str.length; i++) {
        char = str.charCodeAt(i);
        hash = char + (hash << 6) + (hash << 16) - hash;
    }
    return hash;
}
