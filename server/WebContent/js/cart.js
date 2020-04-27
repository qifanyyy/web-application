function handleResult(resultData) {
    if (resultData['errorMessage']) {
        jsonErrorMsgHandler(resultData);  // util.js
        return;
    }

    if (resultData.length === 0) {
        document.body.innerHTML += `<p id="cart-is-empty-prompt"><em>Cart is empty~</em></p>`
        return
    }

    document.getElementById('proceed-to-checkout').disabled = false
    const tableBody = document.getElementById('cart_table_body')
    let subTotal = 0

    for (let cartItem of resultData) {
        let row = `<tr>
    <td>${cartItem['movieTitle']}</td>
    <td>${cartItem['quantity']}</td>
    <td>$2</td>
    <td>$${subTotal += 2 * cartItem['quantity']}</td>
</tr>`

        tableBody.innerHTML += row
    }
}

fetch('/api/cart')
    .then(response => response.json(), reason => console.error(reason))
    .then(json => handleResult(json))
