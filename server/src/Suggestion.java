
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet("/suggestion")
public class Suggestion extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
    
    public Suggestion() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			response.setContentType("application/json; charset=UTF-8"); // Response mime type
			response.setCharacterEncoding("UTF-8");

			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			
			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}	
			

			// TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars

			StringBuilder fulltext = new StringBuilder();
			String[] fullTextArray = query.split(" ");
			for (String s : fullTextArray) fulltext.append("+").append(s).append("* ");


			PreparedStatement getTitle = null;
			String titleQuery = "SELECT * FROM movies WHERE match(title) against (? IN BOOLEAN MODE) LIMIT 10; ";



			Connection con = dataSource.getConnection();

			getTitle = con.prepareStatement(titleQuery);
			getTitle.setString(1, fulltext.toString());

			System.out.println("query:" + getTitle);
			ResultSet rs = getTitle.executeQuery();

			if (!rs.next()) {
				response.getWriter().write(jsonArray.toString());
				con.close();
				return;
			}


			while (rs.next()){
				jsonArray.add(generateJsonObject(rs.getString("id"), rs.getString("title")));
			}

			rs.close();
			con.close();

			
			response.getWriter().write(jsonArray.toString());
		} catch (Exception e) {
			response.getWriter().write(Util.exception2Json(e).toString());
			response.setStatus(500);
		}
	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "heroID": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String movieID, String title) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", title);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("movieID", movieID);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}
