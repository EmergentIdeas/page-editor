Tripartite = {
		templates: {
			defaultTemplate: function(thedata) {
				return '' + thedata;
			}
		},
		constants: {
			templateBoundary: '__',
			templateNameBoundary: '##'
		},
		// This object (if set) will receive the template functions parsed from a script
		// I want to be able to call my templates as global functions, so I've set it
		// to be the window object
		secondaryTemplateFunctionObject: window
};

(function(t) {
var st = t.secondaryTemplateFunctionObject;

t.addTemplate = function(name, template) {
	if(typeof template !== 'function') {
		template = t.pt(template);
	}
	t.templates[name] = template;
	return template;
};

t.parseTemplateScript = function(tx) {
	var tks = t.tts(tx);
	/* current template name */
	var ctn = null;
	for(var i = 0; i < tks.length; i++) {
		var token = tks[i];
		if(token.active) {
			ctn = token.content;
		}
		else {
			if(ctn) {
				var template = t.addTemplate(ctn, t.stw(token.content));
				if(st) {
					st[ctn] = template;
				}
				ctn = null;
			}
		}
	}
}

/* strip template whitespace */
t.stw = function(txt) {
	var i = txt.indexOf('\n');
	if(i > -1 && txt.substring(0, i).trim() == '') {
		txt = txt.substring(i + 1);
	}
	i = txt.lastIndexOf('\n');
	if(i > -1 && txt.substring(i).trim() == '') {
		txt = txt.substring(0, i);
	}
	return txt;
};

t.ActiveElement = function(/* the conditional */cd, data, hd) {
	/* assign the conditional expression */
	this.ce = cd;
	/* assign the data selector expression */
	this.dse = data;
	
	/* assign the hd expression */
	if(hd) {
		this.he = hd;
	}
	else {
		this.he = 'defaultTemplate';
	}
	
	/* evaluated data */
	this.ed = null;
};

var ae = t.ActiveElement;

/* SimpleTemplate */
t.st = function(/* conditional expression */ cd, data, /* handling expression */ hd) {
	var el = new ae(cd, data, hd);
	return function(cc) {
		return el.run(cc);
		};
};


ae.prototype.run = function(/* current context */cc) {
	/* run template */
	var rt = false;
	/* evaluated data */
	this.ed = this.edse(cc);
	if(this.ce) {
		rt = this.eic(cc, this.ce);
	}
	else {
		if(this.ed instanceof Array) {
			if(this.ed.length > 0) {
				rt = true;
			}
		}
		else {
			if(this.ed) {
				rt = true;
			}
		}
	}
	
	var at = this.he;
	if(at.charAt(0) == '$') {
		at = this.eic(cc, at.substring(1));
	}
	if(!at) {
		at = 'defaultTemplate';
	}
	
	if(rt) {
		if(this.ed instanceof Array) {
			var r = '';
			for(var i = 0; i < this.ed.length; i++) {
				r += t.templates[at](this.ed[i]);
			}
			return r;
		}
		else {
			return t.templates[at](this.ed);
		}
	}
	return '';
};

/* evaluate data selector expression */
ae.prototype.edse = function(cc) {
	if(!this.dse) {
		return null;
	}
	if(this.dse === '$this') {
		return cc;
	}
	return this.eic(cc, this.dse);
};

/* evaluate in context */
ae.prototype.eic = function(cc, ex) {
	cc = cc || {};
	return this.eicwt.call(cc, cc, ex);
};

/* Evaluate in context having been called so that this === cc (current context */
ae.prototype.eicwt = function(cc, ex) {
	with (cc) {
		try {
			return eval(ex);
		} catch(e) {
			return null;
		}
	}
};

/* parse template */
t.pt = function(tx) {
	var tks = t.tt(tx);
	var pt = [];
	for(var i = 0; i < tks.length; i++) {
		var tk = tks[i];
		if(tk.active) {
			pt.push(t.tap(tk.content));
		}
		else {
			if(tk.content) {
				pt.push(tk.content);
			}
		}
	}
	
	return function(cc) {
		var r = '';
		for(var i = 0; i < pt.length; i++) {
			if(typeof pt[i] === 'string') {
				r += pt[i];
			}
			else {
				r += pt[i](cc);
			}
		}
		return r;
	};
};

/* tokenize active part */
t.tap = function(tx) {
	var con = null;
	var dat = null;
	var han = null;
	
	/* condition index */
	var ci = tx.indexOf('??');
	if(ci > -1) {
		con = tx.substring(0, ci);
		ci += 2;
	}
	else {
		ci = 0;
	}
	
	/* handler index */
	var hi = tx.indexOf('::');
	if(hi > -1) {
		dat = tx.substring(ci, hi);
		han = tx.substring(hi + 2);
	}
	else {
		dat = tx.substring(ci);
	}
	return new t.st(con, dat, han);
}

/* tokenize template */
t.tt = function(tx) {
	return t.taib(tx, t.constants.templateBoundary);
}

/** tokenize template script */
t.tts = function(tx) {
	return t.taib(tx, t.constants.templateNameBoundary);
}

/* tokenize active and inactive blocks */
t.taib = function(tx, /*Active Region Boundary */ bnd) {
	/* whole length */
	var l = tx.length;
	
	/* current position */
	var p = 0;
	
	/* are we in an active region */
	var act = false;
	
	var tks = [];
	
	while(p < l) {
		var i = tx.indexOf(bnd, p);
		if(i == -1) {
			i = l;
		}
		var tk = { active: act, content: tx.substring(p, i)};
		tks.push(tk);
		p = i + 2;
		act = !act;
	}
	
	return tks;
}
})(Tripartite);



