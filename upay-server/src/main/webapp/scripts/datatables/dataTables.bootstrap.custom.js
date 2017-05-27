/* Set the defaults for DataTables initialisation */
var DataTable = $.fn.dataTable;
$.extend(true, DataTable.defaults, {
    "searching": false,
    "ordering": false,
    "pageLength": 20,
    "lengthMenu": [10, 15, 20, 30, 50],
    "language": {
        "lengthMenu": "显示 _MENU_ 条记录",
        "info": "显示第_START_条到第_END_条    总记录数_TOTAL_条",
        "infoEmpty": "从第 0条到第0条, 总记录数 0条",
        "loadingRecords": "正在努力加载数据...",
        "processing": "正在努力加载数据...",
        "zeroRecords": "没有数据记录",
        "emptyTable": "没有数据记录",
        "paginate": {
            "first": "第一页",
            "previous": "上一页",
            "next": "下一页",
            "last": "最后一页"
        }
    }
});

$.extend($.fn.dataTable.Api.prototype, {
    getSelectedId: function (identifier) {
        var selectedRow = this.rows('.selected').data();
        var id = null;
        if (selectedRow.length > 1) {
            throw "Multiple row selected";
        }

        if (selectedRow.length == 1) {
            id = selectedRow[0][identifier ? identifier : "id"];
            if (!id) {
                throw "Invalid selected id in dataTable";
            }
        }

        return id;
    },
    getSelectedIds: function (identifier) {
        var selectedRow = this.rows('.selected').data();
        var ids = null;

        var id = null;
        if (selectedRow.length > 0) {
            ids = [];
            for (var i=0; i<selectedRow.length; i++) {
                id = selectedRow[i][identifier ? identifier : "id"];
                if (!id) {
                    throw "Invalid selected id in dataTable";
                }
                ids.push(id);
            }
        }

        return ids;
    }
});