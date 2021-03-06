<!DOCTYPE html >
<html >
<head>
    <#-- scrpit & style -->
    <!-- basic styles -->
    <script type="text/javascript" src="${rc.contextPath}/resource/js/lib/jquery-1.9.1.min.js"></script>
    <link href="${rc.contextPath}/lib/ace/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="${rc.contextPath}/lib/ace/js/ace-extra.min.js"></script>
    <link rel="stylesheet" href="${rc.contextPath}/lib/ace/css/font-awesome.min.css"/>
    <script src="${rc.contextPath}/lib/ace/js/ace-elements.min.js"></script>
    <script src="${rc.contextPath}/lib/ace/js/ace.min.js"></script>
    <script src="${rc.contextPath}/lib/ace/js/bootbox.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/resource/js/lib/raphael.2.1.0.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/resource/js/lib/justgage.1.0.1.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/resource/js/lib/Chart.js"></script>
    <!-- page specific plugin scripts -->
    <script src="${rc.contextPath}/lib/ace/js/jquery.dataTables.min.js"></script>
    <script src="${rc.contextPath}/lib/ace/js/jquery.dataTables.bootstrap.js"></script>
    <script src="${rc.contextPath}/lib/ace/js/jquery.validate.min.js"></script>
    <!-- page specific plugin styles -->

    <!-- fonts -->
    <script src="${rc.contextPath}/lib/ace/js/bootstrap.min.js"></script>
    <link rel="stylesheet" href="${rc.contextPath}/lib/ace/css/ace-fonts.css"/>

    <!-- ace styles -->

    <link rel="stylesheet" href="${rc.contextPath}/lib/ace/css/ace.min.css"/>
    <link rel="stylesheet" href="${rc.contextPath}/lib/ace/css/ace-rtl.min.css"/>
    <link rel="stylesheet" href="${rc.contextPath}/lib/ace/css/ace-skins.min.css"/>
    <script src="${rc.contextPath}/static/js/login.js"></script>
    <style>
        .light-login{background:#dfe0e2 url('${rc.contextPath}/img/taurus.jpg') no-repeat}
    </style>

</head>
<body class="login-layout light-login">

<div class="main-container">
    <div class="main-content">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1">
                <div class="login-container">
                    <div class="center">
                        <h1>
                            <i class="ace-icon icon-leaf green"></i>
                            <span class="red">Taurus</span>
                            <span class="white" id="id-text2">任务调度系统</span>
                        </h1>
                        <h4 class="blue" id="id-company-text">&copy; dianping.com</h4>
                        <br>

                    </div>


                    <div class="space-6"></div>
                    <div id="alertContainer" class="container">
                    </div>
                    <div class="position-relative">
                        <div id="login-box" class="login-box visible widget-box no-border">

                            <div class="widget-body">

                                <div class="widget-main">
                                    <h4 class="header blue lighter bigger">
                                        <i class="ace-icon icon-coffee green"></i>
                                        Please Enter Your Information
                                    </h4>

                                    <div class="space-6"></div>

                                    <form id="signupForm">
                                        <fieldset>


                                            <label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input type="text" class="form-control required"  id="username"
                                                                   placeholder="Domain accout"/>
															<i class="ace-icon icon-user green"></i>
														</span>
                                            </label>

                                            <label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input type="password" id="password" class="form-control required"
                                                                   placeholder="Password"
                                                                   onKeyPress="EnterTo('${url!}')"/>
															<i class="ace-icon icon-lock"></i>
														</span>
                                            </label>

                                            <div class="space"></div>

                                            <div class="clearfix">
                                                <button type="button" class="width-35 pull-right btn btn-sm btn-primary"
                                                        onClick="login('${url!}')">
                                                    <i class="ace-icon icon-key"></i>
                                                    <span class="bigger-110">Sign in</span>
                                                </button>
                                            </div>

                                            <div class="space-4"></div>
                                        </fieldset>
                                    </form>


                                </div>
                                <!-- /.widget-main -->


                            </div>
                            <!-- /.widget-body -->
                        </div>
                        <!-- /.login-box -->


                        <!-- /.forgot-box -->


                        <!-- /.signup-box -->
                    </div>
                    <!-- /.position-relative -->


                </div>
            </div>
            <ul>
            <#if switchUrls?exists>
            <#list switchUrls as switchUrl>
                <li>
                    <a href="${switchUrl!}">
                    <#if switchUrl?contains("alpha")>
                    Alpha 环境
                    <#elseif switchUrl?contains("beta")>
                    Beta 环境
                    <#elseif switchUrl?contains("ppe")>
                    PPE 环境
                    <#else>
                    线上环境
                    </#if>
                    </a>
                </li>
            </#list>
            </#if>

            </ul>
            <!-- /.col -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /.main-content -->
</div>
<!-- /.main-container -->

<!-- /container -->

	
	<script>
	    $().ready(function() {
	        $("#signupForm").validate();
	    });
	</script>

</body>

</html>