/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = '<tr><th><a href="single-movie.html?id=' + resultData[i]['movieId'] + '">'// Add a link to single-movie.html with id passed with GET url parameter
            + resultData[i]["movieTitle"] + // display movie_title for the link text
            "</a></th><th>" + resultData[i]["movieYear"] + "</th><th>" + resultData[i]["movieDirector"] + "</th><th>" +
            resultData[i]["movieGenres"] + "</th><th>";
        console.log(resultData[i]["movieStars"]);
        let starData = resultData[i]['movieStars'];
        // Iterate through starData, no more than 3 entries
        for (let i = 0; i < starData.length; i++) {
            if (i !== 0) rowHTML += ", ";
            rowHTML +=
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + starData[i]["starId"] + '">'
            + starData[i]["starName"] +     // display star name for the link text
            '</a>';
        }
        rowHTML += "</th><th>" + resultData[i]["movieRating"] + "</th></tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});