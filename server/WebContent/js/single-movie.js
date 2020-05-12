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

function addMovieToCart(movie) {
    const cartItem = {
        movieId: movie['movie_id'],
        movieTitle: movie['movie_title'],
        increment: 1
    }
    const reqBody = movieObjectToURLSearchParams(cartItem)
    const add2CartButton = document.getElementById('add-shopping-cart')

    fetch('api/cart', {
        method: 'POST',
        body: reqBody
    }).then(async response => response.json(), reason => {
        add2CartButton.classList.remove('btn-primary')
        add2CartButton.classList.add('btn-outline-danger')
        add2CartButton.innerText = 'Oops! Something goes wrong...'
        console.error(reason)
    }).then(json => {
        if (json['status'] !== 'success') {
            add2CartButton.classList.remove('btn-primary')
            add2CartButton.classList.add('btn-outline-danger')
            add2CartButton.innerText = 'Oops! Something goes wrong...'
            jsonErrorMsgHandler(json)
        } else {
            add2CartButton.classList.remove('btn-primary')
            add2CartButton.classList.add('btn-outline-primary')
            add2CartButton.innerText = 'Added'
        }
    })
    add2CartButton.innerText = 'Adding...'
    add2CartButton.disabled = true
}

function prepareAddToShoppingCartButton(movie) {
    const add2CartButton = document.getElementById('add-shopping-cart')
    add2CartButton.addEventListener('click', () => {
        addMovieToCart(movie)
    })
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    let movie = {...resultData}
    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData);  // util.js
        return;
    }

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let pageTitleElement = document.getElementById("title_info");
    let movieInfoElement = document.getElementById('movie_info');
    let posterInfoElement = document.getElementById('poster_info');

    // append two html <p> created to the h3 body, which will refresh the page
    let title = resultData["movie_title"] + " (" + resultData["movie_year"] + ")";
    pageTitleElement.innerText += title;
    movieInfoElement.innerHTML = "<b>" + title + "</b>";

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = document.getElementById("movie_table_body");
    let movieTableBodyElement2 = document.getElementById("movie_table_body2");


    // Concatenate the html tags with resultData jsonObject to create table rows

    posterInfoElement.innerHTML = '<img src="http://img.omdbapi.com/?i=' + movie['movie_id'] + '&apikey=49d674a2">'


    let rowHTML = "<tr><th>" + resultData["movie_director"] + "</th><th>";
    let rowHTML2 = "<tr><th>";





        for (let i = 0; i < resultData["movie_genre"].length; i++) {
            if (i !== 0) rowHTML += ", ";
            rowHTML += '<a href="movie-list.html?genre=' + resultData["movie_genre"][i] + '">' + resultData["movie_genre"][i] + '</a>';
        }

        rowHTML +="</th><th>" + resultData["movie_rating"] + "</th></tr>";
    let starData = resultData["movie_star"];
    // Iterate through starData
    for (let i = 0; i < starData.length; i++) {
        if (i !== 0) rowHTML2 += ", ";
        rowHTML2 +=
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href=single-star.html?id=' + starData[i]["star_id"] + '>'
            + starData[i]["star_name"] + '</a>';
    }
    rowHTML2 += "</th></tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.innerHTML += rowHTML;
    movieTableBodyElement2.innerHTML += rowHTML2;
    prepareAddToShoppingCartButton(movie)
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleResult
(function() {
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
    })
})()