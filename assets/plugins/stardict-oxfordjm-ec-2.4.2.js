function publishDict(event, dict, data) {
    /* stardict-oxfordjm-ec-2.4.2.js */
    if (event == 'ready') {
        var DictDiv = data;
        var phoneticHandler = function(phonetic) {
            phonetic = phonetic.replace(/\[([^\]]+)\]/, '$1');
            var arr = phonetic.split(',');
            var html = '';
            for (var i = 0; i < arr.length; i++) {
                var s = arr[i].trim();
                if (s.length > 0) {
                    if (i == 0) {
                        html = '[<span style="color:green;">' + s + '</span>]</div>';
                    } else {
                        html = html + ',&nbsp;[<span style="color:green;">' + s + '</span>]</div>';
                    }
                }
            }
            return html;
        }

        var testPhonetic = function(txt) {
            var m = txt.match(/^\s*[a-z]+.\s*$/);
            if (m != null) {
                return false;
            }
            ///^[\u4e00-\u9fa5]+$/.test(str)
            m = txt.match(/^[\u0020-\u2009]+$/);
            if (m != null) {
                return true;
            }
            return false;
        }

        var words = getEleByClass('wordBox', 'div', DictDiv);
        for (var i = 0; i < words.length; i++) {
            var html = words[i].innerText;
            var arr = html.split('\n');
            var node = document.createElement('div');
            var nodeTyp = null;
            var nodeExp = null;
            var nodeItem = null;
            for (var j = 0; j < arr.length; j++) {
                var s = arr[j].trim();
                if (testPhonetic(s)) {
                    if (s.length > 0) {
                        var phonetic = document.createElement('div');
                        phonetic.innerHTML = phoneticHandler(s);
                        node.appendChild(phonetic);
                    }
                } else {
                    var m = s.match(/^[a-z]+\./);
                    if (m != null) {
                        if (nodeTyp != null) {
                            nodeItem.appendChild(document.createTextNode(' '));
                        } else {
                            nodeItem = document.createElement('div');
                            node.appendChild(nodeItem);
                        }
                        nodeTyp = document.createElement('span');
                        nodeTyp.innerText = s;
                        nodeItem.appendChild(nodeTyp);
                    } else {
                        nodeExp = document.createElement('span');
                        nodeExp.innerText = s;

                        if (nodeItem == null) {
                            nodeItem = document.createElement('div');
                            node.appendChild(nodeItem);
                        }
                        nodeItem.appendChild(nodeExp);
                        nodeTyp = null;
                    }
                }
            }
            words[i].innerHTML = node.innerHTML;
            words[i].setAttribute('style', 'padding:2px 4px  2px 4px;');
        }
    }
    return;
}