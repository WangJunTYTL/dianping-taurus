<!DOCTYPE html>
<%@ page contentType="text/html;charset=utf-8" %>
<html lang="en">
<head>
    <%@ include file="jsp/common-header.jsp" %>
    <%@ include file="jsp/common-nav.jsp" %>
    <link rel="stylesheet" type="text/css" href="css/DT_bootstrap.css">
</head>
<body data-spy="scroll">
<%@page import="org.restlet.data.MediaType,
                org.restlet.resource.ClientResource,
                com.dp.bigdata.taurus.restlet.resource.IAttemptsResource,
                com.dp.bigdata.taurus.restlet.shared.AttemptDTO,
                java.text.SimpleDateFormat" %>
<%@ page import="com.dp.bigdata.taurus.restlet.utils.IExistInHDFS" %>

<div class="container" style="margin-top: 10px">
    <div id="alertContainer" class="container">
    </div>
    <ul class="breadcrumb">
        <li><a href="./index.jsp">首页</a> <span class="divider">/</span></li>
        <li><a href="./schedule.jsp">调度中心</a> <span class="divider">/</span></li>
        <li><a href="#" class="active">任务历史</a> <span class="divider">/</span></li>
    </ul>
    <table cellpadding="0" cellspacing="0" border="0"
           class="table table-striped table-format table-hover" id="example">
        <thead>
        <tr>
            <th>ID</th>
            <th>实际启动时间</th>
            <th>实际结束时间</th>
            <th>预计调度时间</th>
            <!-- <th>IP</th> -->
            <th>返回值</th>
            <th>状态</th>
            <th>-</th>
        </tr>
        </thead>
        <tbody>
        <%
            String taskID = request.getParameter("taskID");
            String url = host + "attempt?task_id=" + taskID;
            cr = new ClientResource(url);
            cr.setRequestEntityBuffering(true);
            IAttemptsResource resource = cr.wrap(IAttemptsResource.class);
            cr.accept(MediaType.APPLICATION_XML);
            ArrayList<AttemptDTO> attempts = resource.retrieve();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (AttemptDTO dto : attempts) {
                String state = dto.getStatus();
        %>
        <tr id="<%=dto.getAttemptID()%>">
            <td><%=dto.getId()%>
            </td>
            <%if (dto.getStartTime() != null) {%>
            <td><%=formatter.format(dto.getStartTime())%>
            </td>
            <%} else {%>
            <td>NULL</td>
            <%}%>
            <%if (dto.getEndTime() != null) {%>
            <td><%=formatter.format(dto.getEndTime())%>
            </td>
            <%} else {%>
            <td>NULL</td>
            <%}%>
            <%if (dto.getScheduleTime() != null) {%>
            <td><%=formatter.format(dto.getScheduleTime())%>
            </td>
            <%} else {%>
            <td>NULL</td>
            <%}%>
            <td><%=dto.getReturnValue()%>
            </td>
            <td><%if (state.equals("RUNNING")) {%>
                <span class="label label-info"><%=state%></span>
                <%} else if (state.equals("SUCCEEDED")) {%>
                <span class="label label-success"><%=state%></span>
                <%} else {%>
                <span class="label label-important"><%=state%></span>
                <%}%>
            </td>

            <td>
                <%
                    String isExistUrl = host + "isexist/" + dto.getAttemptID();
                    ClientResource isExistCr = new ClientResource(isExistUrl);
                    isExistCr.setRequestEntityBuffering(true);
                    IExistInHDFS isExistResource = isExistCr.wrap(IExistInHDFS.class);
                    isExistCr.accept(MediaType.APPLICATION_XML);
                    String isExist = isExistResource.isExistInHDFS();
                    if (state.equals("RUNNING") || state.equals("TIMEOUT")) {%>

                <a href="#confirm" onClick="action($(this).parents('tr').attr('id'))">Kill</a>
                <%

                    if (isExist.equals("true")) {
                %>
                <a target="_blank" href="attempts.do?id=<%=dto.getAttemptID()%>&action=view-log">日志</a>
                <%} else {%>
                <a target="_blank" href="viewlog.jsp?id=<%=dto.getAttemptID()%>&status=<%=dto.getStatus()%>">日志</a>
                <%}%>
                <%
                } else {
                    if (isExist.equals("true")) {
                %>
                <a target="_blank" href="attempts.do?id=<%=dto.getAttemptID()%>&action=view-log">日志</a>
                <%} else {%>
                <a target="_blank" href="viewlog.jsp?id=<%=dto.getAttemptID()%>&status=<%=dto.getStatus()%>">日志</a>
                <%}%>
            </td>
        </tr>
        <% } }%>
        </tbody>
    </table>
</div>

<div id="confirm" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3 id="id_header"></h3>
    </div>
    <div class="modal-body">
        <p id="id_body"></p>
    </div>
    <div class="modal-footer">
        <a href="#" class="btn" data-dismiss="modal" aria-hidden="true">取消</a>
        <a href="#" class="btn btn-danger" onClick="action_ok()">确定</a>
    </div>
</div>

<script type="text/javascript" charset="utf-8" language="javascript" src="js/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf-8" language="javascript" src="js/DT_bootstrap.js"></script>
<script type="text/javascript" charset="utf-8" language="javascript" src="js/attempt.js"></script>

</body>
</html>