<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.sql.*" %>
<%
        String update = request.getParameter("update");

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection connection =
            DriverManager.getConnection("jdbc:mysql://localhost/kamusidesktop?",
            "root", "2musuuch");
    String query = "SELECT * FROM kamusiupdates WHERE timestamp > ? " +
            "AND approved = ? ORDER by id ASC";
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setString(1, update);
    statement.setString(2, "1");
    ResultSet rs = statement.executeQuery();
    while (rs.next())
    {
      out.print(rs.getString("updatecolumn"));
      out.print("|");
      out.print(rs.getString("updatestring"));
      out.print("|");
      out.print(rs.getString("row"));
      out.print("\n");
    }
    statement.close();
    connection.close();
%>
