<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.sql.*" %>
<%
        String update = request.getParameter("update");
    String row = request.getParameter("row");
    String column = request.getParameter("column");
    long timestamp = java.util.Calendar.getInstance().getTimeInMillis();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection connection =
            DriverManager.getConnection("jdbc:mysql://localhost/kamusidesktop?",
            "root", "2musuuch");
    String query = "INSERT INTO kamusiupdates " +
            "(timestamp, updatecolumn, updatestring, row) " +
            "VALUES (?, ?, ?, ?)";
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setString(1, String.valueOf(timestamp));
    statement.setString(2, column);
    statement.setString(3, update);
    statement.setString(4, row);
    statement.executeUpdate();
    statement.close();
    connection.close();
    out.print(update);
%>
