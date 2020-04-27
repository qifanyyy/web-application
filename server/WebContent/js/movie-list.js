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
    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData);  // util.js
        return;
    }

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    // let movieTableBodyElement = jQuery("#movie_table_body");
    let movieTableBody = document.getElementById('movie_table_body');
    let movieArray = resultData['movies'];
    let customer = resultData['customer'];
    if (customer !== null) {
        document.getElementById('customer-first-name').innerText = `Welcome, ${customer['firstName']}`;
    }
    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < movieArray.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = `<tr>
<th><a href="single-movie.html?id=${movieArray[i]['movieId']}">${movieArray[i]["movieTitle"]}</a></th>
<th>${movieArray[i]["movieYear"]}</th>
<th>${movieArray[i]["movieDirector"]}</th>
<th>${movieArray[i]["movieGenres"]}</th><th>`;

        let starData = movieArray[i]['movieStars'];
        // Iterate through starData, no more than 3 entries
        for (let i = 0; i < starData.length; i++) {
            if (i !== 0) rowHTML += ", ";
            rowHTML += `<a href="single-star.html?id=${starData[i]["starId"]}">${starData[i]["starName"]}</a>`;
        }
        rowHTML += `</th><th>${movieArray[i]["movieRating"]}</th></tr>`;

        // Append the row created to the table body, which will refresh the page 
        // movieTableBodyElement.append(rowHTML);
        movieTableBody.innerHTML += rowHTML;

    }
    if(movieTableBody.innerHTML == ""){
        movieTableBody.innerHTML="<tr><th>No Result</th><th></th><th></th><th></th><th></th><th></th></tr>";
    }

    for (let i = 1; i < 10; ++i) {
        document.getElementById('page').innerHTML += `<li><a href="movie-list.html?page=${i}">${i}</a></li>`
    }

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

fetch(`api/movies?title=${getParameterByName('title')}&year=${getParameterByName('year')}&director=${getParameterByName('director')}&star=${getParameterByName('star')}&genre=${getParameterByName('genre')}&alnum=${getParameterByName('alnum')}&sort=${getParameterByName('sort')}&page=${getParameterByName('page')}&display=${getParameterByName('display')}`, {  // getParameterByName defined in util.js
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
