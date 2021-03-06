<!DOCTYPE html >
<html >
<head>

	<title>Taurus</title>

	<#include "segment/html_header.ftl">
	<!-- jquery-ui.js放在bootstrap.js前面，否则atip tooltip失效 -->
    <script type="text/javascript" src="${rc.contextPath}/js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/static/js/jquery.autocomplete.js"></script>
    <#include "segment/html_header2.ftl">
    <link rel="stylesheet" href="${rc.contextPath}/css/jquery-ui.min.css"/>
    <link href="${rc.contextPath}/css/bwizard.min.css" rel="stylesheet"/>

    <style>
        label.error{margin-left: 10px; color: red;}
        label.success{margin-left: 10px; color: green;}
        .autocomplete-suggestions { border: 1px solid #DDD; background: #FFF; overflow: auto; }
        .autocomplete-suggestion { padding: 2px 5px; white-space: nowrap; overflow: hidden; }
        .autocomplete-selected { background: rgba(113,182,243,0.75); }
        .autocomplete-suggestions strong { font-weight: normal; color: #DCA43B; }
        .autocomplete-group { padding: 2px 5px; }
        .autocomplete-group strong { display: block; border-bottom: 1px solid #111; }
    </style>
</head>

<body>
<#include "segment/header.ftl">
<#include "segment/left.ftl">

<div class="main-content" style="opacity: 1;">

<div class="breadcrumbs" id="breadcrumbs">
    <script type="text/javascript">
        try {
            ace.settings.check('breadcrumbs', 'fixed')
        } catch (e) {
        }
    </script>
    <ul class="breadcrumb">
        <li>
            <i class="icon-home home-icon"></i>
            <a href="${rc.contextPath}/index">HOME</a>
        </li>
        <li class="active">
            <a href="${rc.contextPath}/task">新建任务</a>
        </li>
    </ul>
</div>
<div class="page-content">
<div id="wizard">
<ol>
    <li>作业部署</li>
    <li>基本设置</li>
    <li>其他设置</li>
</ol>
<div id="deploy">
    <form id="deploy-form" class="form-horizontal">
        <fieldset>
            <legend>部署设置</legend>
            <div class="control-group">
                <label class="label label-lg label-info arrowed-right col-sm-1" for="taskType">作业类型*</label>

                <div class="controls col-sm-10">
                    <select id="taskType" name="taskType" class="input-big  field" style="width: 300px">
                        <option>default</option>
                        <option>mschedule</option>
                        <#--<option>hadoop</option>-->
                    </select>
                    <a href="${rc.contextPath}/about#config" class="atip" data-toggle="tooltip" data-placement="top"
                       data-original-title="hadoop: 需要访问hadoop的作业。这种类型的作业，taurus会管理作业的hadoop ticket的申请和销毁。
                            default: 上述两种类型以外所有类型。">帮助</a>
                </div>
            </div>
            <br>

            <div id="jarAddress" style="display:none;">
                <div class="control-group">
                    <label class="label label-lg label-info arrowed-right col-sm-1" for="taskUrl">Jar包ftp地址*</label>

                    <div class="controls col-sm-10">
                        <input type="text" class="input-xxlarge field" id="taskUrl" name="taskUrl"
                               placeholder="ftp://10.1.1.81/{project-name}/{date}/{jarName}" style="width: 300px">
                    </div>
                </div>
            </div>
            <br>

            <div id="hadoopName" style="display:none;">
                <div class="control-group">
                    <label class="label label-lg label-info arrowed-right col-sm-2" for="hadoopName">hadoop用户名*</label>

                    <div class="controls col-sm-9">
                        <input type="text" class="input-large field" id="hadoopName" name="hadoopName"
                               placeholder="kerberos principle (wwwcron)">
                        <a href="${rc.contextPath}/about#config" class="atip" data-toggle="tooltip" data-placement="top"
                           data-original-title=" hadoop类型的作业，需要提供一个用于访问hadoop的principle name。
                                为此，taurus需要读取这个principle的keytab文件，一般情况下这个keytab已经放到相应的目录。
                                如果你不确定这一点，请联系我们。">帮助</a>

                    </div>
                    <br>
                    <br>
                </div>
            </div>

            <div id="hostname" class="control-group">
                <label class="label label-lg label-info arrowed-right col-sm-1" for="hostname">部署的机器*</label>

                <div class="controls col-sm-10">
                    <select id="hostname" name="hostname" class="input-big field" style="width: 300px">
                        <option selected="selected">${ip!}</option>
                    <#if hosts??>
                    <#list hosts as host>
                	<#if host.isConnected() == true >
						<option>${host.ip!}</option>
					</#if>
                    </#list>
                    </#if>
                    </select>
                    <a class="atip" data-toggle="tooltip" data-placement="top"
                       data-original-title="如果你要部署的主机ip不在这里，说明agent机器出现了故障或者主机ip上没有部署agent，请联系运维哥哥">提示</a>
                </div>
            </div>
            <div id="hostlist" class="control-group" style="display:none;">
                <label class="label label-lg label-info arrowed-right col-sm-1" for="hostname">部署的机器*</label>

                <div class="controls col-sm-4">
                    <input type="text" class="input-xxlarge field" id="hostlist" name="hostlist" placeholder="可以不用填,ip0:port0,ip1:port1..." style="width: 300px">
                </div>
            </div>
        </fieldset>
    </form>
</div>

<div id="base">
    <form id="basic-form" class="form-horizontal">
        <fieldset>
            <legend>必要设置</legend>
            <div class="control-group">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="taskName">名称*</label>

                <div class="controls col-sm-9">
                    <input type="text" class="input-xxlarge field" id="taskName" name="taskName" value="${name!}"
                           placeholder="作业的名称，可以作为被依赖的对象，不可修改">
                </div>
                <br>
                <br>
            </div>
            <div class="control-group" style='display:none'>
                <label class="label label-lg label-info arrowed-right col-sm-2" for="taskName">应用名称*</label>

                <div class="controls controls col-sm-9">
                    <input type="text" class="input-xxlarge field" id="appName" name="appName" value="${name!}">
                </div>
                <br>
                <br>
            </div>
            <div id="mainClassCG" class="control-group">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="mainClass">MainClass*</label>

                <div class="controls controls col-sm-9">
                    <input type="text" class="input-xxlarge field required" id="mainClass" name="mainClass"
                           placeholder="mainClass">
                </div>
                <br>
                <br>
            </div>
            <div class="control-group">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="crontab">Crontab*</label>

                <div class="controls col-sm-9">
                    <input type="text" class="input-xxlarge field" id="crontab" name="crontab" value="0 0 0 * * ?">
                    <a href="${rc.contextPath}/about#crontab" target="view_window">帮助</a>
                </div>
                <br>
                <br>

            </div>

            <div class="control-group" id="taskCommand">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="taskCommand">命令*</label>

                <div class="controls col-sm-9">
                    <input type="text" class="input-xxlarge field" id="taskCommand" name="taskCommand"
                           placeholder="执行作业的命令,命令结尾不要使用'&'或';'">
                    <a class="atip" data-toggle="tooltip" data-placement="top"
                       data-original-title="注意，命令结尾不要使用'&'或';' 否则会失败哦～ 请保持命令结尾没有这些字符">提示</a>
                    <br/><span>提示:已部署的作业文件的路径为${path!}</span>
                </div>
                <br>
                <br>
            </div>

            <div id="beanCG" class="control-group">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="taskCommand"></label>

                <div class="controls col-sm-9">
                    <button id="addNewBeanbtn" class="btn btn-small">增加Bean</button>
                    <button id="rmBeanbtn" class="btn btn-small" disabled>删除Bean</button>
                </div>
                <br>
                <br>
            </div>

            <div class="control-group" id="proxyUser">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="proxyUser">运行身份（不可为root）*</label>

                <div class="controls col-sm-9" id="hadoopUser">
                    <input type="text" class="input-xxlarge field" id="proxyUser" name="proxyUser"
                           placeholder="执行作业的用户身份">
                </div>

                <div class="controls col-sm-9" id="defaultUser">
                    <input type="text" class="input-xxlarge field" id="proxyUser" name="proxyUser"
                           placeholder="执行作业的用户身份" value="nobody" disabled="disabled">
                    <a class="atip" data-toggle="tooltip" data-placement="top"
                       data-original-title="如果你非要以其他身份运行，请联系李明【kirin.li@dianping.com】">帮助</a>
                </div>
                <br>
                <br>
            </div>
            <div class="control-group">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="description">描述*</label>

                <div class="controls col-sm-9">
                    <input type="text" class="input-xxlarge field" id="description" name="description"
                           placeholder="请尽可能用中文描述作业的用途">
                </div>
                <br>
                <br>
            </div>
            <input type="text" class="field" style="display:none" id="creator" name="creator" value="${currentUser!}">
        </fieldset>
    </form>
</div>
<div id="extention">
    <form id="extended-form" class="form-horizontal">
        <fieldset>
            <legend>可选设置</legend>
            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2"
                       for="maxExecutionTime">作业最长执行时间(分钟)*</label>
                       
                <div class="controls">
                    <input type="number" class="input-small field" id="maxExecutionTime" name="maxExecutionTime"
                           style="text-align:right" value=60>
                    <a href="${rc.contextPath}/about#config" target="view_window">帮助</a>
                </div>
            </div>
            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="multiInstance">自动kill执行超时实例*</label>

                <div class="controls field" id="isAutoKill">
                    <input type="radio" value="1" name="isAutoKill" checked> 是
                    <input type="radio" value="0" name="isAutoKill"> 否
                    <span class="label">不要轻易修改，除非你确定其含义：<a href="${rc.contextPath}/about#config" target="view_window">帮助</a></span>
                </div>
            </div>

            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="dependency">作业依赖</label>

                <div class="controls">
                    <input type="text" class="input-large field" id="dependency" name="dependency"
                           placeholder="dependency expression" value="">
                    <a href="${rc.contextPath}/about#config" target="view_window">帮助</a>
                </div>
            </div>
            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="maxWaitTime">依赖最长等待时间(分钟)*</label>

                <div class="controls">
                    <input type="number" class="input-small field" id="maxWaitTime" name="maxWaitTime"
                           style="text-align:right" value=60>
                    <a href="${rc.contextPath}/about#config" target="view_window">帮助</a>
                </div>
            </div>

            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="retryTimes">作业失败重试次数*</label>

                <div class="controls">
                    <input type="number" class="input-small field" id="retryTimes" name="retryTimes"
                           style="text-align:right" value=0>
                    <a href="${rc.contextPath}/about#config" target="view_window">帮助</a>
                </div>
            </div>
            
            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2" for="multiInstance">丢弃拥塞实例*</label>

                <div class="controls field" id="iskillcongexp">
                    <input type="radio" value="1" name="iskillcongexp"> 是
                    <input type="radio" value="0" name="iskillcongexp" checked> 否
                    <span class="label">不要轻易修改，除非你确定其含义：<a href="${rc.contextPath}/about#config" target="view_window">帮助</a></span>
                </div>
            </div>

            <div id="isnotconcurrency" >
                <div class="control-group">
                    <label class="label label-lg label-info arrowed-right col-sm-2" for="isnotconcurrency">上次未执行完不启动新任务*</label>

                    <div class="controls field" id="isnotconcurrency">
                        <input type="radio" value="1" name="isnotconcurrency" checked> 是
                        <input type="radio" value="0" name="isnotconcurrency"> 否
                        <span class="label">不要轻易修改，除非你确定其含义：<a href="${rc.contextPath}/about#config" target="view_window">帮助</a></span>
                    </div>
                </div>
            </div>

            <p><span class="label" style="margin-top:20px">注意：使用以下配置项，你需要在用户<a href="${rc.contextPath}/user">用户设置</a>填写您的联系方式</span></p>

            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2">选择何时收到报警</label>

                <div class="controls">
                <#if statuses??>
                <#list statuses as status>
	                <label><input type="checkbox" class="ace ace-checkbox-2 field alertCondition" id="alertCondition"
	                                  name="${status.status!}" <#if status.status == "FAILED" || status.status == "TIMEOUT"> checked="checked" </#if> ><span
	                            class="lbl"> ${status.ch_status!} </span></label>
                </#list>
                </#if>

                </div>
            </div>

            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg  label-info arrowed-right col-sm-2" for="alertType">选择报警方式</label>

                <div class="controls">
                <#if alerttypes??>
                    <#list alerttypes as alerttype>
                        <label><input type="checkbox" class="ace ace-checkbox-2 field alertType" id="alertType"
                                      name="${alerttype.status!}" <#if alerttype.status == 1> checked="checked" </#if> ><span
                                class="lbl"> ${alerttype.ch_status!} </span></label>
                    </#list>
                </#if>
                </div>
            </div>

            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2">选择报警接收人(分号分隔)</label>

                <div class="controls">
                    <input type="text" class="input-large field" id="alertUser" name="alertUser"
                           value="${currentUser!};">
                </div>
            </div>

            <div class="control-group col-sm-12 no-padding-left">
                <label class="label label-lg label-info arrowed-right col-sm-2">选择报警接收组(分号分隔)</label>

                <div class="controls">
                    <input type="text" class="input-large field" id="alertGroup" name="alertGroup"
                           placeholder="group name split with ;">
                </div>
            </div>
        </fieldset>
    </form>
</div>
</div>
</div>
<div class="feedTool">

    <a target="_blank" style="color: white;" href="http://wpa.qq.com/msgrd?v=3&uin=767762405&site=qq&menu=yes"><img border="0" src="${rc.contextPath}/img/qq.png"  width="80" height="80" color="white" alt="点我报错" title="点我报错"/></a>
    <a target="_blank" style="float:right; padding-right:16px;color: white;" href="javascript:close_tool()"><img border="0" src="${rc.contextPath}/img/x_alt.png"  width="20" height="20" color="white" alt="关闭挂件" title="关闭挂件"/></a>
    <p style="text-align: center; padding-right:32px;color: firebrick">点我报错</p>
</div>
<div id="confirm" class="modal fade" role="dialog"
     aria-hidden="true">
    <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h3 id="id_header"></h3>
        </div>
        <div class="modal-body">
            <p id="id_body"></p>
        </div>
        <div class="modal-footer">
        </div>
    </div>
    </div>
</div>
<script type="text/javascript">
    var userList = "", groupList = "", ipList = "";

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
	ipList = ipList
    <#if hosts??>
    <#list hosts as host>
    	+",${host.name!}"
    </#list>
    </#if>
    	;

    ipList = ipList.substr(1);
    userList = userList.substr(1);
    groupList = groupList.substr(1);
    $(".atip").tooltip();
    options = {
        delay: { show: 500, hide: 100 },
        trigger: 'click'
    };

    $(".optiontip").tooltip(options);
</script>
<script src="${rc.contextPath}/js/bwizard.js" type="text/javascript"></script>
<script src="${rc.contextPath}/lib/ace/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="${rc.contextPath}/static/js/taurus_validate.js" type="text/javascript"></script>
<script src="${rc.contextPath}/static/js/task.js" type="text/javascript"></script>
</body>
</html>