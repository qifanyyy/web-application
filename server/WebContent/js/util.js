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
        console.log(key)
        formData.append(key, movie[key])
    }
    return new URLSearchParams(formData)
}
