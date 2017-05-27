var tablePage = (function() {
    var pageTable;
    var doInit = function () {
        pageTable = $('#pageTable').DataTable({
            "processing": true,
            "paginate": true,
            "deferLoading": true,
            "pageLength": 10,
            "ajax": {
                url: contextPath +"/demo/listEmployees.action",
                type: "POST",
                data: function(params) {
                    params["account"] = $.trim($("#account").val());
                    params["mobile"] = $.trim($("#mobile").val());
                    return params;
                }
            },
            "serverSide": true,
            "createdRow": function(row, data, dataIndex) {
                if (data['account'] == 'root') { // 高亮显示root用户
                    $(row).addClass('focus');
                }
            },
            "columnDefs": [ {"targets": [2], "createdCell": function (td, cellData, rowData, row, col) {
                if (cellData == "女") { // 高亮显示性别“女”的用户
                    $(td).css('color', 'red')
                }
            }}],
            "columns": [
                {"data": "account", "defaultContent": ""},
                {"data": "name", "defaultContent": ""},
                {"data": "gender", "defaultContent": ""},
                {"data": "mobile", "defaultContent": ""},
                {"data": "email", "defaultContent": ""},
                {"data": "status", "defaultContent": ""},
                {"data": "description", "defaultContent": ""}
            ]
        });

        $('#pageTable tbody').on('mouseover', 'tr', function () {
            $(this).addClass('highlight');
        });
        $('#pageTable tbody').on('mouseout', 'tr', function () {
            $(this).removeClass('highlight');
        });
        $('#pageTable tbody').on('click', 'tr', function () {
            var that = $(this);
            if (that.attr("fake") != "true") {
                if (that.hasClass('selected')) {
                    that.removeClass('selected');
                } else {
                    pageTable.$('tr.selected').removeClass('selected');
                    that.addClass('selected');
                }
            }
        });

        $('#employeeTable').DataTable({
            "processing": true,
            "paginate": true,
            "pageLength": 10
        });
    }

    var doSearch = function() {
        pageTable.draw();
    }

    var doSelect = function() {
        var account = pageTable.getSelectedId("account");
        if (account) {
            info("你选择了账号为" + account + "的用户");
        } else {
            info("请单击选择一个用户");
        }

    }

    return {
        doInit: doInit,
        doSearch: doSearch,
        doSelect: doSelect
    };
})();

$(function() {
    tablePage.doInit();

    $("#queryButton").click(function() {
        tablePage.doSearch();
    });

    $("#selectButton").click(function() {
        tablePage.doSelect();
    });
});

