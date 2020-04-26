function handleResult(resultData) {
    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData);  // util.js
        return;
    }

    let i = 0;
    for (let genre of resultData) {
        document.getElementById(`genre-list${i}`).innerHTML += `<li><a href="movie-list.html?genre=${genre['name']}">${genre['name']}</a></li>`
        i = i < 4 ? i + 1 : 0;
    }
}

(function () {
    fetch('api/genres', {
        headers: {
            'content-type': 'application/json;charset=UTF-8'
        },
        method: 'GET'
    }).then(response => response.json(), error => console.error(error)).then(json => handleResult(json))

    for (let i = 0; i < 10; ++i) {
        document.getElementById('title-numeric-star').innerHTML += `<li>${i}</li>`
    }
    document.getElementById('title-numeric-star').innerHTML += '<li>*</li>'

    for (let i = 0; i < 26; ++i) {
        document.getElementById('title-uppercase').innerHTML += `<li>${String.fromCharCode(i + 'A'.charCodeAt(0))}</li>`
    }
})()