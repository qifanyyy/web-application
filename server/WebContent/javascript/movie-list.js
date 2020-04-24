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
    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    // let movieTableBodyElement = jQuery("#movie_table_body");
    let movieTableBody = document.getElementById('movie_table_body');

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = '<tr><th><a href=' + resultData[i]['movieId'] + '"../single-movie.html?id=">'// Add a link to single-movie.html with id passed with GET url parameter
            + resultData[i]["movieTitle"] + // display movie_title for the link text
            "</a></th><th>" + resultData[i]["movieYear"] + "</th><th>" + resultData[i]["movieDirector"] + "</th><th>" +
            resultData[i]["movieGenres"] + "</th><th>";

        let starData = resultData[i]['movieStars'];
        // Iterate through starData, no more than 3 entries
        for (let i = 0; i < starData.length; i++) {
            if (i !== 0) rowHTML += ", ";
            rowHTML +=
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href=' + starData[i]["starId"] + '"../single-star.html?id=">'
            + starData[i]["starName"] +     // display star name for the link text
            '</a>';
        }
        rowHTML += "</th><th>" + resultData[i]["movieRating"] + "</th></tr>";

        // Append the row created to the table body, which will refresh the page
        // movieTableBodyElement.append(rowHTML);
        movieTableBody.innerHTML += rowHTML;
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

fetch('api/movies', {
    headers: {
        'content-type': 'application/json;charset=UTF-8'
    },
    method: 'GET'
}).then(response => response.json(), error => console.error(error)).then(json => {
    if (document.readyState === 'complete' || document.readyState === 'interactive') {
        handleMovieResult(json);
    } else {
        window.addEventListener('DOMContentLoaded', e => handleMovieResult(json));
    }
});
