function publishDict(event, dict, data) {
    /*  stardict-xdict-ec-gb-2.4.2.js    */
    if (event == 'ready') {
        var DictDiv = data;
        var phoneticHandler = function(phonetic) {
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
        var words = getEleByClass('wordBox', 'div', DictDiv);
        for (var i = 0; i < words.length; i++) {
            var html = words[i].innerHTML;
            var arr = html.split('<br>');
            html = '';
            for (var j = 0; j < arr.length; j++) {
                var s = arr[j].trim();
                if (j == 0) {
                    if (s.length > 0) {
                        html = html + phoneticHandler(s);
                    }
                } else {
                    html = html + '<div>' + s + '</div>';
                }
            }
            words[i].innerHTML = html;
            words[i].setAttribute('style', 'padding:2px 4px  2px 4px;');
        }
    }
    return;

}