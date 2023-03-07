console.log("引入公共库");
var loread = (function() {
	// 产生数字hash值，规则和java的hashcode相同。返回字符串格式，因为通过$(this).attr('id')获取到的是字符串格式
    function hashCode(str) {
	    if(!str){
		    return;
	    }
        var h = 0;
        var len = str.length;
        for (var i = 0; i < len; i++) {
            h = 31 * h + str.charCodeAt(i);
            if (h > 0x7fffffff || h < 0x80000000) {
                h = h & 0xffffffff;
            }
        }
        return (h).toString();
    }

	function replaceNode(newNode, oldNode) {
	    oldNode.parentNode.insertBefore(newNode, oldNode);
	    newNode.parentNode.removeChild(oldNode);
	}

	function parseDom(str) {
	    var objE = document.createElement("div");
	    objE.innerHTML = str;
	    return objE.childNodes[0];
	}
	function bridge() {
		return (typeof LoreadBridge !== "undefined");
	}
	function log(msg) {
		bridge()?LoreadBridge.log(msg): console.log(msg);
	}
	return {
		hashCode: hashCode,
		bridge: bridge,
		log: log
	};
})();