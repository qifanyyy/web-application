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

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = document.getElementById("star_info");

    if (resultData[0]["star_dob"] == null) resultData[0]["star_dob"] = "N/A";

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.innerHTML += "<h3>" + resultData[0]["star_name"] + "</h3>" +
        "<p>Date of Birth: " + resultData[0]["star_dob"] + "</p>";
    document.querySelector('title').innerText += resultData[0]["star_name"];

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = document.getElementById("movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        // Add a link to single-movie.html with id passed with GET url parameter
        let rowHTML = '<tr><th><a href=single-movie.html?id=' + resultData[i]['movie_id'] + '>'
            + resultData[i]["movie_title"] + // display movie_title for the link text
            "</a></th><th>" + resultData[i]["movie_year"] + "</th><th>" + resultData[i]["movie_director"] + "</th></tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.innerHTML += rowHTML;
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Makes the HTTP GET request and registers on success callback function handleResult
fetch(`api/single-star?id=${getParameterByName('id')}`, {  // getParameterByName defined in util.js
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
})

setUpOnCheckout()
setUpLogOutBtn()
setUpSearchForm()