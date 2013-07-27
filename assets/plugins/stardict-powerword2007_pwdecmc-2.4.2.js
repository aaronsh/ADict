function publishDict(DictDiv) {
	var handlePhonetic = function(ItemDiv) {
		var phonetics = ItemDiv.getElementsByName('icibaNodeClass20');
		for ( var i = 0; i < phonetics.length; i++) {
			var e = phonetics[i];
			var parent = e.parentNode;
			if (e.previousElementSibling != null && parent != null) {
				parent.removeChild(e.previousElementSibling);
			}
			e.outerHTML = '<span style="color:green;">'
					+ fixPhonetic(e.innerHTML) + '</span>';
		}
	}
	var getPhonetic = function(div){		
		return div.innerHTML;
	}
	/* 取得“单词原型”区块内的单词 */
	var getWordText = function(div) {
		for ( var e = div.firstElementChild; e != null; e = e.nextElementSibling) {
			if (e.ElementName == 'icibaNodeClass16_2') {
				return e.innerHTML;
			}
		}
		return "";
	}
	
	
	var PreExplanationPhoneticHandler = function(text){
		var re = /\[([a-zA-Z579\(\)\^:\s]+)\]/g;
		var r = text.replace(re, 
				function($0, $1){
			var phonetic = $1;
			return '[<span style="color:green;">'+fixPhonetic( phonetic )+'</span>]';
		});//text.match(rexp);
		console.log(r);
		return r;
	}
	
	var ExampleHandler = function(ex){
		var e = ex.firstElementChild;
		for (; e != null; e = e.nextElementSibling) {
			switch (e.ElementName) {
			case 'icibaNodeClass1':
				//e.setAttribute('style','color:rgb(22, 101, 190);font-weight:900;');
				break;
			case 'icibaNodeClass15':
				e.setAttribute('style','font-size:14px; text-indent: 1em; color:#808080;font-weight:700;');
				break;
			}
		}
	}
	/* 基本词义区块处理 */
	var handleSectionJibenciyi = function(Section) {
		var e = Section.firstElementChild;
		for (; e != null; e = e.nextElementSibling) {
			var name = e.ElementName;
			if( name == 'icibaNodeClass3_1'){
				e.setAttribute('style','color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
			}
			else if( name == 'icibaNodeClass21_1'){
				e.setAttribute('style','color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
			}
			else if (name == 'icibaNodeClass7') {
				e.setAttribute("style", "border: 1px none blue;margin:1px;")
				for ( var item = e.firstElementChild; item != null; item = item.nextElementSibling) {
					switch (item.ElementName) {
					case 'icibaNodeClass16':
						item.setAttribute("style",
								"margin:1px;font-weight:900;background-color:#dde;height:24px;line-height:24px;")
						var word = getWordText(item);
						var next = item.nextElementSibling;
						if (next != null && next.ElementName == 'icibaNodeClass8') {
							item.innerHTML = word + '&nbsp;&nbsp;[<span style="color:green;">' + next.innerText +'</span>]';
							e.removeChild(next);
						} else {
							item.innerHTML = word;
						}
						break;
					case 'icibaNodeClass18':
						item.setAttribute('style','font-weight:900;');
						//item.removeAttribute("class");
						break;
					case 'icibaNodeClass19':
						item.innerHTML = PreExplanationPhoneticHandler(item.innerText);
						item.setAttribute("style", "text-indent: 1em;")
						//item.removeAttribute("class");
						break;
					case 'icibaNodeClass12':
						item.setAttribute("style", "text-indent: 1em; font-size=16px; background:url(img/dot_brown.png) no-repeat; background-position:0.5em 6px;");
						//item.removeAttribute("class");
						break;
					case 'icibaNodeClass14':
						ExampleHandler(item);
						var html = item.outerHTML;
						for ( var next = item.nextElementSibling; next != null
								&& next.ElementName == 'icibaNodeClass14';) {
							ExampleHandler(next);
							html = html + next.outerHTML;
							var prev = next;
							next = next.nextElementSibling;
							e.removeChild(prev);
						}
						item.innerHTML = '<span style="margin:8px 0px 5px 0.5em; padding:0 40px 0 0; color:#333333;font-weight:700; background:url(img/examples1.png) no-repeat; background-position:right center; display:inline-block;">例句</span>'
								+ '<div style ="border: 1px solid #366; border-radius: 3px; padding:5px;">' 
								+ html 
								+'</div>';
						item.setAttribute("style","padding:3px;");
						//item.removeAttribute("class");
						break;
					}
				}
			}
		}
	}

	/* 继承用法区块处理 */
	var handleSectionJiChengYongFa = function(Section) {
		var e = Section.firstElementChild;
		for (; e != null; e = e.nextElementSibling) {
			switch(e.ElementName){
			    case 'icibaNodeClass22_1':
				    e.setAttribute('style','color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
			        break;
			case 'icibaNodeClass7':
				//e.setAttribute("style", "border: 1px solid blue;margin:1px;")
				var html = '';
				for ( var item = e.firstElementChild; item != null; item = item.nextElementSibling) {
					switch (item.ElementName) {
					case 'icibaNodeClass16':
						var word = getWordText(item);
						html = '<span style="font-weight:700; display:inline-block; color:#630;">' +word
								+ '</span>';
						break;
					case 'icibaNodeClass12':
						html = html + '<span style="padding:3px;">'
								+ item.innerHTML + '</span>';
						break;
					case 'icibaNodeClass8':
						html = html + '&nbsp;&nbsp;[<span style="color:green;">' + item.innerText +'</span>]';
						break;
					case 'icibaNodeClass18':
						html = html + '<span style="padding:0px 6px 0px 6px;font-weight:700;display:inline-block;">'
								+ item.innerHTML + '</span>';
					}
				}
				e.innerHTML = html;
				break;
			}
		}
	}

	/* 习惯用语区块处理 */
	var handleSectionXiGuanYongYu = function(Section) {
		var e = Section.firstElementChild;
		for (; e != null; e = e.nextElementSibling) {
			switch(e.ElementName){
			    case 'icibaNodeClass5_1':
				e.setAttribute('style','color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
				break;
			case 'icibaNodeClass7':
				e.setAttribute("style", "margin-bottom:3px;")
				var html = '';
				for ( var item = e.firstElementChild; item != null; item = item.nextElementSibling) {
					switch (item.ElementName) {
					case 'icibaNodeClass16':
						var word = getWordText(item);
						if (word != '') {
							html = '<span style="font-weight:700; display:inline-block; padding-right:1.5em;color:#630;" >' +word
									+ '</span>';
						}
						break;
					case 'icibaNodeClass12':
						html = html + '<span style="padding:3px;">'
								+ item.innerHTML + '</span>';
						break;
					case 'icibaNodeClass8':
						html = html + getPhonetic(item);
						break;
					case 'icibaNodeClass18':
						html = html + '<span style="padding:2px;">'
								+ item.innerHTML + '</span>';
					}
				}
				e.innerHTML = html;
			}
		}
	}
	
	/* 特殊用法区块处理 */
	var handleSectionTeShuYongFa = function(Section) {
		var e = Section.firstElementChild;
		for (; e != null; e = e.nextElementSibling) {
		    switch(e.ElementName){
		        case 'icibaNodeClass23_1':
		        e.setAttribute('style','color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
		        break;
		    case 'icibaNodeClass7':
				//e.setAttribute("style", "border: 1px solid blue;margin:1px;border-bottom:3px solid #699;")
				var html = '';
				for ( var item = e.firstElementChild; item != null; item = item.nextElementSibling) {
					switch (item.ElementName) {
					case 'icibaNodeClass16':
						var word = getWordText(item);
						if (word != '') {
							html = '<span style="font-weight:700; display:inline-block; padding-right:1.5em;color:#630;" >' +word
									+ '</span>';
						}
						break;
					case 'icibaNodeClass12':
						html = html + '<span style="padding:3px;">'
								+ item.innerHTML + '</span>';
						break;
					case 'icibaNodeClass8':
						html = html + getPhonetic(item);
						break;
					case 'icibaNodeClass18':
						html = html + '<span style="padding:2px;">'
								+ item.innerHTML + '</span>';
						break;
					}
				}
				e.innerHTML = html;
			}
		}
	}
	
	var handleHightlightWord = function(text){
		var re = /&L{([^}]+)}/g;
		var r = text.replace(re, '<a href="bword://$1" ><b>$1</b></a>');//text.match(rexp);
		console.log(r);
		return r;
	}
	/* 参考词汇区块处理 */
	var handleSectionCanKaoCiHui = function(Section) {
		var e = Section.firstElementChild;
		for (; e != null; e = e.nextElementSibling) {
			switch(e.ElementName){
			    case 'icibaNodeClass24_1':
				e.setAttribute('style','color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
				break;
			case 'icibaNodeClass7':
				//e.setAttribute("style", "border: 1px solid blue;margin:1px;")
				var html = '';
				var relativeWords = false;
				for ( var item = e.firstElementChild; item != null; item = item.nextElementSibling) {
					switch (item.ElementName) {
					case 'icibaNodeClass25':
						if( relativeWords ){
							item.innerHTML = '<span style="margin-right:1em; padding:0em 0.5em 0em 0.5em; background-color:#ffc;display:inline-block;">反义词</span>' + handleHightlightWord(item.innerText);
						}	
						else{
							item.innerHTML = '<span style="margin-right:1em; padding:0em 0.5em 0em 0.5em; background-color:#ffc;display:inline-block;">近义词</span>' + handleHightlightWord(item.innerText);
						}
						relativeWords = true;
						break;
					case 'icibaNodeClass19':
					case 'icibaNodeClass12':
						item.setAttribute("style", "text-indent: 1em;")
						break;
					case 'icibaNodeClass14':
						item.setAttribute("style", "border: 1px solid black; background-color:#ded; border-radius: 5px; padding:3px;margin:2px;");
						break;
					}
				}
			}
		}
	}
	var parseWordItem = function(ItemDiv) {
		//ItemDiv.removeAttribute("class");
		//ItemDiv.setAttribute("style", "border: 1px solid red;")
		handlePhonetic(ItemDiv);
		for ( var section = ItemDiv.firstElementChild; section != null; section = section.nextElementSibling) {
			var name = section.ElementName;
			switch (name) {
			case 'icibaNodeClass3':
				handleSectionJibenciyi(section);
				break;
			case 'icibaNodeClass21':
				handleSectionJibenciyi(section);
				break;
			case 'icibaNodeClass22':
				handleSectionJiChengYongFa(section);
				break;
			case 'icibaNodeClass5':
				handleSectionXiGuanYongYu(section);
				break;
			case 'icibaNodeClass23':
				handleSectionTeShuYongFa(section);
				break;
			case 'icibaNodeClass24':
				handleSectionCanKaoCiHui(section);
				break;
			}
			//section.setAttribute("style", "border: 1px solid red; margin:2px;")
		}
	}

    DictDiv.loadElementName();
	var words = DictDiv.getElementsByClassName('wordBox');
	for ( var i = 0; i < words.length; i++) {
		for ( var ele = words[i].firstElementChild; ele != null; ele = ele.nextElementSibling) {
			if (ele.ElementName == 'icibaNodeClass2') {
				parseWordItem(ele);
			}
		}
	}
	return;
}
