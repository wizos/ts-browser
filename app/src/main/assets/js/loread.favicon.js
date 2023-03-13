console.log("图标：引入文件");
//(function() {
	var iconEle = document.querySelector("[rel='shortcut icon']:not([href^='data:'])");
	if(!iconEle){
		iconEle = document.querySelector("[rel='icon']:not([href^='data:'])");
	}
	if(!iconEle){
		iconEle = document.querySelector("[rel*=icon]:not([href^='data:'])");
	}
	if(iconEle){
		var href = iconEle.getAttribute("href");
		if (href.indexOf('http') !== 0) {
            href = new URL(href, window.location.href).href;
        }
        console.log("已抓取到地址：" + href);
        LoreadBridge.onFetchIconHref(window.location.href, href);
	}else{
	    iconEle = document.querySelector("[rel='shortcut icon'][href]");
		if(!iconEle){
    		iconEle = document.querySelector("[rel='icon'][href]");
    	}
		if(!iconEle){
    		iconEle = document.querySelector("[rel*=icon][href]");
    	}
    	if(iconEle){
    	    console.log("已抓取到数据：" + iconEle.getAttribute("href"));
    	    LoreadBridge.onFetchIconData(window.location.href, iconEle.getAttribute("href"));
    	}else{
    	    console.log("未抓取到数据");
    	    LoreadBridge.onFetchIconFail(window.location.href);
    	}
	}
//})();