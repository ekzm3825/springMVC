// 아작스 설정 전 qusi 네임스페이스가 존재하는지 확인.
if (qusi === undefined || qusi === null) {
    throw "qusi doesn't exist! please check qusi.js file is exist."
}

/**
 * QUSI - Ajax
 *
 * @require qusi-0.1.0
 * @version 0.1.0
 * @author yunchan
 * @date 2016. 2. 17
 */
(function (self) {

    /**
     * qusi ajax 스크립트 정보
     * @type {{version: string, option:{preventDoubleClick(boolean)}}}
     */
    self.static.ajax = {
        version: '0.1.0',

        /**
         * 아작스 이벤트 종료를 알리는 이벤트 명칭 (성공)
         * @type {string}
         */
        ajaxSuccess    : 'qusiAjaxSuccess',
        /**
         * 아작스 이벤트 종료를 알리는 이벤트 명칭 (실패)
         * @type {string}
         */
        ajaxFail       : 'qusiAjaxFail',
        format         : '_format',
        method         : '_method',
        option         : {
            preventDoubleClick                 : true,
            preventDuplicatedTime              : 500,
            preventDuplicatedNotifyIntervalTime: 500,
            allowDuplicatedRequest             : true
        },
        defaultCallBack: {}
    };


    /**
     * Ajax
     * namespace of Ajax module
     * @type {{loadingBar: (*|jQuery|HTMLElement), ajax: (Function), get: (Function), put: (Function), remove : (Function), formAjax: (Function), syncFormUpload:(Function), asyncFormUpload : (Function), makeTopLoading : (Function), getQue : (Function), post : (Function)}}
     */
    var Ajax = {};

    Ajax.loadingBar = $('<div class="progress" style="z-index: 999999; position: fixed;top: 0;left: 0; width: 0; height: 4px;background-color: #76a2ff;opacity:0;"></div>');

    /**
     * QUE 리스트
     * @type {Array}
     */
    var ajaxRequestQue            = [];
    var ajaxRequestQueFirstLength = 0;
    /**
     * QUE 가 모두 완료됬는지 체크
     * @type {boolean}
     */
    var isDone                    = true;
    var isSuccess                 = true;
    var failedDataList            = [];
    var isCanQueAjax              = true;

    var extendDeferredCallbackForQusi = function (deferredObject, _reqOption) {

        var returnAjaxObject = $.extend(deferredObject, {

            success       : function (func) {
                return deferredObject.done(func);
            },
            error         : function (func) {
                return deferredObject.fail(func);
            },
            onStatusCode  : function (jqXHR, textStatus, errorThrown, errorData) {
                var errorCallbackContainer = this.errorCallbacks[jqXHR.status.toString()];

                if (errorCallbackContainer) {
                    for (var i = 0; i < errorCallbackContainer.length; i++) {
                        var func = errorCallbackContainer[i];

                        func(jqXHR, textStatus, errorThrown, errorData);
                    }

                    errorCallbackContainer.length = 0; // 에러 콜백을 한 후 어레이 비우기.
                }

                return this;
            },
            /**
             * 권한 실패 callback
             * @param func
             * @returns {returnAjaxObject}
             */
            onStatus      : function (code, func) {
                (this.errorCallbacks[code.toString()] || (this.errorCallbacks[code.toString()] = [])).push(func);
                return this;
            },
            /**
             * 에러 콜백들
             */
            errorCallbacks: {}
        });

        returnAjaxObject.fail(function (jqXHR, textStatus, errorThrown) {
            var errorData = {};

            if (jqXHR) { // jquery xhr 이 존재 할 경우만. 그 외 경우는 errorThrown 으로 온다.
                if (jqXHR.responseText != null)
                    try {
                        errorData = JSON.parse(jqXHR.responseText);
                    } catch (e) {
                    }

                var callback = qusi.static.ajax.defaultCallBack[jqXHR.status];

                // 기본 콜백을 사용한다.
                var preventDefault = _reqOption == null ? false :
                                     _reqOption['prevent' + jqXHR.status.toString()] != undefined ?
                                     _reqOption['prevent' + jqXHR.status.toString()] : false;
                if (preventDefault) {
                    if (_reqOption['onStatus'])
                        if (_reqOption['onStatus'][jqXHR.status])
                            _reqOption['onStatus'][jqXHR.status](jqXHR, textStatus, errorThrown, errorData);

                    return returnAjaxObject.onStatusCode(jqXHR, textStatus, errorThrown, errorData);
                }

                if (callback !== null && callback !== undefined)
                    callback(jqXHR, textStatus, errorThrown, errorData);

            }

        });

        return returnAjaxObject;
    };

    /**
     * spring 프로젝트에 맞는 format 방식으로 변환합니다
     * @param reqOption
     * @returns {*}
     */
    var requestOptionAdoptKeyname = function (reqOption) {

        if (typeof reqOption !== 'undefined') {
            if (typeof reqOption.format !== 'undefined') {
                reqOption[qusi.static.ajax.format] = reqOption.format;
                reqOption.format                  = undefined;
            }
            if (typeof reqOption.method !== 'undefined') {
                reqOption[qusi.static.ajax.method] = reqOption.method;
                reqOption.method                  = undefined;
            }
        } else {
            reqOption = {};
        }

        return reqOption;
    };

    /**
     * 아작스 요청 que push
     * @returns {boolean}
     */
    var pushQue = function (url, option, reqOption, pushStateOption, deferred) {

        var request = new RequestObject(url, option, reqOption, pushStateOption, deferred);

        /* 중복 요청을 막는다. 만약 'reqOption.allowDuplicatedRequest' 값이 true 라면 아래의 로직을 무시하고 모두 'ajax que'에 등록되며 모든 아작스가 시작된다.
         아작스 중복 요청을 막을 수 있는 조건은 총 3가지 :
         1. reqOption.allowDuplicatedRequest = true
         2. 아작스큐 (ajaxRequestQue) 에 동일한 요청이 있을 경우
         3. isCanQueAjax = true (중복 아작스 요청이 셋 타임아웃이 끝나지 않아 false 인 경우)
         */
        if ((request.getReqOption().allowDuplicatedRequest === false) && (
                $.grep(ajaxRequestQue, function (item) {
                    return item.getUrl() === request.getUrl() &&
                        qusi.util.isEqualValue(item.getOption(), request.getOption()) &&
                        qusi.util.isEqualValue(item.getReqOption(), request.getReqOption()) &&
                        qusi.util.isEqualValue(item.getPushStateOption(), request.getPushStateOption());
                }).length > 0 || !isCanQueAjax
            )) {

            // 중복 요청 시도시에 실패를 알린다. ajax 요청이 이루어 지지 않았기 때문에 reject의 인자값은 마지막 Exception
            // 에만 전달된다.
            deferred.reject(null, null, {msg: '중복 요청 불가.'});

            return deferred;
        }

        // 아작스 리퀘스트를 큐에 넣는다.
        ajaxRequestQue.push(request);
        ajaxRequestQueFirstLength += 1;

        if (!request.getReqOption().allowDuplicatedRequest && request.getReqOption().allowDuplicatedRequest === false) // 중복 요청이 불가능 하다면.
        {
            var preventTime               = reqOption.preventDuplicatedTime || qusi.static.ajax.option.preventDuplicatedTime;
            var preventNotifyIntervalTime = reqOption.preventDuplicatedNotifyIntervalTime || qusi.static.ajax.option.preventDuplicatedNotifyIntervalTime;
            isCanQueAjax                  = false; // 아작스 리퀘스트를 큐에 넣었다면, 큐 가능 여부를 알리는 isCanQueAjax를 false로 변경한다.

            // 경과시간.
            var timePassed = 0;

            // interval
            var interval = setInterval(function () {
                timePassed += preventNotifyIntervalTime;
                request.getDeferred().notifyPreventing(timePassed, (timePassed / preventTime) * 100);
            }, preventNotifyIntervalTime)

            // time out
            setTimeout(function () {
                isCanQueAjax = true; // 시간이 지나면 다시 아작스 요청을 할 수 있게 만든다.
                clearInterval(interval); // interval 클리어
                timePassed = 0; // 경과 시간 초기화.
                request.getDeferred().notifyEndPreventing(); // 완료 notify.
            }, preventTime);

        }


        // 큐 등록 및 큐의 길이 프로퍼티 설정이 마무리 되었다면
        // 큐를 resolve 하기 시작한다.
        resolveQue();

        // 큐의 아이템이 리졸브 될 때 deferred 오브젝트를 선언 부로 반환한다.
        return deferred;
    };

    /**
     * 아작스 요청 오브젝트(RequestObject)의 큐를 해결해 나간다
     * 이 메서드는 'doAjax' 메서드가 종료 된 후 다시 재 호출된다.
     * (재귀)
     */
    var resolveQue = function () {
        if (ajaxRequestQue.length > 0 && isDone) {
            var req = ajaxRequestQue[0];
            doAjax(req);
        }
    };

    /**
     * 아작스 요청을 실시한다.
     * @param req
     * @returns {*}
     */
    var doAjax = function (req) {

        isDone = false;

        var reqParam                     = {};
        reqParam[qusi.static.ajax.method] = req.getReqOption()[qusi.static.ajax.method] || 'get';
        reqParam[qusi.static.ajax.format] = req.getReqOption()[qusi.static.ajax.format] || 'json';

        var urlParam   = $.param($.extend(reqParam, qusi.util.urlParam(req.getUrl())));
        var splitedUrl = (req.getUrl() && req.getUrl().indexOf('?') > 0) ? req.getUrl().split('?')[0] : req.getUrl();
        var url        = splitedUrl + (urlParam ? '?' + urlParam : '');


        $.ajax(url, $.extend({
             method: reqParam[qusi.static.ajax.method] === 'get' ? 'get' : 'post'
         }, req.getOption()))
         .success(function (data, textStatus, jqXHR) { //아작스 요청 성공시 pushState 시도


             // 스트링 값이 json 일 경우 json 으로 파싱 (IE같이 type별로 제대로 오지 않을 경우를 위해)
             if (typeof data === 'string' && reqParam[qusi.static.ajax.format] === 'json') {
                 // 요청값이 jsoN 임에도 불구하고 json이 아닐 경우를 대비하여 try 한다.
                 try {
                     // string json을 자바스크립트 오브젝트로 파싱한다.
                     data = JSON.parse(data);
                 } catch (e) {
                 }
             }

             req.getDeferred().resolve(data, textStatus, jqXHR);
             //pushStateOption 이 있을 때 pushState
             if (req.getPushStateOption())
                 History.pushState(
                     req.getPushStateOption()['data'] || data,
                     req.getPushStateOption()['title'],
                     req.getPushStateOption()['path']
                 );

             // 성공, 마무리 한다
             whenDone(
                 true,
                 (typeof jqXHR.responseJSON !== 'undefined' ? jqXHR.responseJSON : jqXHR.responseText),
                 jqXHR,
                 textStatus
             );

         })
         .error(function (jqXHR, textStatus, errorThrown) {

             var data;

             try {
                 data = JSON.parse(jqXHR.responseText);
             } catch (e) {
                 console.log('결과를 json으로 변환하는데 실패하였습니다.');
             }

             req.getDeferred().reject(jqXHR, textStatus, errorThrown, data);

             // 실패! 다음을 위해 마무리 한다
             whenDone(
                 false,
                 (typeof jqXHR.responseJSON !== 'undefined' ? jqXHR.responseJSON : jqXHR.responseText),
                 jqXHR,
                 textStatus,
                 errorThrown
             );

         })
         .always(function () {

             isDone = true;
             resolveQue();
         });

        return Ajax.makeTopLoading(req.getDeferred());
    };


    // 아작스 요청이 하나씩 마무리 될 때마다 실행 된다
    // 성공, 실패 모두 실행된다.
    // 성공시에는 true, 실패시에는 false 를 보낸다.
    var whenDone = function (_isSuccess, data, jqXHR, textStatus, errorThrown) {

        ajaxRequestQue.shift();
        isSuccess = _isSuccess;

        if (!isSuccess)
            failedDataList.push({data: data, status: textStatus, jqXHR: jqXHR, errorThrown: errorThrown});

        // 아작스 요청 큐가 더이상 없을 경우에는 완료 이벤트를 발동한다.
        if (ajaxRequestQue.length <= 0)
            if (isSuccess)
                $(window).trigger(qusi.static.ajax.ajaxSuccess, {
                    jqXHR         : jqXHR,
                    textStatus    : textStatus,
                    failedDataList: failedDataList,
                    errorThrown   : errorThrown
                });
            else
                $(window).trigger(qusi.static.ajax.ajaxFail, {
                    jqXHR         : jqXHR,
                    textStatus    : textStatus,
                    failedDataList: failedDataList,
                    errorThrown   : errorThrown
                });

    };


    /**
     * Request Object
     * 리퀘스트 오브젝트 선언
     * @type {RequestObject}
     * @param url {String}
     * @param option {{}}
     * @param reqOption {{}}
     * @param pushStateOption {{}}
     * @param deferred {Deferred}
     */
    var RequestObject = function (url, option, reqOption, pushStateOption, deferred) {
        var _url             = url;
        var _option          = option;
        var _reqOption       = requestOptionAdoptKeyname(reqOption);
        var _pushStateOption = pushStateOption;
        var _deferred        = $.extend(deferred, {
            preventingManifest           : [], // 중복 요청 방지 중 알림 구독중인 함수들
            preventingEndManifest        : [], // 중복 요청 방지 완료 알림 구독중인 함수들
            notifyPreventing             : function () { // 중복을 방지중임을 알림.
                for (var i = 0; i < this.preventingManifest.length; i++) {
                    // 중복 요청 방지 중임을 알리는 이벤트에 구독중인 함수들에게 알린다.
                    this.preventingManifest[i].apply(this, arguments);
                }
            },
            notifyEndPreventing          : function () {
                for (var i = 0; i < this.preventingEndManifest.length; i++) {
                    // 중복 요청 방지 중임을 알리는 이벤트에 구독중인 함수들에게 알린다.
                    this.preventingEndManifest[i].apply(this, arguments);
                }
            },
            onDuplicatedRequestPreventing: function (func) {
                this.preventingManifest.push(func);
                return this; // jquery deferred 반환.
            },
            onEndDuplicatedPreventing    : function (func) {
                this.preventingEndManifest.push(func);
                return this; // jquery deferred 반환.
            }
        });

        var _hash = $.now().toString();

        this.getHash = function () {
            return _hash;
        };

        this.getUrl             = function () {
            return _url;
        };
        this.getOption          = function () {
            return _option;
        };
        this.getReqOption       = function () {
            return _reqOption;
        };
        this.getPushStateOption = function () {
            return _pushStateOption;
        };
        this.getDeferred        = function () {
            return _deferred;
        };
    };

    /**
     * 아작스 요청 que getter
     * @returns {Array}
     */
    Ajax.getQue = function () {
        return ajaxRequestQue;
    };

    /**
     * QUSI ajax common
     * @param url: 요청 url
     * @param option: $.ajax 요청 옵션
     * @param _reqOption: QUSI 리퀘스트시 필요한 옵션 (format:{json, html, etc}, method:{post, put, get, etc}, option : {allowRepeat: 중복 요청 가능})
     * @param pushStateOption: pushState 옵션 {title:'타이틀 명칭', path:'푸시스테이트의 path'}
     */
    Ajax.ajax = function (url, option, _reqOption, pushStateOption) {
        return extendDeferredCallbackForQusi(pushQue(url, option, _reqOption, pushStateOption, new $.Deferred()), _reqOption);
    };

    /**
     * QUSI ajax get
     * @param url: 요청 url
     * @param option: $.ajax 요청 옵션
     * @param reqOption: QUSI 리퀘스트시 필요한 옵션 (_format:{json, html, etc}, _method:{post, put, get, etc})
     * @param pushStateOption: pushState 옵션 {title:'타이틀 명칭', path:'푸시스테이트의 path'}
     */
    Ajax.get = function (url, option, reqOption, pushStateOption) {
        return Ajax.ajax(url, option, reqOption, pushStateOption);
    };

    /**
     * QUSI ajax post common
     * @param url: 요청 url
     * @param option: $.ajax 요청 옵션
     * @param reqOption: QUSI 리퀘스트시 필요한 옵션 (_format:{json, html, etc}, _method:{post, put, get, etc})
     * @param pushStateOption: pushState 옵션 {title:'타이틀 명칭', path:'푸시스테이트의 path'}
     */
    Ajax.post = function (url, option, reqOption, pushStateOption) {
        option = option || {};
        return this.ajax(url, $.extend({method: 'post'}, option), $.extend({method: 'post'}, reqOption), pushStateOption);
    };


    /**
     * QUSI ajax put common
     * @param url
     * @param option
     * @param reqOption
     * @param pushStateOption
     */
    Ajax.put = function (url, option, reqOption, pushStateOption) {
        option = option || {};

        var reqParam = {_method: 'put'};

        return this.ajax(url, $.extend({
            method: 'post'
        }, option), $.extend(reqParam, reqOption), pushStateOption);
    };

    /**
     * QUSI ajax delete common
     * @param url
     * @param option
     * @param reqOption
     * @param pushStateOption
     */
    Ajax.remove = function (url, option, reqOption, pushStateOption) {
        option = option || {};

        var reqParam = {_method: 'delete'};

        return this.ajax(url, $.extend({
            method: 'post'
        }, option), $.extend(reqParam, reqOption), pushStateOption);
    };


    /**
     * QUSI form ajax 요청
     * @param form form
     * @param option 아작스 옵션
     * @param reqOption
     * @param pushStateOption
     */
    Ajax.formAjax = function (form, option, reqOption, pushStateOption) {
        var Ajax = this;

        var targetForm = $(form);

        return Ajax.ajax(targetForm.attr('action'), $.extend({
            type: 'post',
            data: targetForm.serialize()
        }, option || {}), $.extend({_method: targetForm.attr('method') || 'post'}, reqOption), pushStateOption);
    };

    /**
     * 아작스 업로드
     * @param _form {$}
     * @param option {Object}
     * @param reqOption
     */
    Ajax.syncFormUpload = function (_form, option, reqOption) {

        var actionUrl      = $(_form).attr('action');
        var uploadDeferred = new $.Deferred()
            .progress(function (ev, prog) {
                Ajax.loadingBar.stop().animate({width: parseInt(prog) * 100}, {duration: 900});
            });

        // form 이 submit 될 때
        $(_form).submit(function (e) {

            // 기본 이벤트를 막는다.
            e.stopPropagation();
            e.preventDefault();

            // 리퀘스트 옵션 오브젝트를 qusi 프로젝트에 알맞도록 adopt 한다.
            reqOption = requestOptionAdoptKeyname(reqOption);

            // 기본 리퀘스트 파람 등록
            var reqParam                     = {};
            reqParam[qusi.static.ajax.method] = 'post';
            reqParam[qusi.static.ajax.format] = 'json';

            // 업로드를 시작할 form의 action을 qusi의 형식에 맞도록 수정한다.
            var urlParam = $.param(
                $.extend(
                    $.extend(
                        $.extend(reqParam, reqOption),
                        reqOption),
                    actionUrl.indexOf('&') > 0 ? qusi.util.queryToJson(actionUrl) : {}
                )
            );

            // 폽의 action을 위에서 생성한 알맞은 url 형식으로 수정한다.
            $(_form).prop('action', actionUrl.split('?')[0] + (urlParam ? '?' + urlParam : ''));

            // jquery form 플러그인을 사용하여 아작스 업로드를 시작한다.
            $(_form).ajaxSubmit($.extend({
                type       : 'post',
                cache      : false,
                dataType   : 'json',
                processData: false,
                contentType: false,

                xhr  : function () {

                    var uploadXHR = $.ajaxSettings.xhr();

                    uploadXHR.upload.addEventListener('progress', function (ev) {
                        if (ev.lengthComputable) {
                            var percentComplete = Math.round((ev.loaded / ev.total) * 100);
                            uploadDeferred.notify(ev, percentComplete);
                        }
                    }, false);

                    return uploadXHR;
                },
                // 아작스 업로드에 실패했을 경우
                error: function (jqXHR, textStatus, errorThrown) {
                    // 생성한 데퍼드 오브젝트를 reject 시킨다.
                    uploadDeferred.reject(jqXHR, textStatus, errorThrown);
                },

                // 아작스 업로드가 종료 된다면.
                complete: function (jqXHR, textStatus) {
                    // 생성한 데퍼드 오브젝트의 always 를 시킨다.
                    uploadDeferred.always(jqXHR, textStatus);

                    // 성공, 마무리 한다 (애니메이션 등의 종료 이벤트 시작)
                    whenDone(
                        true,
                        (typeof jqXHR.responseJSON !== 'undefined' ? jqXHR.responseJSON : jqXHR.responseText),
                        jqXHR,
                        textStatus
                    )

                },
                success : function (data, textStatus, jqXHR) {
                    uploadDeferred.resolve(data, textStatus, jqXHR);
                }

            }, option));

            // 아작스 로딩바를 나오도록 만들고, 아작스 로딩 애니메이션 초기화를 위해  width 값을 0으로 맞춘다.
            $(Ajax.loadingBar).stop().css({opacity: 1, width: 0});

            // 아작스 업로드가 진행 될 때 마다 이벤트를 이용해 uploadDeferred 를 notify 한다.
            $(window).on('uploading', function (ev, percentage) {
                $(Ajax.loadingBar).stop().animate({width: percentage}, {duration: 900});
                uploadDeferred.notify(ev, percentage);
            });


        });

        return extendDeferredCallbackForQusi(uploadDeferred, reqOption);

    };


    /**
     * 아작스 로딩시 상단 로딩 바를 만듭니다.
     * @param ajax
     * @param noAnimation
     * @returns {*}
     */
    Ajax.makeTopLoading = function (ajax, noAnimation) {

        noAnimation = noAnimation || false;

        if (!noAnimation) {

            var percent = 100 - Math.floor((ajaxRequestQue.length / ajaxRequestQueFirstLength) * 100);
            Ajax.loadingBar.stop().css({opacity: 1}).animate({
                width: percent + '%'
            }, {duration: 100});

            if (ajaxRequestQue.length < 1)
                Ajax.loadingBar.animate({
                    width: '100%'
                }, {
                    duration: 4000,
                    easing  : 'easeOutQuint'
                });


            // fail 이 되었을 경우
            ajax.then(null,
                function () {
                    $(Ajax.loadingBar).css({backgroundColor: 'red'});
                }
            );
        }

        return ajax;
    };

    $(function () {
        $('body').append(Ajax.loadingBar);

        // 모든 QUE 가 완료되면 정리한다.
        $(window).on(qusi.static.ajax.ajaxSuccess, function () {
            ajaxRequestQueFirstLength = 0;
            failedDataList            = [];
            completeAnim();
        });

        $(window).on(qusi.static.ajax.ajaxFail, function () {
            ajaxRequestQueFirstLength = 0;
            failedDataList            = [];
            completeAnim();
        });


        var completeAnim = function () {

            $(Ajax.loadingBar).stop().animate({
                width: '100%'
            }, {
                duration: 500,
                complete: function () {
                    $(this).animate({opacity: 0}, {
                        duration: 500,
                        complete: function () {
                            $(this).css({width: 0, backgroundColor: '#76a2ff'});
                        }
                    });
                }
            })

        }
    });

    return qusi.Ajax = Ajax;
})(qusi);