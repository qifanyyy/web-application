function handleResult(resultData) {
    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData);  // util.js
        return;
    }

    let i = 0;
    for (let genre of resultData) {
        document.getElementById(`genre-list${i}`).innerHTML += `<li>${genre['name']}</li>`
        i = i < 4 ? i + 1 : 0;
    }
}

fetch('api/genres', {
    headers: {
        'content-type': 'application/json;charset=UTF-8'
    },
    method: 'GET'
}).then(response => response.json(), error => console.error(error)).then(json => handleResult(json))