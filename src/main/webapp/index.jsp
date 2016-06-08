<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js"></script>
        <link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/themes/smoothness/jquery-ui.css" />
        <link rel="stylesheet" href="styles/dialog.css" type="text/css" />
        <link rel="stylesheet" href="styles/main.css" type="text/css" />
        <link rel="stylesheet" href="http://131.179.64.145/wis/css/normalize.min.css" />
        <link rel="stylesheet" href="http://131.179.64.145/wis/css/main.css" />
        <script type="text/javascript" src="questions.js"></script>
        <script type="text/javascript">
            function select() {
                var selected = $('#qa6').find(":selected").val();
                $('#question').val(selected);
                $("#qa6").val($("#qa6 option:first").val());
            }
            function escapeHtml(unsafe) {
                return unsafe
                        .replace(/&/g, "&amp;")
                        .replace(/</g, "&lt;")
                        .replace(/>/g, "&gt;")
                        .replace(/"/g, "&quot;")
                        .replace(/'/g, "&#039;");
            }
            function submit() {
                var question = $('#question').val().trim();
                if (question.length < 10) {
                    alert("Type a question before submitting, thanks!");
                    return;
                }
                $('#template-results').html("");
                $('#filling-results').html("");
                $('#query-results').html("");
                $('#tag-results').html(
                        "Finding annotations and dataset for question: <i>" + question +
                        "</i><br /><img src=\"img/processing.gif\" />");
                $( "#tabs" ).tabs({ active: 0 });
                tag(question);
            }

            function tag(question) {
                var ajax_params = {};
                ajax_params.data = {};
                ajax_params.data["question"] = question;
                ajax_params.url = "tag";
                ajax_params.type = "POST";
                ajax_params.dataType = "json";

                // Attach the success callback
                ajax_params.success = function (result) {
                    var res = "Dataset: <b>" + result.dataset + "</b><br /><br />Annotations:<br /><table><thead><tr><td>Chunk</td><td>Subject</td><td>Property</td><td>Value</td></tr></thead><tbody>";
                    for (var i = 0; i < result.annotatedChunks.length; i++) {
                        var ac = result.annotatedChunks[i];
                        if (ac.subject) {
                            res += "<tr><td>" + escapeHtml(ac.text) + "</td><td>" + escapeHtml(ac.subject) + "</td><td>" + escapeHtml(ac.property) + "</td><td>" + escapeHtml(ac.value) + "</td></tr>"
                        }
                    }
                    res += "</tbody></table>"
                    $('#tag-results').html(res);
                    $('#template-results').html(
                            "Finding template for question: <i>" + question +
                            "</i><br /><img src=\"img/processing.gif\" />");
                    $( "#tabs" ).tabs({ active: 1 });
                    template(question);
                }

                ajax_params.failure = function (errMsg) {
                    $('#tag-results').html(errMsg);
                }
                // Make the request
                $.ajax(ajax_params);
            }

            function template(question) {
                var ajax_params = {};
                ajax_params.data = {};
                ajax_params.data["question"] = question;
                ajax_params.url = "template";
                ajax_params.type = "POST";
                ajax_params.dataType = "json";

                // Attach the success callback
                ajax_params.success = function (result) {
                    if (!result) {
                        $('#template-results').html("The system could not find a template for this question");
                        return;
                    }
                    var res = "Regex: <b>" + result.expression + "</b><br /><br />Sparql:<br />";
                    res += escapeHtml(result.template).replace(/(\r\n|\n|\r)/g, "<br />");
                    $('#template-results').html(res);
                    $('#filling-results').html(
                            "Filling out template for question: <i>" + question +
                            "</i><br /><img src=\"img/processing.gif\" />");
                    $( "#tabs" ).tabs({ active: 0 });
                    fill(question);
                }

                ajax_params.failure = function (errMsg) {
                    $('#template-results').html(errMsg);
                }
                // Make the request
                $.ajax(ajax_params);
            }

            function fill(question) {
                var ajax_params = {};
                ajax_params.data = {};
                ajax_params.data["question"] = question;
                ajax_params.url = "fillout";
                ajax_params.type = "POST";
                ajax_params.dataType = "json";

                // Attach the success callback
                ajax_params.success = function (result) {
                    var res = "Sparql:<br /><br />";
                    res += escapeHtml(result).replace(/(\r\n|\n|\r)/g, "<br />");
                    $('#filling-results').html(res);
                    $('#query-results').html(
                            "Executing the Sparql query using endpoint http://cubeqa.aksw.org/sparql" +
                            "<br /><img src=\"img/processing.gif\" />");
                    $( "#tabs" ).tabs({ active: 3 });
                    query(question);

                }

                ajax_params.failure = function (errMsg) {
                    $('#filling-results').html(errMsg);
                }
                // Make the request
                $.ajax(ajax_params);
            }

            function query(question) {
                var ajax_params = {};
                ajax_params.data = {};
                ajax_params.data["question"] = question;
                ajax_params.url = "query";
                ajax_params.type = "POST";
                ajax_params.dataType = "json";

                // Attach the success callback
                ajax_params.success = function (result) {
                    var res = "";
                    for (var i = 0; i < result.length; i++) {
                        res += escapeHtml(result[i]) + "<br />";
                    }
                    $('#query-results').html(res);
                }

                ajax_params.failure = function (errMsg) {
                    $('#query-results').html(errMsg);
                }
                // Make the request
                $.ajax(ajax_params);
            }

            $(document).ready(function () {
                var qa6 = $('#qa6');
                var i = 1;
                $.each(test_questions, function (key, value) {
                    qa6
                            .append($("<option></option>")
                                    .attr("value", value)
                                    .text("TE" + i + ") " + value));
                    i = i + 1;
                });
                i = 1;
                $.each(training_questions, function (key, value) {
                    qa6
                            .append($("<option></option>")
                                    .attr("value", value)
                                    .text("TR" + i + ") " + value));
                    i = i + 1;
                });
            });
            $(function () {
                $("#tabs").tabs();
            });
        </script>
        <script>
            (function (i, s, o, g, r, a, m) {
                i['GoogleAnalyticsObject'] = r;
                i[r] = i[r] || function () {
                    (i[r].q = i[r].q || []).push(arguments)
                }, i[r].l = 1 * new Date();
                a = s.createElement(o),
                        m = s.getElementsByTagName(o)[0];
                a.async = 1;
                a.src = g;
                m.parentNode.insertBefore(a, m)
            })(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');

            //ga('create', 'UA-68452078-1', 'auto');
            //ga('send', 'pageview');
        </script>           
    </head>

    <body style="margin-bottom: 200px;">    
        <div class="header-container">
            <header class="wrapper clearfix">
                <table border="0" cellspacing="20px" cellpadding="10px">
                    <tr>
                        <td><img src="http://yellowstone.cs.ucla.edu/wis/img/logo1.jpg" width="200px" alt="ScAI"> </td>
                        <td>    <div class="title-container">
                                <h1 class="title">QA<sup>3</sup>: Statistical Question Answering over RDF Cubes</h1>
                            </div>
                        </td>
                    </tr>
                </table>
                <%--
                <div class="nav-container">
                    <nav>
                        <ul>
                            <li><a href="about.jsp">About</a></li>
                            <li><a href="index.jsp">Demo</a></li>
                            <li><a href="qald.jsp">Experiments</a></li>
                        </ul>
                    </nav>
                </div>
                --%>
            </header>
        </div>  
        <div style="margin-top:50px; margin-left: auto; margin-right: auto; width: 1200px; font-size:14px; line-height: 16px; position:relative;">
            <input id="question" class="large" style="width: 89%" placeholder="Type your question or select one question below"/>
            <input type="button" class="large" style="width: 9%" id="submitb" value="Submit" onclick="javascript:submit()"><br />
            <select id="qa6" class="large" style="width: 99%; margin-top: 5px" onchange="javascript:select()">
                <option></option>
            </select>
        </div>
        <div id="tabs" style="margin-top: 20px">
            <ul>
                <li><a href="#tag-results">Step 1</a></li>
                <li><a href="#template-results">Step 2</a></li>
                <li><a href="#filling-results">Step 3</a></li>
                <li><a href="#query-results">Answer</a></li>
            </ul>
            <div id="filling-results"></div>
            <div id="template-results"></div>
            <div id="tag-results"></div>
            <div id="query-results"></div>
        </div>        
    </body>
</html>

