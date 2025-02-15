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

function movieObjectToURLSearchParams(movie) {
    let formData = new FormData()
    for (let key of Object.keys(movie)) {
        formData.append(key, movie[key])
    }
    return new URLSearchParams(formData)
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

function advance() {
    $("#nav-placeholder").load("advance.html", () => {
        setUpSearchForm()
    });
}

function goBackFullTextSearch() {
    $("#nav-placeholder").load("nav.html");
}


function colorchange(id) {
    sessionStorage.clear()
    let background = document.getElementById(id).style.backgroundColor;
    if (background === "transparent") {
        document.getElementById(id).style.background = "#007bff";
        document.getElementById(id).style.color = "#ffffff";
        document.getElementById("fuzzyy").value = "Fuzzyon";
    } else {
        document.getElementById(id).style.background = "transparent";
        document.getElementById(id).style.color = "#007bff";
        document.getElementById("fuzzyy").value = "FuzzyOff";
    }

}