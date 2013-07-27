function publishDict(DictDiv) {
/*  qq_online.js    */ 
 	var onQueryFinish = function(status, responseText, container){
		if( status != 200 ){
			container.innerText = "查询失败！";
			return;
		}
		var dict = JSON.parse(responseText);
		if( dict.err != undefined ){
			container.innerText = dict.err;
			return;
		}
		if( dict.local == undefined ){
			container.innerText = "无此单词！";
			return;
		}
		if( dict.local.length == 0 ){
			container.innerText = "查询失败！";;
			return;
		}
		var html = '<div class="keyWord" onclick="toggleWord(this)">'+dict.local[0].word+'</div>';
		
		//build basic explain
		var basic = document.createElement('div');
		var title = document.createElement('div');
		title.setAttribute('style', 'color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
		title.innerText = '基本词义';
		basic.appendChild(title);
		var PhoneticAndExplains = document.createElement('div');
		for(localWordId in dict.local){
			var localWord = dict.local[localWordId];
			if( localWord.pho != undefined ){
				var phoneticBox = document.createElement('div');
				phoneticBox.setAttribute('style', 'margin:1px;font-weight:900;background-color:#dde;height:24px;line-height:24px;');
				phoneticBox.innerHTML = phoneticHandler(localWord.pho[0]);
				PhoneticAndExplains.appendChild(phoneticBox);
			}
			if( localWord.mor != undefined ){
				var moreBox = document.createElement('div');
				for(moreId in localWord.mor){
					var mor = localWord.mor[moreId];
					if( moreBox.childElementCount > 1 ){
						moreBox.appendChild(document.createTextNode(" "));
					}
					var morSpan = document.createElement('span');
					//morSpan.setAttribute('style', 'font-weight:700; display:inline-block; color:#630;');
					morSpan.innerText = mor.c;
					moreBox.appendChild(morSpan);
					moreBox.appendChild(document.createTextNode(":"));
					morSpan = document.createElement('span');
					morSpan.setAttribute('style', 'margin:1px;font-weight:900;height:24px;line-height:24px;');
					morSpan.innerText = mor.m;
					moreBox.appendChild(morSpan);
				}
				PhoneticAndExplains.appendChild(moreBox);
			}
			var explainsDiv = document.createElement('div');
			for(i in localWord.des ){
				var wordDes = localWord.des[i];
				var explainDiv = document.createElement('div');
				explainDiv.setAttribute('style', 'text-indent: 1em; font-size=16px; background:url(img/dot_brown.png) no-repeat; background-position:0.5em 6px;');
				if( wordDes.p == undefined ){
					explainDiv.innerText = wordDes.d;
				}
				else{
					explainDiv.innerText = wordDes.p + " " + wordDes.d;
				}
				explainsDiv.appendChild(explainDiv);
			}
			PhoneticAndExplains.appendChild(explainsDiv);
			basic.appendChild(PhoneticAndExplains);
		}
		//build net explain
/*		
		var netDiv = document.createElement('div');
		title = document.createElement('div');
		title.setAttribute('style', 'color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
		title.innerText = '网络释义';
		netDiv.appendChild(title);
		explainsDiv = document.createElement('div');
		for(i in dict.web ){
			webWord = dict.web[i];
			var webDiv = document.createElement('div');
			var wordText = document.createElement('span');
			wordText.setAttribute('style', 'font-weight:700; display:inline-block; color:#630;');
			wordText.innerText = webWord.key;
			webDiv.appendChild(wordText);
			for(index in webWord.value ){
				var explainDiv = document.createElement('span');
				explainDiv.setAttribute('style', 'padding:3px;');
				explainDiv.innerText = webWord.value[index];
				if( webDiv.childElementCount > 1 ){
					webDiv.appendChild(document.createTextNode("; "));
				}
				webDiv.appendChild(explainDiv);
			}
			explainsDiv.appendChild(webDiv);
		}
		netDiv.appendChild(explainsDiv);
*/		
		var wordBox = document.createElement('div')
		wordBox.setAttribute('class', 'wordBox');
		wordBox.appendChild(basic);
		//wordBox.appendChild(netDiv);
		container.innerHTML = html;
		container.appendChild(wordBox);
	}
	var wordDiv = DictDiv.getElementsByTagName('word')[0];
	var word = wordDiv.innerHTML;
	var contentsDiv = wordDiv.parentElement;
	//console.log(fixPhonetic('fixPhonetic'));
	queryOnlineDict('http://dict.qq.com/dict?q='+encodeURIComponent(word), onQueryFinish, contentsDiv);
	var phoneticHandler = function(phonetic){
        var arr = phonetic.split('; ');
        var html = '';
        for(var i=0; i<arr.length; i++){
            var s = arr[i].trim();
            if( s.length > 0 ){
                if( i==0 ){
                    html = '[<span style="color:green;">'+s+'</span>]';
                }
                else{
                    html = html +  ';&nbsp;[<span style="color:green;">'+s+'</span>]';
                }
            }
        }
        return html;
    }
	return;
}