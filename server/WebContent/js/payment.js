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

fetch('api/cart')
    .then(response => response.json(), reason => console.error(reason))
    .then(json => displayCartTable(json))