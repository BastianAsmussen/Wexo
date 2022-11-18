function validateData() {

    const shouldAlert = (data === null || Object.keys(data).length < 1)

    if (shouldAlert) {

        alert("Der er sket en fejl på vores side, prøv igen senere!")
    }
}

validateData()

console.log('Data', data)
