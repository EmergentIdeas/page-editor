window.require = require
window.jQuery = window.$ = require('jquery')

require('jquery-ui')
window.Tripartite = require('tripartite')
/*
var templates = require('../static_content/templates/pages.tmpl')
Tripartite.parseTemplateScript(templates)
templates = require('../static_content/templates/app.tmpl')
Tripartite.parseTemplateScript(templates)
*/
require('crud-tools').allowItemsSort()


var UploadableImage = require('../resources/page-editor/uploadable-image')

$('[data-image-dir]').each(function() {
	new UploadableImage(this)
})

$('.categories.check-list input').change(function(event) {
	var vals = []
	$('.categories.check-list input').each(function() {
		var $this = $(this)
		if($this.prop('checked')) {
			vals.push($this.val())
		}
		
	})
	$('.catCombined').val(vals.join(','))
});

