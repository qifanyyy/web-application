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

fetch('api/dashboard')
    .then(response => response.json(), reason => console.error(reason))
    .then(json => handleResult(json))