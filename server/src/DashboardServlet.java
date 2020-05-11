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
}
