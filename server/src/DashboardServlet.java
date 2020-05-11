import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8"); // Response mime type
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            Statement dbMetadataQuery = connection.createStatement();
            ResultSet tableRS = dbMetadataQuery.executeQuery(
                    "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'moviedb'"
            );

            JsonObject ret = new JsonObject();
            ret.addProperty("status", "success");
            JsonArray tableArray = new JsonArray();
            while (tableRS.next()) {
                JsonObject tableInfoJson = new JsonObject();

                String tableName = tableRS.getString(1);
                tableInfoJson.addProperty("name", tableName);

                JsonArray columnInfoArray = new JsonArray();

                PreparedStatement tableMetadataQuery = connection.prepareStatement(
                        "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'moviedb'"
                );
                tableMetadataQuery.setString(1, tableName);
                ResultSet columnInfoRS = tableMetadataQuery.executeQuery();
                while (columnInfoRS.next()) {
                    JsonObject columnInfoObject = new JsonObject();
                    columnInfoObject.addProperty("columnName", columnInfoRS.getString(1));
                    columnInfoObject.addProperty("columnType", columnInfoRS.getString(2));
                    columnInfoArray.add(columnInfoObject);
                }

                tableInfoJson.add("columns", columnInfoArray);
                tableArray.add(tableInfoJson);
                tableMetadataQuery.close();
            }
            dbMetadataQuery.close();
            ret.add("tables", tableArray);
            out.write(ret.toString());
            resp.setStatus(200);
        } catch (Exception e) {
            out.write(Util.exception2Json(e).toString());
            resp.setStatus(500);
        }
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8"); // Response mime type
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String db = req.getParameter("db");
        int nextStarId = 0;
        int nextMovieId = 0;

        try (Connection connection = dataSource.getConnection()) {
            if (db.equals("stars")) {
                String name = req.getParameter("name");

                if (name == null || (name = name.trim()).length() == 0) {
                    out.write(Util.makeGeneralErrorJsonObject("invalid value for parameter name").toString());
                    out.close();
                    resp.setStatus(400);
                    return;
                }

                String birthYearStr = req.getParameter("birthYear");
                Integer birthYear = null;
                if (birthYearStr != null && (birthYearStr = birthYearStr.trim()).length() > 0) {
                    try {
                        birthYear = Integer.parseInt(birthYearStr);
                    } catch (NumberFormatException e) {
                        out.write(Util.exception2Json(e).toString());
                        out.close();
                        resp.setStatus(400);
                        return;
                    }
                }

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COALESCE(MAX(id), 'ws00000000') FROM stars WHERE id LIKE 'ws%'");
                if (resultSet.next()) {
                    nextStarId = Integer.parseInt(resultSet.getString(1).substring(2)) + 1;
                }
                statement.close();

                String starId = String.format("ws%08d", nextStarId);
                PreparedStatement insertStarStatement = connection.prepareStatement(
                        "INSERT INTO stars VALUES (?, ?, ?)"
                );
                insertStarStatement.setString(1, starId);
                insertStarStatement.setString(2, name);
                insertStarStatement.setObject(3, birthYear);
                insertStarStatement.executeUpdate();
                insertStarStatement.close();
                JsonObject ret = new JsonObject();
                ret.addProperty("status", "success");
                out.write(ret.toString());
                out.close();
                resp.setStatus(201);
            } else if (db.equals("movies")) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COALESCE(MAX(id), 'ws00000000') FROM stars WHERE id LIKE 'ws%'");
                if (resultSet.next()) {
                    nextStarId = Integer.parseInt(resultSet.getString(1).substring(2)) + 1;
                }
                statement.close();
                statement = connection.createStatement();
                resultSet = statement.executeQuery("SELECT COALESCE(MAX(id), 'wm00000000') FROM stars WHERE id LIKE 'wm%'");
                if (resultSet.next()) {
                    nextMovieId = Integer.parseInt(resultSet.getString(1).substring(2)) + 1;
                }
                statement.close();

                // TODO: add movie using stored procedure
            } else {
                out.write(Util.makeGeneralErrorJsonObject("invalid value for parameter db").toString());
                out.close();
                resp.setStatus(400);
            }
        } catch (Exception e) {
            resp.setStatus(500);
            out.write(Util.exception2Json(e).toString());
            out.close();
        }
    }
}
