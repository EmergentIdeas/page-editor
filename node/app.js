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


