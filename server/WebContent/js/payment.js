function displayCartTable(json) {
    if (json['errorMessage']) {
        jsonErrorMsgHandler(json);  // util.js
        return;
    }

    const tableBody = document.getElementById('cart_table_body')
    let total = 0

    for (let cartItem of json) {
        let row = `<tr class="align-middle sale-row" data-movie-id="${cartItem['movieId']}">
    <td class="align-middle">${cartItem['movieTitle']}</td>
    <td class="align-middle">${cartItem['quantity']}</td>
    <td class="align-middle">$2</td>
</tr>`
        tableBody.innerHTML += row
        total += 2 * cartItem['quantity']
    }

    document.getElementById('total-price').innerText = `Total Price: $${total}`
}

function updateTableForSale(sales) {
    for (let sale of sales) {
        const row = document.querySelector(`.sale-row[data-movie-id="${sale['movieId']}"]`)
        const titleTd = document.querySelector(`.sale-row[data-movie-id="${sale['movieId']}"] > td:first-child`)
        const saleIdTd = row.insertBefore(document.createElement('td'), titleTd)
        saleIdTd.classList.add('align-middle')
        saleIdTd.innerText = sale['saleId']
    }
}

function updatePaymentFormWhenSuccess() {
    for (let paymentInput of document.querySelectorAll('#payment-form input')) {
        paymentInput.disabled = true
        paymentInput.readonly = true
    }
    const placeOrderBtn = document.getElementById('place-order-btn')
    placeOrderBtn.value = 'Success! Thanks for your purchase'
    placeOrderBtn.disabled = true
    placeOrderBtn.readonly = true
}

(function () {
    const placeOrderBtn = document.getElementById('place-order-btn')
    placeOrderBtn.addEventListener('click', ev => {
        const form = document.getElementById('payment-form')
        if (!form.checkValidity()) {
            return
        }
        ev.target.value = 'Please wait...'
        ev.target.disabled = true
        ev.preventDefault()
        const payloadBody = new URLSearchParams(new FormData(form))
        const errMsg = document.getElementById('err-msg')
        const errMsgWrapper = document.getElementById('err-msg-wrapper')
        fetch('api/payment', {
            method: 'POST',
            body: payloadBody
        }).then(response => response.json(), reason => {
            errMsg.innerText = reason
            errMsgWrapper.style.display = 'block'
            ev.target.value = 'Place Order'
            ev.target.disabled = false
        }).then(json => {
            if (json['status'] !== 'success') {
                errMsg.innerText = json['errorMessage']
                errMsgWrapper.style.display = 'block'
                jsonErrorMsgHandler(json)
                ev.target.value = 'Place Order'
                ev.target.disabled = false
            } else {
                errMsgWrapper.style.display = 'none'
                document.getElementById('sale-id-header').style.display = 'block'
                updateTableForSale(json['sales'])
                updatePaymentFormWhenSuccess()
            }
        }, reason => {
            errMsg.innerText = reason
            errMsgWrapper.style.display = 'block'
            ev.target.value = 'Place Order'
            ev.target.disabled = false
        })
    })

    // document.getElementById('card-expiration').setAttribute('min', new Date().toISOString().split('T')[0])

    fetch('api/cart')
        .then(response => response.json(), reason => console.error(reason))
        .then(json => displayCartTable(json))
})()