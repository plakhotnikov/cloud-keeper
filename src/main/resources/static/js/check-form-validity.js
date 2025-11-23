/* Custom validity settings */
function setSearchInputCustomValidity() {
    let input = document.getElementById("query");
    if (input === null || input === undefined) {
        return;
    }
    let errorMessage = input.getAttribute("data-error-message")
    input.addEventListener("invalid", event => {
        if (event.target.validity.valueMissing || event.target.validity.patternMismatch) {
            event.target.setCustomValidity(errorMessage);
        }
    })

    input.addEventListener("change", event => {
        event.target.setCustomValidity("");
    })

    input.addEventListener("change", event => {
        input.closest("form").classList.add("was-validated");
    })
}

function clearDefaultValidity() {
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    const forms = document.querySelectorAll(".needs-validation")
    // Loop over them and prevent submission
    Array.from(forms).forEach(form => {
        form.addEventListener("submit", event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add("was-validated");
        }, false)
    })
}

function addAutoClearValidityErrorsOnModalInputs() {
    let inputs = document.querySelectorAll("#mk-dir-name, #new-obj-name");
    Array.from(inputs).forEach(input => {
        input.addEventListener("change", event => {
            input.closest("form").classList.add("was-validated");
        })
    })
}

clearDefaultValidity();
addAutoClearValidityErrorsOnModalInputs()
setSearchInputCustomValidity();