/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let pageTitleElement = document.getElementById("title_info");
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    let title = resultData["movie_title"] + " (" + resultData["movie_year"] + ")";
    pageTitleElement.innerText += title;
    movieInfoElement.append(title);

    console.log("handleResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "<tr><th>" + resultData["movie_director"] + "</th><th>" + resultData["movie_genre"] + "</th><th>" + resultData["movie_rating"] + "</th><th>";
    let starData = JSON.parse(resultData["movie_star"]);
    // Iterate through starData
    for (let i = 0; i < starData.length; i++) {
        if (i !== 0) rowHTML += ", ";
        rowHTML +=
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + starData[i]["star_id"] + '">'
            + starData[i]["star_name"] +     // display star name for the link text
            '</a>';
    }
    rowHTML += "</th></tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});