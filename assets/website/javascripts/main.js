function loadContents(resIdStr) {
    if (window.adict == undefined) {
        window.adict = new Object();
        window.adict.getAjax = function(url) {
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.open("GET", "mock/" + url, false);
            xmlhttp.send();
            return xmlhttp.responseText;
        }
        window.adict.loadResourceString = function(resIdStr) {
            var text = window.adict.getAjax("dictionaries.js");
            return text;
        }
    }

    var text = window.adict.loadResourceString(resIdStr);
    var htmlNode = document.createElement("div");
    htmlNode.innerHTML = text;
    document.body.appendChild(htmlNode);

}
