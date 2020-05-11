/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function jsonErrorMsgHandler(json) {
    console.error(json['errorMessage']);
    if (json['stackTrace']) {
        console.error(json['stackTrace']);
    }
}

function setUpOnCheckout() {
    document.getElementById('checkout-btn').addEventListener('click', ev => {
        window.location.href = 'cart.html'
    })
}

function movieObjectToURLSearchParams(movie) {
    let formData = new FormData()
    for (let key of Object.keys(movie)) {
        formData.append(key, movie[key])
    }
    return new URLSearchParams(formData)
}

function setUpLogOutBtn() {
    document.getElementById('logout-btn').addEventListener('click', ev => {
        fetch('api/logout', { method: 'POST' })
            .then(response => response.json(), reason => console.error(reason))
            .then(json => {
                console.log(json)
                window.location.href = 'login.html'
            })
    })
}

function setUpSearchForm() {
    const searchBtn = document.querySelector('#search-form > button')
    const firstField = document.querySelector('#search-form > input:first-child')
    searchBtn.addEventListener('click', ev => {
        for (let inputField of document.querySelectorAll('#search-form > input')) {
            if (inputField.value !== '') {
                firstField.removeAttribute('required')
                firstField.setCustomValidity('')
                break
            }
            firstField.setAttribute('required', '')
            firstField.setCustomValidity('Please at least enter one search field')
        }
    })
}


/* When the user clicks on the button,
toggle between hiding and showing the dropdown3 content */
function myFunction3() {
    document.getElementById("mydropdown3").classList.toggle("show");
}

// Close the dropdown3 if the user clicks outside of it
window.onclick = function(event) {
    if (!event.target.matches('.dropbtn3')) {
        var dropdown3s = document.getElementsByClassName("dropdown3-content");
        var i;
        for (i = 0; i < dropdown3s.length; i++) {
            var opendropdown3 = dropdown3s[i];
            if (opendropdown3.classList.contains('show')) {
                opendropdown3.classList.remove('show');
            }
        }
    }
}