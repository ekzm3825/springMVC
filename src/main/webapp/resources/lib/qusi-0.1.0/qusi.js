/**
 * QUSI - Common
 *
 * @requires jquery-1.11.3
 * @version 0.1.0
 * @author yunchan
 * @date 2016. 2. 15
 */
var qusi = {};

(function (qusi) {
    /**
     * 스태틱 정보
     * @type {{version: number}}
     */
    qusi.static = {
        version: '0.1.0'
    };

    /**
     * QUSI 유틸리티
     * @type {{urlParam: Window.util.urlParam}}
     */
    qusi.util = {

        /**
         * 오브젝트를 비교한다.
         * 오브젝트의 값중의 오브젝트는 비교 할 수 없으니 주의하여 사용 할 것
         * _.isEqual() 의 기능과 동일함. underscore를 사용한다면 해당 라이브러리의 기능을 사용하는것이 좋습니다.
         * @param value{Object}
         * @param target{Object}
         * @returns {boolean}
         */
        isEqualValue: function (value, target) {

            // 만약 두 값 모두 undefined 값이라면 모두 동일하다고 할 수 있다.
            if (value === undefined && target === undefined)
                return true;
            else if (value !== undefined || target === undefined)
                return false;
            else if (value === undefined || target !== undefined)
                return false;

            // 오브젝트의 키값의 어레이를 생성한다.
            var aProps = Object.getOwnPropertyNames(value);
            var bProps = Object.getOwnPropertyNames(target);

            // 만약 값의 길이가 같지 않다면 다른 값이다,
            // 오브젝트는 같다고 할 수 없다
            if (aProps.length != bProps.length) {
                return false;
            }

            for (var i = 0; i < aProps.length; i++) {
                var propName = aProps[i];

                // 만약 프로퍼티의 값이 동일하지 않다면 ,
                // 오브젝트는 같지 않다고 할 수 있다.
                if (value[propName] !== target[propName]) {
                    return false;
                }
            }

            // 위의 필터를 모두 통과한다면 동일한 오브젝트라고 할 수 있다.
            return true;
        },

        /**
         * Url 파라미터
         * @param name
         * @returns {*|number}
         */
        urlParam: function (name) {
            var paramString = name ? name.split('?')[1] : '';
            if (paramString)
                return JSON.parse('{"' + decodeURI(paramString).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g, '":"') + '"}');
            else
                return {};
        },

        /**
         * querystring을 JSON 오브젝트로 변환한다.
         * @param query
         * @returns {{}|*}
         */
        queryToJson: function (query) {
            var j, q;
            q = query.replace(/\?/, "").split("&");
            j = {};
            if(Object.keys(j).length > 0)
                $.each(q, function (i, arr) {
                    arr = arr.split('=');
                    return j[arr[0]] = arr[1];
                });
            return j;
        },

        url: (function () {
            var url = {};
            url.addParamsToUrl = function (url, params) {
                var splitArray = url.split('?');
                var urlExceptQueryString = splitArray[0];
                var queryString = '';

                var resultUrl = urlExceptQueryString;

                if (splitArray.length > 1) {
                    queryString = splitArray.splice(1).join('');
                }

                if (typeof params === 'string') {
                    queryString += '&' + params;
                } else if (typeof params === 'object') {
                    $.each(params, function (k, v) {
                        queryString += '&' + encodeURIComponent(k) + '=' + encodeURIComponent(v);
                    });
                }

                if (s.startsWith(queryString, '&')) {
                    queryString = queryString.substr(1);
                }

                if (queryString) {
                    resultUrl += '?' + queryString;
                }

                return resultUrl;
            };

            url.toObjectProperty = function (obj, parentName) {
                var result = {};

                var recursive = function (obj, parentName) {
                    if (obj.constructor === [].constructor) {
                        $.each(obj, function (i, v) {
                            recursive(v, parentName + '[' + i + ']');
                        });
                    }
                    else if (obj.constructor === {}.constructor) {
                        $.each(obj, function (i, v) {
                            recursive(v, parentName + (parentName ? '.' : '') + i);
                        });
                    }
                    else {
                        result[parentName] = obj;
                    }
                };

                recursive(obj, parentName || '');

                return result;
            };

            url.toMapProperty = function (obj, parentName) {
                var result = {};

                var recursive = function (obj, parentName) {
                    if (obj.constructor === [].constructor) {
                        $.each(obj, function (i, v) {
                            recursive(v, parentName + '[' + i + ']');
                        });
                    }
                    else if (obj.constructor === {}.constructor) {
                        $.each(obj, function (i, v) {
                            recursive(v, parentName + '[' + i + ']');
                        });
                    }
                    else {
                        result[parentName] = obj;
                    }
                };

                recursive(obj, parentName || '');

                return result;
            };

            return url;
        })(),

        form: (function () {
            var form = {};
            /**
             * form 요소들을 그룹핑
             * groupName 에 공통된 각 요소들의 name 앞글자를 입력하면
             * name 의 앞글자가 동일한 폼요소들이 그룹핑되어 리턴
             */
            form.filterFormGroup = function (formGroup, groupName, delimiter) {
                var convertRecursively = function (targetMap, delimiter, key, value) {

                    if (key.indexOf(delimiter, 1) > -1) {
                        var currentKey = key.substring(0, key.indexOf(delimiter, 1));
                        var currentChildKey = key.substring(key.indexOf(delimiter, 1));

                        currentKey = removeDelimiter(currentKey, delimiter);

                        if (typeof targetMap[currentKey] != 'object') {
                            targetMap[currentKey] = {};
                        }

                        convertRecursively(targetMap[currentKey], delimiter, currentChildKey, value);
                    }

                    else {
                        key = removeDelimiter(key, delimiter);

                        if (targetMap[key]) {
                            if (!targetMap[key].push) {
                                targetMap[key] = [targetMap[key]];
                                targetMap[key].push(value);
                            } else {
                                targetMap[key].push(value);
                            }
                        }

                        else {
                            targetMap[key] = value;
                        }
                    }
                };

                var removeDelimiter = function (key, delimiter) {
                    if (delimiter == '[') {
                        key = key.replace(new RegExp('[\\[\\]]*', 'g'), '');
                    } else {
                        key = key.replace(new RegExp('\\' + delimiter + '*', 'g'), '');
                    }

                    return key;
                };

                delimiter = delimiter || '.';
                groupName = groupName || '';

                var group = $(formGroup).find('[name^="' + groupName + delimiter + '"]').addBack('[name^="' + groupName + delimiter + '"]');
                var groupTable = {};

                group.each(function (i, v) {
                    var currentName = v.name;
                    if (currentName.charAt(0) == delimiter) {
                        currentName = currentName.substring(1);
                    }

                    currentName = currentName.replace(groupName, '');

                    convertRecursively(groupTable, delimiter, currentName, v);
                });

                return groupTable;
            };

            form.disableElements = function (elements) {
                var elements = $(elements);

                elements.filter(':input').prop('disabled', true);
                elements.filter('a')
                    .eventPause('pause')
                    .on('click.disableElements', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                    });
            };

            form.enableElements = function (elements) {
                var elements = $(elements);

                elements.filter(':input').prop('disabled', false);
                elements.filter('a')
                    .eventPause('active')
                    .off('click.disableElements');
            };

            return form;
        })(),

        date: (function () {
            var date = {};
            /**
             * 날짜 포맷 형식 지정
             * @param date        포맷을 설정할 날짜
             * @param format    날자 포맷 형식 : 기본값은 YYYY-MM-DD
             * @param patten    날짜 중간에 포함할 특수문자 : 기본값은 -
             */
            date.setDateFormat = function (date, format, patten) {
                if (date == null) {
                    return '';
                }

                var f;
                var p = '';

                p = patten != undefined ? patten : '-';

                switch (format) {
                    case 'ymd':
                        f = 'YYYY' + p + 'MM' + p + 'DD';
                        break;
                    case 'ymdhms':
                        f = 'YYYY' + p + 'MM' + p + 'DD ' + 'hh:mm:ss';
                        break;
                    case 'ymdHms':
                        f = 'YYYY' + p + 'MM' + p + 'DD ' + 'HH:mm:ss';
                        break;
                    case 'ymdhm':
                        f = 'YYYY' + p + 'MM' + p + 'DD ' + 'HH:mm';
                        break;
                    case 'ymdHm':
                        f = 'YYYY' + p + 'MM' + p + 'DD ' + 'HH:mm';
                        break;
                    default:
                        f = 'YYYY' + p + 'MM' + p + 'DD';
                        break;
                }

                return moment(date).format(f);
            };

            /**
             * 현재 날짜와 시작일~종료일 기간 비교해서 상태 return
             * @param startDate 기간 시작일
             * @param endDate 기간 종료일
             */
            date.checkStatus = function (startDate, endDate) {
                var today = new Date();
                var status = '';
                startDate = startDate.date().print();
                endDate = endDate.date().print();

                if (today.date().diff(startDate) > 0) {
                    status = 'scheduled';
                } else if (today.date().diff(startDate) <= 0 && today.date().diff(endDate) >= 0) {
                    status = 'active';
                } else if (today.date().diff(endDate) < 0) {
                    status = 'deactive';
                }

                return status;
            };

            return date;
        })(),

        numberFormat: (function () {

            var numberFormat = {};

            /**
             * 숫자를 금액 표기법으로 변경하여 리턴
             */
            numberFormat.formatPrice = function (number) {

                if (number == undefined || number == null) {
                    return '0';
                }

                var reg = /(^[+-]?\d+)(\d{3})/;
                var price = number.toString();

                while (reg.test(price)) {
                    price = price.replace(reg, '$1' + ',' + '$2');
                }

                return price;
            };

            /**
             * 콤마가 들어간 금액에서 콤마를 제거
             */
            numberFormat.unformatPrice = function (number) {

                if (number == undefined) return 0;

                return number.toString().replace(/,/gi, "");
            };

            /**
             * 금액 input 에 keyup 이벤트로 콤마가 들어가게함
             */
            numberFormat.bindFormatPrice = function (selector) {
                $(selector).on('keyup', function (e) {
                    var unfomatValue = numberFormat.unformatPrice(this.value);
                    this.value = numberFormat.formatPrice(unfomatValue);
                });
            };

            /**
             * 선택된 selector 의 value 에서 콤마를 모두 제거
             * submit 시 주로 사용
             */
            numberFormat.unformatPriceSelector = function (selector) {
                $(selector).each(function (i, v) {
                    this.value = numberFormat.unformatPrice(this.value);
                });
            };

            return numberFormat;
        })(),

        string: (function () {
            var str = {};
            str.getByteLength = function (str, bytePerUnicode) {
                var byte = bytePerUnicode || 3;

                var b = 0;
                var c = '';

                for (var i = 0; c = str.charCodeAt(i++); b += c >> 11 ? byte : c >> 7 ? 2 : 1);

                return b;
            };

            return str;

        })()


    };

    /**
     * 유효성 검사
     */
    qusi.valid = (function () {
        var valid = {};
        var _elements = [];
        var _defaultCallback;

        var self = this;

        valid.ValidateException = function (_target, _rule, _ruleValue, _message, _callback) {
            var target = _target;
            var rule = _rule;
            var ruleValue = _ruleValue;
            var message = _message;
            var callback = _callback;

            valid.focusing = function () {
                if (typeof valid.target.focus !== 'undefined') {
                    valid.target.focus();
                }
            };
        };

        var rules = {
            'required': function (ruleValue, inputValue) {
                if (!inputValue) {
                    return true;
                }
            },

            'min-length': function (ruleValue, inputValue) {
                if (inputValue.length < ruleValue) {
                    return true;
                }
            },

            'max-length': function (ruleValue, inputValue) {
                if (inputValue.length > ruleValue) {
                    return true;
                }
            },

            'min-byte': function (ruleValue, inputValue) {
                var inputValueByte = qusi.util.string.getByteLength(inputValue, 3);

                if (inputValueByte < ruleValue) {
                    return true;
                }
            },

            'max-byte': function (ruleValue, inputValue) {
                var inputValueByte = qusi.util.string.getByteLength(inputValue, 3);

                if (inputValueByte > ruleValue) {
                    return true;
                }
            },

            'email': function (ruleValue, inputValue) {
                var r = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

                return !r.test(inputValue);
            },

            'regexp': function (ruleValue, inputValue) {
                return !ruleValue.test(inputValue);
            },

            'allow-char': function (ruleValueArray, inputValue) {
                // alphabet, alphabet-upper, alphabet-lower, hangul, numeric
                // (any special characters)

                if (typeof ruleValueArray === 'string') {
                    ruleValueArray = ruleValueArray.split(',');
                }

                var rStr = '';

                $.each(ruleValueArray, function (i, v) {
                    v = $.trim(v);

                    if (v == 'alphabet') {
                        rStr += 'a-zA-Z';
                    } else if (v == 'alphabet-upper') {
                        rStr += 'A-Z';
                    } else if (v == 'alphabet-lower') {
                        rStr += 'a-z';
                    } else if (v == 'hangul') {
                        rStr += '가-힣';
                    } else if (v == 'numeric') {
                        rStr += '0-9';
                    } else {
                        rStr += v;
                    }
                });

                var r = new RegExp('^[' + rStr + ']+$');

                return !r.test(inputValue);
            },
            phoneNumber: function (ruleValue, inputValue) {
                var phoneNumberRegex = /^[0-9]{3}-[0-9]{4}-[0-9]{4}$/;

                return !phoneNumberRegex.test(inputValue)
            }
        };

        var _check = function (target, rule, ruleValue, message, callback) {
            var value = '';

            if (typeof target === 'string') {
                value = target;
            } else if (target instanceof jQuery) {
                value = target.val();
            } else if (typeof target.value !== 'undefined') {
                value = target.value;
            }

            value = $.trim(value);

            var result = rules[rule].apply(this, [ruleValue, value]);

            if (result) {
                throw new valid.ValidateException(target, rule, ruleValue, message, callback);
            }
        };

        valid.setDefaultCallback = function (callback) {
            _defaultCallback = callback;
        };

        valid.add = function (target, rule, ruleValue, message, callback) {
            _elements.push({
                'target': target,
                'rule': rule,
                'ruleValue': ruleValue,
                'message': message,
                'callback': callback
            })
        };

        valid.validate = function () {
            var result = true;

            $.each(_elements, function (i, v) {
                try {
                    _check(v.target, v.rule, v.ruleValue, v.message, v.callback);
                }

                catch (e) {
                    if (e instanceof valid.ValidateException) {
                        if (e.callback && typeof e.callback === 'function') {
                            e.callback.apply(valid, [e]);
                        } else if (_defaultCallback && typeof _defaultCallback === 'function') {
                            _defaultCallback.apply(valid, [e]);
                        }

                        return result = false;
                    } else {
                        console.error(e);
                    }
                }
            });

            return result;
        };

        return valid;
    })();


    /**
     * 팝업
     */
    qusi.popup = (function () {

        var popup = {};

        /**
         * 팝업 윈도우에서 opener 객체를 통해 작업을 진행할 경우
         * 본 callback 함수에 접근할 변수나 함수들을 담아 접근 할 수 있게 함
         */
        this.callback = {};

        /**
         * 팝업윈도우를 센터 포지션으로 위치시키기 위해 계산
         */
        var _calculateCenterPosition = function (w, h) {
            // ref. http://stackoverflow.com/questions/4068373/center-a-popup-window-on-screen
            // Fixes dual-screen position                         Most browsers      Firefox
            var dualScreenLeft = window.screenLeft != undefined ? window.screenLeft : screen.left;
            var dualScreenTop = window.screenTop != undefined ? window.screenTop : screen.top;

            var width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
            var height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

            return {
                left: ((width / 2) - (w / 2)) + dualScreenLeft,
                top: ((height / 2) - (h / 2)) + dualScreenTop
            };
        };

        /**
         * 팝업 옵션들을 key=value, 형태로 변환
         */
        var _makeOptionString = function (options, fieldNames) {
            var arr = [];

            $.each(fieldNames, function (i, v) {
                var optionValue = options[v];

                if (typeof optionValue === 'boolean') {
                    optionValue = optionValue ? 'yes' : 'no';
                }

                arr.push(v + '=' + optionValue);
            });

            return arr.join(',');
        };

        /**
         * 팝업 열기
         */
        popup.open = function (popupOptions) {
            popupOptions = popupOptions || {};

            var options = $.extend({
                url: '',
                name: '',
                top: '0',
                left: '0',
                width: '200',
                height: '200',
                params: {},             // URL 파라메터 부분 (querystring)
                resizable: false,       // (IE only) Window is resizable.
                status: false,          // Add a status bar (bottom)
                scrollbars: true,       // Display scroll bars.
                location: false,        // (Opera only) Display the address field.
                menubar: false,         // Display menu bar. (Contains File, Edit, etc)
                toolbar: false,         // (IE, FF only) Display the browser toolbar.
                titlebar: false,        // Display the browser title bar.
                copyhistory: false,     // Copy the old browser window's history list to the new window.

                fullscreen: false,      // (IE only) Display the browser in full-screen mode. Default is no.
                                        // A window in full-screen mode must also be in theater mode.
                channelmode: false,     // Display the window in theater mode. Default is no. IE only
                directories: false,     // Obsolete. Add directory buttons. Default is yes. IE only

                focus: true             // Enable window.focus().
            }, popupOptions);

            // calcaulte top, left position (default)
            options = $.extend(options, _calculateCenterPosition(options.width, options.height), {
                top: popupOptions.top,
                left: popupOptions.left
            });

            var popupOptionString = _makeOptionString(options, [
                'width', 'height', 'top', 'left',
                'fullscreen', 'location', 'menubar', 'resizable', 'scrollbars',
                'status', 'toolbar', 'titlebar'
            ]);

            var url = qusi.util.url.addParamsToUrl(options.url, options.params);

            // open popup
            var openedPopup = window.open(url, options.name, popupOptionString);

            // focus popup window
            if (options.focus && openedPopup.focus) {
                openedPopup.focus();
            }

            return openedPopup;
        };

        return popup;
    })();

    /**
     * Polyfilling
     */
    qusi.polyfilling = (function (){

        // 기본 기능 확장
        Array.prototype.last = function () {
            return this[this.length - 1];
        };

        if (!Object.keys) {
            Object.keys = (function () {
                'use strict';
                var hasOwnProperty = Object.prototype.hasOwnProperty,
                    hasDontEnumBug = !({toString: null}).propertyIsEnumerable('toString'),
                    dontEnums = [
                        'toString',
                        'toLocaleString',
                        'valueOf',
                        'hasOwnProperty',
                        'isPrototypeOf',
                        'propertyIsEnumerable',
                        'constructor'
                    ],
                    dontEnumsLength = dontEnums.length;

                return function (obj) {
                    if (typeof obj !== 'object' && (typeof obj !== 'function' || obj === null)) {
                        throw new TypeError('Object.keys called on non-object');
                    }

                    var result = [], prop, i;

                    for (prop in obj) {
                        if (hasOwnProperty.call(obj, prop)) {
                            result.push(prop);
                        }
                    }

                    if (hasDontEnumBug) {
                        for (i = 0; i < dontEnumsLength; i++) {
                            if (hasOwnProperty.call(obj, dontEnums[i])) {
                                result.push(dontEnums[i]);
                            }
                        }
                    }
                    return result;
                };
            }());
        }
    })();

    return qusi;

})(qusi || window.qusi || (qusi = {}));