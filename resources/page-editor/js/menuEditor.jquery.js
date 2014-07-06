(function($) {
	var defaults = {
			templatesLocation: "/page-editor/templates/menu-editor.templates"
	};
	
	$.fn.menuEditor = function(options) {

		var theselector = this.selector;
		var $menuContainer = $(this.selector);
		var $theTree = $menuContainer.find('.the-tree');
		var $itemDetails = $menuContainer.find('.item-details');
		
		var menuDataPrefix = $menuContainer.attr('data-menu-data-prefix');
		
		var fullOptions = $.extend({}, defaults, options);
		
		if(!fullOptions.menuDataPrefix) {
			fullOptions.menuDataPrefix = menuDataPrefix;
		}
		
		function saveTree() {
			console.log('saving tree');
			$.ajax({
				url: fullOptions.menuDataPrefix,
				type: 'PUT',
				data: {
					data: $theTree.tree('toJson')
				}
			})
		}
		
		function setupListeners() {
			$.get(fullOptions.menuDataPrefix).done(function(data) {
				$theTree.tree(
						{ 
							data: JSON.parse(data),
							dragAndDrop: true
						}
					);
				
				$theTree.on('tree.select', function(evt) {
					if(evt.node) {
						$itemDetails.html(menuItemEdit());
						var node = evt.node;
						$itemDetails.binddata({ id: node.id, itemLabel: node.name, itemUrl: node.itemUrl, customClasses: node.customClasses });
						console.log('clicked: ' + evt.node.name);
					}
				});
				
				$theTree.on('tree.move', function(evt) {
					saveTree();
				});
				
				$itemDetails.on('click', '.save', function() {
					var $form = $(this).closest('form');
					var node = $theTree.tree('getNodeById', $form.find('[name=id]').val());
					$.each($form.serializeArray(), function() { node[this.name] = this.value });
					node.label = node.itemLabel;
					$theTree.tree('updateNode', node, node.label);
					saveTree();
				});

				$menuContainer.find('.add-menu-item').on('click', function(evt) {
					evt.preventDefault();
					var selected = $theTree.tree('getSelectedNode');
					var appended = $theTree.tree('appendNode', {
						label: 'new item',
						id: 'n' + new Date().getTime()
					}, selected);
					if(selected) {
						$theTree.tree('openNode', selected);
					}
					$theTree.tree('scrollTo', appended);
					$theTree.tree('selectNode', appended);
				});
			});
		};
		
		if(!Tripartite.templates.menuItemEdit) {
			$.get('/page-editor/templates/menu-editor.templates').done(function(data) {
				Tripartite.parseTemplateScript(data);
				setupListeners();
			});
		}
		else {
			setupListeners();
		}
		

	};
})(jQuery);