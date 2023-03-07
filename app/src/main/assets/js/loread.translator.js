console.log("翻译器：引入文件");
(function() {
    const noTranslateCssPaths = ['pre','#__vconsole','.prettyprint','.mjx-chtml','.MJXc-display','.MathJax_Display','.math-container','.MathJax','.katex--display','.syntaxhighlighter','.code_cell','.highlight'];

    for(var i = 0; i < noTranslateCssPaths.length; i++) {
        addNoTranslate(noTranslateCssPaths[i]);
    }

	console.log("翻译器：执行脚本");
	var controller = document.createElement('div');
	controller.setAttribute('id', 'google_translate_element');
	controller.setAttribute('style', 'position:fixed;bottom:110px;right:10px;z-index:2000;opacity:0.8');
	document.body.appendChild(controller);

	var controllerInit = document.createElement('script');
	controllerInit.innerText = 'function googleTranslateElementInit(){new google.translate.TranslateElement({layout:/mobile/i.test(navigator.userAgent)?0:2,includedLanguages:"zh-CN,zh-TW,en,ja,ru",layout:google.translate.TranslateElement.InlineLayout.SIMPLE}, "google_translate_element")}';
	document.body.appendChild(controllerInit);

	var translateEl = document.createElement('script');
	translateEl.setAttribute('src', 'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit');
	document.body.appendChild(translateEl);


    function addNoTranslate (selector) {
        var els = document.querySelectorAll(selector);
        for(var i = 0; i < els.length; i++) {
            els[i].classList.add("notranslate");
        }
    }
})();