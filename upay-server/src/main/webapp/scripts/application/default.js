var application = (function() {
    var doInit = function () {
        $('#side-menu').metisMenu();

        // 当页面resize时设置page-content的min-height以便去除滚动条
        $(window).bind("load resize", function() {
            var topOffset = 50;
            var width = (this.window.innerWidth > 0) ? this.window.innerWidth : this.screen.width;
            if (width < 768) {
                $('div.navbar-collapse').addClass('collapse');
                topOffset = 100; // 2-row-menu
            } else {
                $('div.navbar-collapse').removeClass('collapse');
            }

            var height = ((this.window.innerHeight > 0) ? this.window.innerHeight : this.screen.height) - 1;
            var height = height - topOffset;
            if (height < 1) height = 1;
            if (height > topOffset) {
                $("#page-content").css("min-height", (height) + "px");
            }
        });

        // 当页面url与某个菜单的href相同时(页面是点击某个菜单的功能页面，则自动将主菜单自动展开)
        var url = window.location;
        var element = $('ul.nav a').filter(function() {
            return this.href == url || url.href.indexOf(this.href) == 0;
        }).addClass('active').parent().parent().addClass('in').parent();
        if (element.is('li')) {
            element.addClass('active');
        }
    }

    var loadPage = function(url, params, callback) {
        $("#page-content").load(url, params, callback);
    }

    return {
        doInit: doInit,
        loadPage: loadPage
    };
})();

$(function() {
    application.doInit();
});

