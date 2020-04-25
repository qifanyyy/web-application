function handleResult(resultData) {
    let errorMessage

    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData)
        errorMessage = 'Unable to log in'
    }

    if (resultData['status'] === 'success') {
        window.location.replace('index.html')
        return
    }

    errorMessage = 'Invalid email address or password'
    document.getElementById('err-msg').innerText = errorMessage
    document.getElementById('err-msg-wrapper').style.display = 'block';
}

const loginForm = document.getElementById('login-form')

loginForm.addEventListener('submit', ev => {
    ev.preventDefault()
    fetch('api/login', {
        method: 'POST',
        body: new URLSearchParams(new FormData(loginForm))
    }).then(response => response.json(), error => console.error(error)).then(json => handleResult(json))
})