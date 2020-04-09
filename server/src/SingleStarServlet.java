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
import java.sql.Statement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
	private static final long serialVersionUID = 3L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json; charset=UTF-8"); // Response mime type
		response.setCharacterEncoding("UTF-8");

		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();

			// Declare our statement
			Statement statement = dbcon.createStatement();

			// Construct a query with parameter represented by "?"
			String query = "SELECT * from stars, stars_in_movies, movies WHERE movies.id = movieId AND starId = stars.id AND stars.id = '" + id + "';";

			// Perform the query
			ResultSet rs = statement.executeQuery(query);

			JsonArray jsonArray = new JsonArray();

			// Iterate through each row of rs
			while (rs.next()) {
				// Create a JsonObject based on the data we retrieve from rs
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("star_id", rs.getString("starId"));
				jsonObject.addProperty("star_name", rs.getString("name"));
				jsonObject.addProperty("star_dob", rs.getString("birthYear"));
				jsonObject.addProperty("movie_id", rs.getString("movieId"));
				jsonObject.addProperty("movie_title", rs.getString("title"));
				jsonObject.addProperty("movie_year", rs.getString("year"));
				jsonObject.addProperty("movie_director", rs.getString("director"));
				jsonArray.add(jsonObject);
			}
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			rs.close();
			statement.close();
			dbcon.close();
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();
	}
}
