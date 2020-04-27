let getCart

function updateItemQuantity(movieId, movieTitle, btn) {
    let quantity
    if (isNaN(quantity = Number(btn.value))) {
        btn.value = '1'
        quantity = 1
    }

    if (!Number.isInteger(quantity)) {
        quantity = Math.ceil(quantity)
        btn.value = String(quantity)
    }

    const itemInfo = {
        movieId: movieId,
        movieTitle: movieTitle,
        quantity: quantity
    }

    const reqBody = movieObjectToURLSearchParams(itemInfo)

    fetch('api/cart', {
        method: 'POST',
        body: reqBody
    }).then(async response => response.json(), reason => console.error(reason)).then(json => {
        if (json['status'] !== 'success') {
            alert('Failed to update shopping cart')
            jsonErrorMsgHandler(json)
        }
        getCart()
    })
}

function removeItem(movieId, movieTitle) {
    fetch('api/cart', {
        method: 'POST',
        body: movieObjectToURLSearchParams({
            movieId: movieId,
            movieTitle: movieTitle,
            quantity: 0
        })
    }).then(async response => response.json(), reason => console.error(reason)).then(json => {
        if (json['status'] !== 'success') {
            alert('Failed to update shopping cart')
            jsonErrorMsgHandler(json)
        }
        getCart()
    })
}

function handleResult(resultData) {
    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData);  // util.js
        return;
    }

    const tableBody = document.getElementById('cart_table_body')
    tableBody.innerHTML = ''
    if (resultData.length === 0) {
        document.body.innerHTML += `<p id="cart-is-empty-prompt"><em>Cart is empty~</em></p>`
        document.getElementById('proceed-to-payment').disabled = true
        return
    }

    document.getElementById('proceed-to-payment').disabled = false
    let subTotal = 0

    for (let cartItem of resultData) {
        let row = `<tr class="align-middle">
    <td class="align-middle">${cartItem['movieTitle']}</td>
    <td class="align-middle">
        <input data-movie-id="${cartItem['movieId']}" data-movie-title="${cartItem['movieTitle']}" type="number" class="quantity-field form-control" min="1" value="${cartItem['quantity']}">
        <button data-movie-id="${cartItem['movieId']}" data-movie-title="${cartItem['movieTitle']}" class="btn btn-danger remove-item-from-cart">Remove</button>
    </td>
    <td class="align-middle">$2</td>
    <td class="align-middle">$${subTotal += 2 * cartItem['quantity']}</td>
</tr>`

        tableBody.innerHTML += row
    }

    for (let quantityInput of document.querySelectorAll('.quantity-field')) {
        quantityInput.addEventListener('blur', () => {
            updateItemQuantity(quantityInput.dataset.movieId, quantityInput.dataset.movieTitle, quantityInput)
        })
    }

    for (let removeBtn of document.querySelectorAll('.remove-item-from-cart')) {
        removeBtn.addEventListener('click', () => {
            removeItem(removeBtn.dataset.movieId, removeBtn.dataset.movieTitle)
        })
    }
}

(getCart = function() {
    setUpLogOutBtn()
    fetch('api/cart')
        .then(response => response.json(), reason => console.error(reason))
        .then(json => handleResult(json))
})()

document.getElementById('proceed-to-payment').addEventListener('click', ev => {
    window.location.href = 'payment.html'
})
