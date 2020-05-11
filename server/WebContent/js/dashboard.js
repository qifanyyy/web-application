function handleResult(json) {
    if (json['status'] !== 'success') {
        jsonErrorMsgHandler(json)
        return
    }

    let showcaseRootDiv = document.getElementById('database-metadata')
    showcaseRootDiv.innerHTML = '<ul>'
    let tables = json['tables']
    for (let table of tables) {
        let tableName = table['name']
        let columns = table['columns']
        let tableHTML = `<li>${tableName}<ul>`
        for (let column of columns) {
            tableHTML += `<li>${column['columnName']} (type is "${column['columnType']}")</li>`
        }
        tableHTML += '</ul></li>'
        showcaseRootDiv.innerHTML += tableHTML;
    }
    showcaseRootDiv.innerHTML += '</ul>'
}

function onSubmit(event, form) {
    event.preventDefault()
    fetch('api/dashboard', {
        method: 'POST',
        body: new URLSearchParams(new FormData(form))
    }).then(response => response.json(), reason => {
        console.error(reason)
        alert(`Failed to add`)
    }).then(json => {
        if (json['status'] !== 'success') {
            jsonErrorMsgHandler(json)
            alert(`Failed to add`)
        } else {
            alert(`Success!`)
        }
    })
}

const addStarForm = document.getElementById('add-star-form')
addStarForm.addEventListener('submit', (ev) => onSubmit(ev, addStarForm))

const addMovieForm = document.getElementById('add-movie-form')
addMovieForm.addEventListener('submit', ev => onSubmit(ev, addMovieForm))

fetch('api/dashboard')
    .then(response => response.json(), reason => console.error(reason))
    .then(json => handleResult(json))