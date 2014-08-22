var attemptID;
var status;
var error_log_rtn;
var log_rtn;
var result;
var is_flash;
$(document).ready(function () {
    attemptID = GetQueryString("id"); //通过表达式获得传递参数
    status = get_task_status();
    is_flash = true;
    var is_new = is_new_agent();
    var error_panel=document.getElementById("error-panel");
    var log_panel = document.getElementById("spann");
    if (is_new == "true"){
        error_panel.style.display="none";
        log_panel.style.width="95%";
    }
    do_relash_task();
});

function GetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}


function fetch_errorLog() {
    var $logContainer = $("#errolog");
    var is_end = is_log_end();
    if(is_end == "true"){
        clearInterval(error_log_rtn);
    }
    status = get_task_status();

    $.ajax({
        url : "attempts.do",
        data : {
            id : attemptID,
            action : 'runlog',
            querytype:'errorlog',
            status:status
        },
        timeout : 1000000,
        type : 'POST',
        error: function(){
            $logContainer.text("无数据");
        },
        success: function (response) {
            result = response.replace("\n","<br>")

            $logContainer.append("<div class=\"terminal-like\">"+response+"</div>");
            $logContainer.scrollTop($logContainer.get(0).scrollHeight);
            $(".loading").hide();
        },
        beforeSend:function(){//正在加载，显示“正在加载......”
            $(".loading").show();
        }

    });



}

function fetch_Log() {
    var $logContainer = $("#strout")

    var is_end = is_log_end();
    if(is_end == "true"){
        clearInterval(log_rtn);
    }

    status = get_task_status();

    $.ajax({
        url: "attempts.do",
        data: {
            id: attemptID,
            action: 'runlog',
            querytype:'log',
            status:status
        },
        timeout: 1000000,
        type: 'POST',
        error: function () {
            $logContainer.text("没有找到日志数据");
        },
        success: function (response) {

            result = response.replace("\n","<br>");

            $logContainer.append("<div class=\"terminal-like\">"+response+"</div>");
            $logContainer.scrollTop($logContainer.get(0).scrollHeight);
            $(".loading").hide();
        },
        beforeSend:function(){//正在加载，显示“正在加载......”
            $(".loading").show();
        }

    });

}

function do_relash_task(){
    if(status == "RUNNING"){
        var is_end = is_log_end();
        if(is_end == "false"){
            error_log_rtn = setInterval("fetch_errorLog()", 1500);
            log_rtn = setInterval("fetch_Log()", 1500);
        }else{
            fetch_errorLog();
            fetch_Log();
        }

    }else
    {
        fetch_errorLog();
        fetch_Log();
    }
}

function is_log_end() {
var ret="";
    $.ajax({
        url : "attempts.do",
        data : {
            id : attemptID,
            action : 'isend'
        },
        timeout : 10000,
        type : 'POST',
        async:false,
        error: function(){
            ret =  "null"
        },
        success: function (response) {
            if(response == "true")
            {
                ret = "true";

            }else{
                ret = "false";
            }
        }

    });
    return ret;
}


function get_task_status() {
    var ret="";
    $.ajax({
        url : "attempts.do",
        data : {
            id : attemptID,
            action : 'status'
        },
        timeout : 10000,
        type : 'POST',
        async:false,
        error: function(){
            ret =  "null"
        },
        success: function (response) {
            ret = response;
        }

    });
    return ret;
}

function is_new_agent() {
    var ret="";
    $.ajax({
        url : "attempts.do",
        data : {
            id : attemptID,
            action : 'isnew'
        },
        timeout : 10000,
        type : 'POST',
        async:false,
        error: function(){
            ret =  "null"
        },
        success: function (response) {
            ret = response;
        }

    });
    return ret;
}

window.onbeforeunload=function(){
    if(status == "RUNNING"){
        opener.document.location.reload();
    }

}

