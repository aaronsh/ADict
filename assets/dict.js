String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
}
String.prototype.ltrim = function() {
	return this.replace(/(^\s*)/g, "");
}
String.prototype.rtrim = function() {
	return this.replace(/(\s*$)/g, "");
}

function toggleDict(targetid) {
	if (document.getElementById) {
		var target = document.getElementById(targetid);
		var img = document.getElementById(targetid + 'Btn');
		if (target.style.display == 'none') {
			target.style.display = 'block';
			img.src = "img/close.png";
		} else {
			target.style.display = 'none';
			img.src = "img/expand.png";
		}

	}
}

function toggleDictContents(e) {
	var img = e.firstElementChild;
	e = e.nextElementSibling;
	e = e.nextElementSibling;
	if (e.style.display == 'none') {
		e.style.display = 'block';
		img.src = "img/close.png";
	} else {
		e.style.display = 'none';
		img.src = "img/expand.png";
	}
}

function toggleWord(e) {
	var wordDetailDive = e.nextElementSibling;
	if (wordDetailDive.style.display == 'none') {
		wordDetailDive.style.display = 'block';
		e.style.background = "url('img/minus.png') no-repeat left center"
	} else {
		wordDetailDive.style.display = 'none';
		e.style.background = "url('img/plus.png') no-repeat left center"
	}
}

function toggleExample(targetid) {
	if (document.getElementById) {
		var target = document.getElementById(targetid);
		var img = document.getElementById(targetid + 'Btn');
		if (target.style.display == 'none') {
			target.style.display = 'block';
			img.src = "img/up_brown.png";
		} else {
			target.style.display = 'none';
			img.src = "img/down_brown.png";
		}

	}
}

function getEleByClass(classname, tagname, parentNode) {
	var parent = parentNode || document.body;
	var tagname = tagname || '*';
	var o = parent.getElementsByTagName(tagname);
	var p = [];
	for ( var i = 0; i < o.length; i++) {
		var e = o[i];
		var classNames = e.className.split(' ');
		for ( var j = 0; j < classNames.length; j++) {
			if (classname == classNames[j]) {
				p.push(e);
				// p[p.length]=o[i];
				break;
			}
		}
	}
	return p;
}

function handlePlugins() {
	console.log("handlePlugins 1");
	for(var i=0; i<PlugIns.length; i++){
		console.log(i+" name:"+PlugIns[i].name+" func:"+PlugIns[i].func);
		var plugin = PlugIns[i];
		var e = document.getElementById(plugin.name);
		plugin.func(e);
	}
}

window.onload = handlePlugins;