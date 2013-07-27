function publishDict(DictDiv) {
	/* stardict-kdic-ec-11w-2.4.2.js */

	var phoneticHandler = function(text) {
		var arr = text.split("");
		var s = '';
		for ( var i = 0; i < arr.length; i++) {
			switch (arr[i]) {
			case '5':
				s = s + "'"
				break;
			case '7':
				s = s + "ˌ"
				break;
			case '9':
				s = s + 'ˈ';
				break;
			case '3 ':
				s = s + 'ə';
				break;
			case 'V':
				s = s + 'ʒ';
				break;
			case 'Q':
				s = s + 'ʌ';
				break;
			case '@':
				s = s + 'æ';
				break;
			case '^':
				s = s + 'ɡ';
				break;
			case 'J':
				s = s + 'ʊ';
				break;
			case '?':
				s = s + 'ŋ';
				break;
			case '+':
				s = s + 'ð';
				break;
			case 'E':
				s = s + 'ɛ';
				break;
			case ')':
				s = s + 'ɔ';
				break;
			case '&':
				s = s + 'ɑ';
				break;
			case 'U':
				s = s + 'u';
				break;
			case 'I':
				s = s + 'ɪ';
				break;
			case '$':
				s = s + 'ʃ';
				break;
			case 'R':
				s = s + 'ɔ';
				break;
			case '}':
				s = s + 'ɜ';
				break;
			case '%':
				s = s + 'θ';
				break;
			default:
				s = s + arr[i];
				break;
			}
		}
		return s;
	}

	var words = getEleByClass('wordBox', 'div', DictDiv);
	for ( var i = 0; i < words.length; i++) {
		var txt = words[i].innerText;
		var html = document.createElement('div');
		for ( var result = txt.match(/^\s*<([^>]+)>/); result != null; result = txt
				.match(/^\s*,\s*<(r[^>]+)>/)) {

			var phonetic = html.firstElementChild;
			if (phonetic == null) {
				phonetic = document.createElement('div');
				html.appendChild(phonetic);

				phonetic.appendChild(document.createTextNode('['));
			} else {
				phonetic.appendChild(document.createTextNode(', ['));
			}

			var span = document.createElement('span');
			span.innerHTML = phoneticHandler(result[1]);
			span.setAttribute("style", "color:green;");
			phonetic.appendChild(span);
			phonetic.appendChild(document.createTextNode(']'));

			txt = txt.substr(result[0].length);
		}

		// explanations
		var nodeExplanation = document.createElement('div');
		while (txt.length > 0) {
			var wordTypeMatch = txt.match(/^\s*([a-z]+)\./);
			var wordTypeNexMatch = txt.match(/;\s*([a-z]+)\./);

			if (wordTypeMatch == null) {
				nodeExplanation.appendChild(document.createTextNode(txt));
				break;
			}
			var wordTyp = wordTypeMatch[0];
			var wordExp = '';
			if (wordTypeNexMatch != null) {
				var pos = txt.indexOf(wordTypeNexMatch[0]) + 1;
				var len = pos - wordTypeMatch[0].length;
				wordExp = txt.substr(wordTypeMatch[0].length, len);
				txt = txt.substr(pos);
			} else {
				wordExp = txt.substr(wordTypeMatch[0].length);
				txt = '';
			}

			var wordExpItem = document.createElement('div');
			var wordTypSpan = document.createElement('span');
			wordTypSpan.innerText = wordTyp;
			wordTypSpan.setAttribute("style",
					"margin:3px;padding-right:5px; font-weight:900;");
			wordExpItem.appendChild(wordTypSpan);
			var wordExpSpan = document.createElement('span');
			wordExpSpan.innerText = wordExp;
			wordExpItem.appendChild(wordExpSpan);
			nodeExplanation.appendChild(wordExpItem);
		}
		console.log(nodeExplanation.innerHTML);
		html.appendChild(nodeExplanation);

		words[i].innerHTML = html.innerHTML;
		words[i].setAttribute('style', 'padding:2px 4px  2px 4px;');
	}
	return;
}