// Handle the search input placeholder text:
// Get the search input element.
const inputElement = document.getElementById("query");
// If a search query is present, use it as the placeholder text. E.g. /?search=foo.
const searchQuery = new URLSearchParams(window.location.search).get("search");

if (searchQuery) {
	
	inputElement.value = searchQuery;
	
} else {
	
	// Otherwise, use the placeholder text from the HTML.
	inputElement.placeholder = inputElement.getAttribute("placeholder");
}
