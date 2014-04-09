
function publishDict(DictDiv) {
/*  youdao_online.js    */    
 	var onQueryFinish = function(status, jsonpData, container){
		if( status != 200 ){
			container.innerText = "查询失败！";
			return;
		}
		var dict = jsonpData;
		if( dict.errorCode != 0 ){
			container.innerText = "查询失败！错误号："+dict.errorCode;
			return;
		}
		var html = '<div class="keyWord" onclick="toggleWord(this)">'+dict.query+'</div>';
		
		var wordBox = document.createElement('div')
		wordBox.setAttribute('class', 'wordBox');
		//build basic explain
		if(dict.basic != undefined ){
			var basic = document.createElement('div');
			var title = document.createElement('div');
			title.setAttribute('style', 'color:rgb(22, 101, 190);font-weight:900;margin:8px 0px 3px 0px;border-bottom:3px solid #699;');
			title.innerText = '基本词义';

			var PhoneticAndExplains = document.createElement('div');
			if( dict.basic.phonetic != undefined ){
				var phoneticBox = document.createElement('div');
				phoneticBox.setAttribute('style', 'margin:1px;font-weight:900;background-color:#dde;height:24px;line-height:24px;');
				phoneticBox.innerHTML = phoneticHandler(dict.basic.phonetic);
				PhoneticAndExplains.appendChild(phoneticBox);
			}
			var explainsDiv = document.createElement('div');
			for(i in dict.basic.explains ){
				explain = dict.basic.explains[i];
				var explainDiv = document.createElement('div');
				explainDiv.setAttribute('style', 'text-indent: 1em; font-size=16px; background:url(img/dot_brown.png) no-repeat; background-position:0.5em 6px;');
				explainDiv.innerText = explain;
				explainsDiv.appendChild(explainDiv);
			}
			PhoneticAndExplains.appendChild(explainsDiv);
			basic.appendChild(title);
			basic.appendChild(PhoneticAndExplains);
			wordBox.appendChild(basic);
		}
		//build net explain
		if( dict.web != undefined && dict.web.length > 0 ){
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
    		wordBox.appendChild(netDiv);
	    }
		container.innerHTML = html;
		container.appendChild(wordBox);
	}
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
    
	if(DictDiv.toString() == '[object HTMLDivElement]'){
        var wordDiv = DictDiv.getElementsByTagName('word')[0];
    	var word = wordDiv.innerHTML;
    	var contentsDiv = wordDiv.parentElement;
    	
    	queryOnlineDict('http://fanyi.youdao.com/openapi.do?keyfrom=N3verL4nd&key=208118276&type=data&doctype=json&version=1.1&q='+encodeURIComponent(word), getJsonpCallback(DictDiv));    	
	}
	else{
		var div = arguments[1];
		var wordDiv = div.getElementsByTagName('word')[0];
    	var contentsDiv = wordDiv.parentElement;
    	
		onQueryFinish(DictDiv.status.http_code, DictDiv.contents, contentsDiv);
	}
	return;
}