window.require = require
window.jQuery = window.$ = require('jquery')


var Tripartite = require('tripartite')
/*
var templates = require('../static_content/templates/pages.tmpl')
Tripartite.parseTemplateScript(templates)
*/

require('../resources/page-editor/js/page-editor')


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

