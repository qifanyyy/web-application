import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8"); // Response mime type
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try {
            // the following few lines are for connection pooling
            // Obtain our environment naming context
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/r");

            Connection connection = ds.getConnection();

            Statement dbMetadataQuery = connection.createStatement();
            ResultSet tableRS = dbMetadataQuery.executeQuery("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'moviedb'");

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

        try {
            // the following few lines are for connection pooling
            // Obtain our environment naming context
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/rw");
            Connection connection = ds.getConnection();

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
                ret.addProperty("retStarId", starId);
                out.write(ret.toString());
                out.close();
                resp.setStatus(201);
            } else if (db.equals("movies")) {
                boolean hasInvalidParam = false;
                String title = req.getParameter("title");
                if (title == null || (title = title.trim()).length() == 0) {
                    hasInvalidParam = true;
                }
                String yearStr = req.getParameter("year");
                int year = -1;
                try {
                    year = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    hasInvalidParam = true;
                }
                String director = req.getParameter("director");
                if (director == null || (director = director.trim()).length() == 0) {
                    hasInvalidParam = true;
                }
                String starName = req.getParameter("starName");
                if (starName == null || (starName = starName.trim()).length() == 0) {
                    hasInvalidParam = true;
                }
                String genreName = req.getParameter("genreName");
                if (genreName == null || (genreName = genreName.trim()).length() == 0) {
                    hasInvalidParam = true;
                }

                String starBirthYearStr = req.getParameter("birthYear");
                Integer starBirthYear = null;
                if (starBirthYearStr != null && (starBirthYearStr = starBirthYearStr.trim()).length() > 0) {
                    try {
                        starBirthYear = Integer.parseInt(starBirthYearStr);
                    } catch (NumberFormatException e) {
                        hasInvalidParam = true;
                    }
                }

                if (hasInvalidParam) {
                    out.write(Util.makeGeneralErrorJsonObject("please check parameter values").toString());
                    out.close();
                    resp.setStatus(400);
                    return;
                }

                assert year > 0;

                CallableStatement addMovieProcedureCall = connection.prepareCall("CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                addMovieProcedureCall.setString(1, title);
                addMovieProcedureCall.setInt(2, year);
                addMovieProcedureCall.setString(3, director);
                addMovieProcedureCall.setString(4, genreName);
                addMovieProcedureCall.setString(5, starName);
                addMovieProcedureCall.setObject(6, starBirthYear);

                addMovieProcedureCall.registerOutParameter(7, Types.BOOLEAN);
                addMovieProcedureCall.registerOutParameter(8, Types.BOOLEAN);
                addMovieProcedureCall.registerOutParameter(9, Types.BOOLEAN);
                addMovieProcedureCall.registerOutParameter(10, Types.VARCHAR);
                addMovieProcedureCall.registerOutParameter(11, Types.VARCHAR);
                addMovieProcedureCall.registerOutParameter(12, Types.INTEGER);

                addMovieProcedureCall.executeUpdate();

                boolean hasDupMovie, hasDupStar, hasDupGenre;
                String retMovieId, retStarId;
                int retGenreId;

                hasDupMovie = addMovieProcedureCall.getBoolean(7);
                hasDupStar = addMovieProcedureCall.getBoolean(8);
                hasDupGenre = addMovieProcedureCall.getBoolean(9);
                retMovieId = addMovieProcedureCall.getString(10);
                retStarId = addMovieProcedureCall.getString(11);
                retGenreId = addMovieProcedureCall.getInt(12);

                JsonObject ret = new JsonObject();
                ret.addProperty("status", "success");
                if (!hasDupMovie) {
                    ret.addProperty("db_status", "updated");
                    resp.setStatus(201);
                } else {
                    ret.addProperty("db_status", "not modified");
                    resp.setStatus(200);
                }
                ret.addProperty("hasDupMovie", hasDupMovie);
                ret.addProperty("hasDupStar", hasDupStar);
                ret.addProperty("hasDupGenre", hasDupGenre);
                ret.addProperty("retMovieId", retMovieId);
                ret.addProperty("retStarId", retStarId);
                ret.addProperty("retGenreId", retGenreId);
                out.write(ret.toString());
                out.close();
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
