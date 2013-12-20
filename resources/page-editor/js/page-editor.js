CKEDITOR.disableAutoInline = true;
CKEDITOR.config.extraPlugins = 'sourcedialog';
CKEDITOR.config.allowedContent = true;
CKEDITOR.config.disableNativeSpellChecker = false;
pageEditingEnabled = false;
$(function() {
	$('.page-properties-inner input').on('keyup keypress blur change', function() {
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
		return false;
	});
});

function enablePageSave() {
	$('.page-properties-save').removeAttr('disabled');	
}



function sdbmCode(str){
    var hash = 0;
    for (i = 0; i < str.length; i++) {
        char = str.charCodeAt(i);
        hash = char + (hash << 6) + (hash << 16) - hash;
    }
    return hash;
}
