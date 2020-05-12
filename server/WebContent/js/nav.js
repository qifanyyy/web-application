$(function(){
    $("#nav-placeholder").load("./nav.html", () => {
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
    });
});