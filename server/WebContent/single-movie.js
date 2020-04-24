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
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData);  // util.js
        return;
    }

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let pageTitleElement = document.getElementById("title_info");
    let movieInfoElement = document.getElementById('movie_info');

    // append two html <p> created to the h3 body, which will refresh the page
    let title = resultData["movie_title"] + " (" + resultData["movie_year"] + ")";
    pageTitleElement.innerText += title;
    movieInfoElement.innerText = title;

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = document.getElementById("movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "<tr><th>" + resultData["movie_director"] + "</th><th>" + resultData["movie_genre"] + "</th><th>" +
        resultData["movie_rating"] + "</th><th>";
    let starData = resultData["movie_star"];
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
    movieTableBodyElement.innerHTML += rowHTML;
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleResult
fetch(`api/single-movie?id=${getParameterByName('id')}`, {  // getParameterByName defined in util.js
    headers: {
        'content-type': 'application/json;charset=UTF-8'
    },
    method: 'GET'
}).then(response => response.json(), error => console.error(error)).then(json => {
    if (document.readyState === 'complete' || document.readyState === 'interactive') {
        handleResult(json);
    } else {
        window.addEventListener('DOMContentLoaded', e => handleResult(json));
    }
});
