var $ = require('jquery')
var ImageInInput = require('ei-pic-browser/image-in-input')

var urlsListPath = '/files/thumbnails/urls'
var fileUploadUrlPrefix = '/files/upload'
var thumbnailsPathPrefix = '/files/thumbnails/thumb'

var getPossibleImages = function(url, callback) {
	$.get(urlsListPath + url + "?fileType=image", function(data) {
		// var imgObjects = [];
		// $.each(data, function() {
		// 	imgObjects.push(this);
		// });
		callback(data)
	});
}

var UploadableImage = function(inputControl) {
	this.$inputControl = $(inputControl)
	var prefix = this.$inputControl.attr('data-image-dir')
	var self = this
	
	getPossibleImages(prefix, function(images) {
		var img = new ImageInInput(self.$inputControl, function() {
			return images
		}, function(data, name) {
			$.ajax({
				url: fileUploadUrlPrefix + prefix,
				type: 'POST',
				cache: false,
				data: {
					dataUrl: data,
					dataFilename: name
				}
			});
			
			var finalUrl = prefix + (prefix.endsWith('/') ? '' : '/') + name;
			
			self.$inputControl.val(finalUrl);
		}, prefix)
		self.img = img
		img.render()
	})
}

UploadableImage.prototype.cleanup = function() {
	this.img.cleanup()
}

module.exports = UploadableImage
