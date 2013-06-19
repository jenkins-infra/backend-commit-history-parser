<html>
<head>
  <script type="text/javascript" src="http://cdn.jsdelivr.net/jquery/2.0.2/jquery.js"></script>

  <link type="text/css" rel="stylesheet" href="http://cdn.jsdelivr.net/tablesorter/2.0.5b/addons/pager/jquery.tablesorter.pager.css" />
  <link rel="stylesheet" href="http://tablesorter.com/themes/blue/style.css" type="text/css" id="" media="print, projection, screen" />
  <script type="text/javascript" src="http://cdn.jsdelivr.net/tablesorter/2.0.5b/addons/pager/jquery.tablesorter.pager.js" ></script>
  <script type="text/javascript" src="http://cdn.jsdelivr.net/tablesorter/2.0.5b/jquery.metadata.js" ></script>
  <script type="text/javascript" src="http://cdn.jsdelivr.net/tablesorter/2.0.5b/jquery.tablesorter.js" ></script>
</head>
<body>

<h1>Overall</h1>
<table id="table" class="tablesorter">
  <thead>
    <tr>
      <th>ID</th>
      <th>Votes</th>
      <th>Priority</th>
      <th>Status</th>
      <th>Resolution</th>
      <th>Type</th>
      <th>Summary</th>
    </tr>
  </thead>
  <tbody>
    <% issues.each { i -> %>
      <tr>
        <td><a href="#${i.key}">${i.key}</a></td>
        <td>${i.votes}</td>
        <td>${i.priority}</td>
        <td>${app.jiraStatus(i.status)}</td>
        <td>${app.jiraResolution(i.resolution)}</td>
        <td>${app.jiraType(i.type)}</td>
        <td><a href="https://issues.jenkins-ci.org/browse/${i.key}">${i.summary}</a></td>
      </tr>
    <% } %>
  </tbody>
</table>

<%
  cherrypicks.each { /*Ticket*/ t ->
%>
  <a name="${t}">
  <h2>${t}</h2>
  <ul>
    <% t.commits.reverse().each { c -> %>
      <li><a href="http://jenkins-ci.org/commit/core/${c}">${c}</a></li>
    <% } %>
  </ul>
<% } %>

<script>
  \$(document).ready(function()
      {
          \$("#table").tablesorter();
      }
  );
</script>
</body>
</html>