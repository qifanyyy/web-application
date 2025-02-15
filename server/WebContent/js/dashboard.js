function onSubmit(event, form, onSuccess) {
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
            alert('Failed to add')
        } else {
            onSuccess(json)
        }
    })
}

function makeTableCardColumn({columnName, columnType}) {
    return `<div class="database-column">
<p class="database-column-name">${columnName}</p>
<p class="database-column-type">${columnType}</p>
</div>`
}

function makeTableCardColumns(columns) {
    return `<div class="database-columns">
${columns.reduce((html, col) => html + makeTableCardColumn({...col}), '')}
</div>`
}

function makeTableCard({name, columns}) {
    return `<div class="database-card">
<p class="database-card-table-name">${name}</p>
${makeTableCardColumns(columns)}
</div>`
}

function handleResult(json) {
    if (json['status'] !== 'success') {
        jsonErrorMsgHandler(json)
        return
    }

    document.getElementById('database-metadata').innerHTML =
        json['tables'].reduce((html, table) => html + makeTableCard({...table}), '')
}

const addStarForm = document.getElementById('add-star-form')
addStarForm.addEventListener('submit', (ev) => onSubmit(ev, addStarForm, json => {
    alert(`Star (id=${json['retStarId']}) added`)
}))

const addMovieForm = document.getElementById('add-movie-form')
addMovieForm.addEventListener('submit', ev => onSubmit(ev, addMovieForm, json => {
    if (json['hasDupMovie']) {
        alert(`Adding duplicate movie (id=${json['retMovieId']}), rejected; no modification on database`)
    } else {
        let prompt = `Movie (id=${json['retMovieId']}) with star (${json['hasDupStar'] ? 'existed' : 'new'}, id=${json['retStarId']}) and genre (${json['hasDupGenre'] ? 'existed' : 'new'}, id=${json['retGenreId']}))`
        alert(prompt)
    }
}))

document.getElementById('logout-button').addEventListener('click', ev => {
    ev.preventDefault()
    fetch('logout').then(response => {
        console.log(response)
        window.location.href = 'employee_login.html'
    })
})

const showMetaDataButton = document.getElementById('show-metadata-button')
showMetaDataButton.addEventListener('click', ev => {
    ev.preventDefault()
    if (showMetaDataButton.classList.contains('active-btn')) {
        return
    }

    showMetaDataButton.classList.replace('non-active-btn', 'active-btn')
    document.getElementById('database-metadata').style.display = 'flex'

    document.getElementById('show-add-star-button').classList.replace('active-btn', 'non-active-btn')
    document.getElementById('show-add-movie-button').classList.replace('active-btn', 'non-active-btn')
    document.getElementById('add-star').style.display = 'none'
    document.getElementById('add-movie').style.display = 'none'
})

const showAddStarButton = document.getElementById('show-add-star-button')
showAddStarButton.addEventListener('click', ev => {
    ev.preventDefault()
    if (showAddStarButton.classList.contains('active-btn')) {
        return
    }

    showAddStarButton.classList.replace('non-active-btn', 'active-btn')
    document.getElementById('add-star').style.display = 'block'

    document.getElementById('show-metadata-button').classList.replace('active-btn', 'non-active-btn')
    document.getElementById('show-add-movie-button').classList.replace('active-btn', 'non-active-btn')
    document.getElementById('database-metadata').style.display = 'none'
    document.getElementById('add-movie').style.display = 'none'
})

const showAddMovieButton = document.getElementById('show-add-movie-button')
showAddMovieButton.addEventListener('click', ev => {
    ev.preventDefault()
    if (showAddMovieButton.classList.contains('active-btn')) {
        return
    }

    showAddMovieButton.classList.replace('non-active-btn', 'active-btn')
    document.getElementById('add-movie').style.display = 'block'

    document.getElementById('show-metadata-button').classList.replace('active-btn', 'non-active-btn')
    document.getElementById('show-add-star-button').classList.replace('active-btn', 'non-active-btn')
    document.getElementById('database-metadata').style.display = 'none'
    document.getElementById('add-star').style.display = 'none'
})

fetch('api/dashboard')
    .then(response => response.json(), reason => console.error(reason))
    .then(json => handleResult(json))