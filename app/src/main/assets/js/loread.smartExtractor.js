console.log('智能提取');

let xx = LoreadBridge.smartExtractor3(document.documentElement.outerHTML);

	function measureCssPath(cssPathListStr) {
        console.log(cssPathListStr);
        let arr = cssPathListStr.split(' || ');
        let greatPathScope = '';
        let greatPathChild = '';
        let greatArea = 0;
        for (var i = 0, l = arr.length; i < l; i++) {
            let path = arr[i].split(' >> ');
            console.log(path);
            let els = document.querySelectorAll(path[0]);
            let area = 0;
            if(els){
                els.forEach(el=>{
                    area = area + el.offsetHeight*el.offsetWidth;
                });
            }
            if(area > greatArea){
                greatPathScope = path[0];
                greatPathChild = path[1];
                greatArea = area;
            }
            console.log("测量选择器：" + greatPathScope + " => " + greatPathChild + ", 分数：" + greatArea);
        }
        if(greatArea > 0){
            if(greatPathChild){
                return greatPathScope + " > " + greatPathChild;
            }else{
                return greatPathScope;
            }
        }
	}

let greatPath = measureCssPath(xx);
console.log('最佳：' + greatPath);

