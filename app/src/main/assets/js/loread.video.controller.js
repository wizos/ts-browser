console.log('媒体控制器：引入文件');
(function ($) {
    // 保存 视频元素 与其 控制器 的关系
    var elMap = new Map();

    // 表示是否加载依赖文件（仅在首次发现视频时加载）
    var loader = false;

    console.log('监听动态增减 video');
    const mediaObserver = new MutationObserver(function (records) {
        records.forEach((record) => {
            if (record.type === 'childList') {
                if (record.addedNodes.length !== 0) {
                    record.addedNodes.forEach(node => {
                        if (node.nodeName === 'VIDEO') {
                            foundVideo(node);
                        }
                    })
                } else if (record.removedNodes.length !== 0) {
                    record.removedNodes.forEach(node => {
                        if (node.nodeName === 'VIDEO') {
                            removeController(node);
                        }
                    })
                }
            } else if (record.type === 'attributes') {
                if (record.target.nodeName === 'VIDEO') {
                    if (record.oldValue) {
                        removeController2(record.oldValue);
                    }
                    foundVideo(record.target);
                }
            }
            resetPosition();
        })
    });

    mediaObserver.observe(document.body, {
        attributes: true,
        attributeFilter: ["src"],
        attributeOldValue: true,
        childList: true,
        subtree: true
    });

    // 创建实例
    const intersectionObserver = new IntersectionObserver(changes => {
        changes.forEach(change => {
            let controllerEl = elMap.get(change.target);
            if (change.intersectionRatio === 0) {
                // controllerEl.style.visibility = "hidden";
                // console.log("滚动是否可见：隐藏");
            } else {
                // controllerEl.style.visibility = "visible";
                // console.log("滚动是否可见：可见");
                setPosition(change.target, controllerEl);
            }
        });
    });

    document.body.addEventListener("touchstart", (e) => {
        //console.log("触摸开始");
        seek();
    })

    seek();


    function seek() {
        //console.log('查找多媒体');
        var videos = document.getElementsByTagName("video");

        let videoSet = new Set();
        for (var i = 0; i < videos.length; i++) {
            let controllerEl = elMap.get(videos[i]);
            videoSet.add(videos[i]);
            let hidden = isHidden(videos[i]);
            if (controllerEl) {
                // 更新定位，控制器
                setPosition(videos[i], controllerEl);
            } else if (!hidden) {
                // 生成控制器
                foundVideo(videos[i]);
            }
        }

        elMap.forEach(function (controllerEl, videoEl) {
            if (!videoSet.has(videoEl)) {
                // 剔除
                removeController(videoEl);
                elMap.delete(videoEl);
            }
        })
    }

    function resetPosition() {
        elMap.forEach(function (value, key) {
            setPosition(key, value);
        })
    }

    function foundVideo(el) {
        //console.log('发现视频');
        //console.log(el.outerHTML);
        var src = el.src;
        if (!src) {
            let sourceEl = el.querySelector('source');
            if (sourceEl) {
                src = sourceEl.src;
            }
        }
        if (!src) {
            return;
        }
        el.setAttribute('loread__src', src);

        intersectionObserver.observe(el);

        generateController(el);

        el.onplaying = function () {
            const id = "controls__" + hashCode(src);
            var controllerEl = document.querySelector("#" + id);
            if (!controllerEl) return;
            if (el.videoHeight == 0 || el.videoWidth == 0) {
                removeController(el);
                return;
            }
            setSpeed(el);
            setPosition(el, controllerEl);
        }

        if (window.location.href.indexOf('player.youku.com/embed') != -1) {
            var meta = document.createElement('meta');
            meta.setAttribute('name', 'viewport');
            meta.setAttribute('content', 'width=device-width, initial-scale=1.0, user-scalable=no');
            document.head.appendChild(meta);
            $(".ykplayer").css("position", "inherit");
            $("#youku-playerBox").attr("style", "");
            $(document.body).css("background", "black");
        } else if (window.location.href.indexOf('tv.sohu.com/s/sohuplayer/iplay.html') != -1) {
            var meta = document.createElement('meta');
            meta.setAttribute('name', 'viewport');
            meta.setAttribute('content', 'width=device-width, initial-scale=1.0, user-scalable=no');
            document.head.appendChild(meta);
            $("#sohuplayer div.x-download-panel").remove();
        } else if (window.location.href.indexOf('m.bilibili.com/video') != -1) {
            $(".m-float-openapp, .m-video2-main-img, .mplayer-control-dot, .mplayer-widescreen-callapp, .mplayer-comment-text, .mplayer-control-btn-quality, .mplayer-control-btn-speed").remove();
        } else if (window.location.href.indexOf('m.iqiyi.com/v_') != -1) {
            $(".m-iqylink-guide").remove();
        } else if (window.location.href.indexOf('video.zhihu.com/video/') != -1) {
            $("video").css("height", "auto").css("max-height", "100%").css("max-width", "100%");
        }
    }


    function generateController(el) {
        let src = el.getAttribute('loread__src');
        var id = "controls__" + hashCode(src);
        if (document.querySelector("#" + id)) return;
        var controllerEl = document.createElement("div");
        controllerEl.id = id;
        controllerEl.classList.add("loread__controls");
        let controlHtml = `
        <div class="loread__controls__item loread__menu">
            <button loread-data="settings" type="button" class="loread__control">
                <svg class="loread__icon" focusable="false">
                    <use xlink:href="#plyr-settings"></use>
                </svg>
            </button>
            <div class="loread__menu__container" role="menu" hidden>
                <button loread-data="speed" type="button" role="menuitemradio" class="loread__control" value="0.75"><span>0.75×</span>
                </button>
                <button loread-data="speed" type="button" role="menuitemradio" class="loread__control" value="1"><span>1x</span>
                </button>
                <button loread-data="speed" type="button" role="menuitemradio" class="loread__control" value="1.5"><span>1.5×</span>
                </button>
                <button loread-data="speed" type="button" role="menuitemradio" class="loread__control" value="1.75"><span>1.75×</span>
                </button>
                <button loread-data="speed" type="button" role="menuitemradio" class="loread__control" value="2"><span>2×</span>
                </button>
            </div>
        </div>`;

        if (src.indexOf("blob:") !== 0) {
            controlHtml += `
        <button class="loread__controls__item loread__control" type="button" loread-data="download">
            <svg class="loread__icon" focusable="false">
                <use xlink:href="#plyr-download"></use>
            </svg>
        </button>`;
        }

        controlHtml += `
        <button class="loread__controls__item loread__control" type="button" loread-data="fullscreen">
            <svg class="loread__icon icon--pressed" focusable="false">
                <use xlink:href="#plyr-exit-fullscreen"></use>
            </svg>
            <svg class="loread__icon icon--not-pressed" focusable="false">
                <use xlink:href="#plyr-enter-fullscreen"></use>
            </svg>
        </button>`;

        controllerEl.innerHTML = controlHtml;
        document.body.appendChild(controllerEl);
        elMap.set(el, controllerEl);

        setSpeed(el);
        setPosition(el, controllerEl);


        $("#" + id + " > [loread-data=download]").click(function (event) {
            console.log("下载：" + el.getAttribute('loread__src'));
            LoreadBridge.downFile(el.getAttribute('loread__src'));
        });

        $("#" + id + " > [loread-data=fullscreen]").click(function (event) {
            LoreadBridge.postVideoPortrait(el.videoHeight > el.videoWidth);
            //console.log("是否竖屏：" + (el.videoHeight > el.videoWidth));
            if ($(this).hasClass("loread__control--pressed")) {
                $(this).removeClass("loread__control--pressed");
                if (document.exitFullscreen) {
                    document.exitFullscreen();
                }
                console.log("退出全屏");
            } else {
                let videoView = getEl(el);
                $(this).addClass("loread__control--pressed");
                if (videoView.requestFullscreen) {
                    videoView.requestFullscreen();
                }
                console.log("请求全屏");
            }
        });
        $("#" + id + " [loread-data=settings]").click(function (e) {
            var menu = $("#" + id + " .loread__menu__container");
            var hidden = menu.prop("hidden");
            console.log("倍速菜单：" + (hidden ? "显示" : "隐藏"));
            menu.prop("hidden", !hidden);
        });

        $("#" + id + " [loread-data=speed]").click(function (e) {
            var value = $(this).attr("value");
            el.playbackRate = value;
            var menu = $("#" + id + " .loread__menu__container");
            menu.prop("hidden", true);
            window.localStorage.setItem('speed', value);
            console.log("设置倍速：" + value);
        });
    }

    function removeController(el) {
        const id = "controls__" + hashCode(el.getAttribute('loread__src'));
        var controllerEl = document.querySelector("#" + id);
        if (!controllerEl) return;
        controllerEl.parentNode.removeChild(controllerEl);
    }

    function removeController2(src) {
        const id = "controls__" + hashCode(src);
        var controllerEl = document.querySelector("#" + id);
        if (!controllerEl) return;
        controllerEl.parentNode.removeChild(controllerEl);
    }

    function setSpeed(videoEl) {
        const speed = window.localStorage.getItem('speed');
        if (speed) {
            videoEl.playbackRate = speed;
            console.log("初始倍速：" + speed);
        }
    }

    function setPosition(videoEl, controllerEl) {
        if (!videoEl || !controllerEl) {
            return;
        }

        let videoPos = videoEl.getBoundingClientRect();
        let videoTop = videoPos.top + getScrollTop(videoEl);

        let lastVideoTop = $(controllerEl).attr("y");
        let lastVideoLeft = $(controllerEl).attr("x");

    	if (lastVideoTop && lastVideoLeft && Math.abs(lastVideoTop - videoTop) < 5 && Math.abs(lastVideoLeft - videoPos.left) < 5) {
            return;
        }

        $(controllerEl).attr("y", videoTop);
        $(controllerEl).attr("x", videoPos.left);

        let videoHeight = videoEl.offsetHeight;
        if (videoHeight < controllerEl.offsetHeight) {
            videoHeight = document.body.clientHeight;
        }
        let videoWidth = videoEl.offsetWidth;
        if (videoWidth < controllerEl.offsetWidth) {
            videoWidth = document.body.clientWidth;
        }

        let controllerTop = videoTop + (videoHeight - controllerEl.offsetHeight) / 2;
        let controllerleft = Math.max(videoPos.left, 0) + videoWidth - controllerEl.offsetWidth;
        $(controllerEl).css({top: controllerTop, left: controllerleft});
    }

    function isHidden(videoEl) {
        var style = window.getComputedStyle(videoEl);
        return style.display === 'none' || style.visibility === 'hidden' || videoEl.offsetHeight < 20 || videoEl.offsetWidth < 20 || videoEl.offsetParent === null
    }

    function getScrollTop(videoEl) {
        if (!videoEl.window) {
            videoEl.window = $(window);
        }
        return videoEl.window.scrollTop();
    }

    function getEl(el) {
        if (el.offsetHeight < document.body.offsetHeight - 100) {
            return el;
        }
        return document.body;
    }

    function hashCode(str) {
        if (!str) {
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
})(Zepto);
