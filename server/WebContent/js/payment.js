function displayCartTable(json) {
    if (json['errorMessage']) {
        jsonErrorMsgHandler(json);  // util.js
        return;
    }

    const tableBody = document.getElementById('cart_table_body')
    let total = 0

    for (let cartItem of json) {
        let row = `<tr class="align-middle">
    <td class="align-middle">${cartItem['movieTitle']}</td>
    <td class="align-middle">${cartItem['quantity']}</td>
    <td class="align-middle">$2</td>
</tr>`
        tableBody.innerHTML += row
        total += 2 * cartItem['quantity']
    }

    document.getElementById('total-price').innerText = `Total Price: $${total}`
}

(function () {
    const placeOrderBtn = document.getElementById('place-order-btn')
    placeOrderBtn.addEventListener('click', ev => {
        const form = document.getElementById('payment-form')
        if (!form.checkValidity())
            return
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
        })
            .then(json => {
                if (json['status'] === 'success') {
                    window.location.replace('payment-confirmation.html')
                    return
                }
                errMsg.innerText = json['errorMessage']
                errMsgWrapper.style.display = 'block'
                jsonErrorMsgHandler(json)
            }, reason => {
                errMsg.innerText = reason
                errMsgWrapper.style.display = 'block'
            })
    })

    // document.getElementById('card-expiration').setAttribute('min', new Date().toISOString().split('T')[0])
    setUpLogOutBtn()

    fetch('api/cart')
        .then(response => response.json(), reason => console.error(reason))
        .then(json => displayCartTable(json))
})()