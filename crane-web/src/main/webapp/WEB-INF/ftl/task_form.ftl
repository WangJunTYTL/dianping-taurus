
<div class="modal-dialog "style="width: 900px">
<div class="modal-content" >
<div id="modal-confirm" class="modal fade" role="dialog"
     aria-hidden="true" >
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <a href="#" class="close">&times;</a>

                <h3>修改成功</h3>
            </div>
            <div class="modal-body">
                <p>您的修改已经生效！</p>
            </div>
            <div class="modal-footer">
                <button id="confirmBtn" class="btn btn-primary"
                        onClick="window.location.reload();">确定
                </button>
            </div>
        </div>
    </div>
</div>
<div class="modal-header" style="height: 50px">
    <button type="button" class="close smaller" data-dismiss="modal"  >x</button>
    <h4 id="myModalLabel" class="smaller">Task详细信息</h4>
</div>
<div class="modal-body">
<form id="form_${dto.taskid!}" class="form-horizontal task-form">
<fieldset>
<div id="host" class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="host">部署的机器*</label>

    <div class="controls">
        <input type="text" id="hostname" name="hostname"
               class="input-big field" value="${dto.hostname!}" disabled>
    </div>
</div>
<br>
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="taskType">选择作业类型*</label>

    <div class="controls">
        <input type="text" id="taskType"
               name="taskType" class="input-big field" value="${dto.type!}" disabled>
    </div>
</div>
<#if dto.type?exists && dto.type == "hadoop" >
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="hadoopName">hadoop用户名*</label>

    <div class="controls">
        <input type="text" class="input-big field" id="hadoopName"
               name="hadoopName" value="${dto.hadoopName!}" disabled>
    </div>
    <br>
</div>
</#if>

<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="taskName">名称*</label>

    <div class="controls">
        <input type="text" class="input-big" id="taskName"
               name="taskName" value="${dto.name!}" readonly>
    </div>
</div>
<br>

<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="crontab">Crontab*</label>

    <div class="controls">
        <input type="text" class="input-xxlarge field " id="crontab"
               name="crontab" value="${dto.crontab!}" disabled>
    </div>
</div>
<br>

<#if dto.type?exists && dto.type != "mschedule" >
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="taskCommand">命令*</label>

    <div class="controls">
        <input type="text" class="input-xxlarge field " id="taskCommand"
               name="taskCommand" value="${dto.htmlCommand!}" disabled>
    </div>
</div>
<br>
</#if>

<#if dto.type?exists && dto.type != "mschedule" >
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="proxyUser">运行身份（不可为root）*</label>

    <div class="controls">
        <input type="text" class="input-xxlarge field " id="proxyUser"
               name="proxyUser" value="${dto.proxyuser!}" disabled>
    </div>
</div>
</#if>
<br>

<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2" for="description">描述*</label>

    <div class="controls">
        <input type="text" class="input-xxlarge field " id="description"
               name="description" value="${dto.description!}" disabled>
    </div>
</div>
<br>

<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2">作业最长执行时间(分钟)*</label>

    <div class="controls">
        <input type="number" class="input-small field "
               id="maxExecutionTime" name="maxExecutionTime"
               style="text-align: right" value="${dto.executiontimeout!}" disabled>
    </div>
</div>
<br>

<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-4">自动kill执行超时实例*</label>

    <div class="controls field" id="isAutoKill">
    <#if dto.isAutoKill() >
        <input type="radio" value="1" name="isAutoKill" checked>是
        <input type="radio" value="0" name="isAutoKill">否
    <#else>
        <input type="radio" value="1" name="isAutoKill">是
        <input type="radio" value="0" name="isAutoKill" checked>否
    </#if>
    </div>
</div>
<br>

<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2">作业依赖</label>

    <div class="controls">
        <input type="text" class="input-large field " id="dependency"
               name="dependency" placeholder="dependency expression"
               value="${dto.dependencyexpr!}" disabled>
    </div>
</div>

<br>
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2">依赖最长等待时间(分钟)*</label>

    <div class="controls">
        <input type="number" class="input-small field " id="maxWaitTime"
               name="maxWaitTime" style="text-align: right"
               value=${dto.waittimeout!} disabled>
    </div>
</div>
<br>

<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-2">作业失败重试次数*</label>

    <div class="controls">
        <input type="number" class="input-small field " id="retryTimes"
               name="retryTimes" style="text-align: right"
               value=${dto.retrytimes!} disabled>
    </div>
</div>

<br>
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-4">丢弃拥塞实例*</label>

    <div class="controls field" id="iskillcongexp">
    <#if dto.iskillcongexp >
        <input type="radio" value="1" name="iskillcongexp" checked>是
        <input type="radio" value="0" name="iskillcongexp">否
    <#else>
        <input type="radio" value="1" name="iskillcongexp">是
        <input type="radio" value="0" name="iskillcongexp" checked>否
    </#if>
    </div>
</div>

<br>
<div class="control-group col-sm-12" id="isnotconcurrency">
    <label class="label label-lg label-info arrowed-right col-sm-4">上次未执行完不启动新任务*</label>

    <div class="controls field" id="isnotconcurrency">
    <#if dto.getIsnotconcurrency() >
        <input type="radio" value="1" name="isnotconcurrency" checked>是
        <input type="radio" value="0" name="isnotconcurrency">否
    <#else>
        <input type="radio" value="1" name="isnotconcurrency">是
        <input type="radio" value="0" name="isnotconcurrency" checked>否
    </#if>
    </div>
</div>

<br>
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-3">选择何时收到报警</label>

    <div class="controls">
    <#list statuses as status>
        <#if conditionStr?exists && conditionStr?contains(status.status)>
            <input type="checkbox" class="field alertCondition"
               id="alertCondition" name="${status.status!}"
               checked="checked" disabled>
               ${status.ch_status!}
        <#else>
            <input type="checkbox" class="field alertCondition"
               id="alertCondition" name="${status.status!}" disabled>
               ${status.ch_status!}
        </#if>
    </#list>
    </div>
</div>
<br>

<br>
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-3">选择报警方式</label>

    <div class="controls">
    <#list alerttypes as alerttype>
        <#if alerttypeStr?exists && alerttypeStr?contains(alerttype.status+"")>
            <input type="checkbox" class="field alertType"
               id="alertType" name="${alerttype.status!}"
               checked="checked" disabled>
               ${alerttype.ch_status!}
        <#else>
            <input type="checkbox" class="field alertType"
               id="alertType" name="${alerttype.status!}" disabled>
               ${alerttype.ch_status!}
        </#if>
    </#list>
    </div>
</div>

<br>
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-4" for="alertUser">选择报警接收人(分号分隔)</label>

    <div class="controls">
        <input type="text" class="input-large field" id="alertUser"
               name="alertUser" value="${dto.userid!}" disabled>
    </div>
</div>
<br>
<div class="control-group col-sm-12">
    <label class="label label-lg label-info arrowed-right col-sm-4" for="alertGroup">选择报警接收组(分号分隔)</label>

    <div class="controls">
        <input type="text" class="input-large field" id="alertGroup"
               name="alertGroup" value="${dto.groupid!}" disabled>
    </div>
</div>
<input type="text" class="field" style="display: none" id="creator"
       name="creator"
       value="${currentUser!}">
</fieldset>
</form>
</div>
<div class="modal-footer">
    <button id="updateBtn" class="btn btn-primary"
            onClick="action_update('${dto.getTaskid()!}')"
            data-loading-text='正在保存..'>修改
    </button>
    <button class="btn" data-dismiss="modal">关闭</button>
</div>

</div>
</div>
<script type="text/javascript">
    var userList = "", groupList = "";
    userList = userList
    <#if users??>
    <#list users as user>
        +",${user.name!}"
    </#list>
    </#if>
        ;
    groupList = groupList
    <#if groups??>
    <#list groups as group>
        +",${group.name!}"
    </#list>
    </#if>
        ;
    userList = userList.substr(1);
    groupList = groupList.substr(1);

    if($('input[type=radio]:checked','#iskillcongexp').val() === "1"){
        $("#isnotconcurrency").hide();
    }else{
        $("#isnotconcurrency").show();
    }

    $("#iskillcongexp").change(function(e){
        var checked = $('input[type=radio]:checked','#iskillcongexp').val();
        if(checked === '1'){
            $("#isnotconcurrency").hide();
        } else{
            $("#isnotconcurrency").show();
        }
    });

</script>
