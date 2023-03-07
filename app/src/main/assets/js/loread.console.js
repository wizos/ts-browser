console.log('console 注入点');

LoreadBridge.evalJsFile("js/vconsole.min.js", true);
LoreadBridge.evalJs("var vConsole = new window.VConsole();", true);


