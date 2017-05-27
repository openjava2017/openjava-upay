var utils = (function() {
    var info = function info(message, setting) {
        var o = {
            'title': '',
            'position': ['right - 20', 'top + 20'],
            'width': 0,
            'auto_close': 2000
        };
        $.extend(true, o, setting);

        new $.Zebra_Dialog(message, {
            'type': 'information',
            'title': o.title,
            'buttons':  false,
            'modal': false,
            'position': o.position,
            'width': o.width,
            'auto_close': o.auto_close
        });
    }

    var warn = function warn(message, setting) {
        var o = {
            'title': '',
            'position': ['right - 20', 'top + 20'],
            'width': 0,
            'auto_close': 2000
        };
        $.extend(true, o, setting);

        new $.Zebra_Dialog(message, {
            'type': 'warning',
            'title': o.title,
            'buttons':  false,
            'modal': false,
            'position': o.position,
            'width': o.width,
            'auto_close': o.auto_close
        });
    }

    var error = function error(message, setting) {
        warn(message, setting);
    }

    var openAjaxDialog = function(url, setting, buttons) {
        if (!url) {
            if (console) {
                console.error("Url missed for openAjaxDialog");
            }
            return;
        }

        var o = {
            'title': '',
            'cache': false,
            'width': 0,
            'data': {},
            'type': false,
            'position': ['center', 'middle'],
            'width': 0,
            'max_height': 0,
            'modal': true,
            'keyboard': true,
            'center_buttons': false,
            'buttons': true,
            'complete': function(XHR, TS) {},
            'onClose': function(caption) {}
        };
        $.extend(true, o, setting);
        o.buttons = buttons || true;
        o.source = {
            'ajax': {
                'url': url,
                'data': o.data,
                'cache': o.cache,
                'complete': o.complete
            }}

        return new $.Zebra_Dialog('', o);
    }

    return {
        info: info,
        warn: warn,
        error: error,
        openAjaxDialog: openAjaxDialog
    }
})();