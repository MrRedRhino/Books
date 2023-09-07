let currentPage;
let book = null;
const searchHistory = JSON.parse(localStorage.getItem("search-history") ?? "[]");

class Book {
    constructor(id, title, subject, pageCount) {
        this.pageCount = pageCount;
        this.subject = subject;
        this.title = title;
        this.id = id;
    }

    pageCount;
    subject;
    title;
    id;
}

document.getElementById("search").addEventListener("keyup", (event) => {
    if (event.code !== "Enter") return;
    event.preventDefault();
    const element = document.getElementById("suggestions").children.item(0);
    if (!element.classList.contains("no-enter-select")) element.click();
});

addEventListener("keydown", (event) => {
    if (book == null
        || document.getElementById("search") === document.activeElement
        || document.getElementById("page-selector") === document.activeElement
        || document.getElementById("text-search-input") === document.activeElement
    ) return;

    switch (event.code) {
        case "ArrowRight": {
            changePage(1);
            break;
        }
        case "ArrowLeft": {
            changePage(-1);
            break;
        }
    }
});

function selectBook(newBook, page = 1) {
    book = newBook;
    currentPage = page;
    document.getElementById("controls").hidden = book == null;
    document.getElementById("max-page").innerHTML = "/ " + book.pageCount;
    setPage(page);
}

function changePage(lambda) {
    setPage(currentPage + lambda);
    searchOnCurrentPage();
}

function setPage(page, updateInputField = true, after = null) {
    if (isNaN(page)) page = 1;
    currentPage = Math.max(1, Math.min(book.pageCount, page));
    if (updateInputField) document.getElementById("page-selector").value = currentPage;
    window.history.replaceState({}, null, "?book=" + book.id + "&page=" + currentPage);

    const promise = fetch(`/api/books/${book.id}/${currentPage}`).then(r => r.text().then(t => {
        const book = document.getElementById("book1");
        book.innerHTML = t
        resizeBook();
    }));

    if (after !== null) {
        promise.then(after);
    }
}

document.getElementById("search").onfocus = () => {
    doSearch();
};

document.getElementById("search").addEventListener("focusout", () => {
    hideSuggestions();
});

function doSearch() {
    const list = document.getElementById("suggestions");
    const query = document.getElementById("search").value;

    if (query.trim().length === 0 && searchHistory.length > 0) {
        list.innerHTML = "";
        searchHistory.forEach(element => {
            const li = createThing("li", "list-element", "");
            const a = createThing("a", "list-element-name", element.name + " S. " + element.page);
            li.onmousedown = () => {
                openBook(element.id, element.page, false);
            }
            li.appendChild(a);
            list.appendChild(li);
        });
        document.getElementById("suggestions-div").hidden = false;
        updateRoundedCorners();

        return;
    }

    fetch("/api/completions?query=" + query).then(r => r.json().then(resp => {
        list.innerHTML = "";

        let hide = true;
        for (let i = 0; i < resp.length; i++) {
            const s = resp[i];
            addSearchResult(list, s["book"]["title"], s["book"]["subject"], s["page"], s["book"]["id"], false);
            hide = false;
        }

        if (resp.length === 0 && query.length > 0) {
            addSearchResult(list,
                "Nicht gefunden?", "Drücke <span style='font-weight: bold'>hier</span>, um das neue Buch hinzuzufügen.",
                0, 0, true
            );
            hide = false;
        }

        document.getElementById("suggestions-div").hidden = hide;
        if (hide) {
            hideSuggestions();
        } else {
            updateRoundedCorners();
        }
    }));
}

function addSearchResult(ul, title, subject, page, bookId, isUploadMessage) {
    const li = document.createElement("li");
    li.classList.add("list-element");
    li.appendChild(createThing("a", "list-element-name", title));
    if (!isUploadMessage) li.appendChild(createThing("a", "list-element-subject", " S. " + page));
    li.appendChild(createThing("p", "list-element-subject", subject));
    if (isUploadMessage) {
        li.addEventListener("click", () => {
            location.href = "/upload"
        });
    } else {
        li.addEventListener("click", () => {
            openBook(bookId, page, true);
            hideSuggestions();
        });
    }
    if (isUploadMessage) li.classList.add("no-enter-select");

    ul.appendChild(li);
}

function openBook(bookId, page, addToSearchHistory) {
    fetchBook(bookId).then(b => {
        if (book !== null && book.id !== b.id) {
            history.pushState(null, "", window.location);
            history.go(history.length);
        }
        selectBook(b, page);

        if (!addToSearchHistory) return;

        searchHistory.unshift({
            page: page,
            name: b.title,
            id: b.id
        });

        while (searchHistory.length > 5) {
            searchHistory.pop();
        }
        localStorage.setItem("search-history", JSON.stringify(searchHistory));
    });
}

function createThing(obj, clazz, text) {
    const o = document.createElement(obj);
    o.classList.add(clazz);
    o.innerHTML = text;
    return o;
}

document.getElementById("page-selector").addEventListener("input", () => {
    setPage(parseInt(document.getElementById("page-selector").value), false);
    searchOnCurrentPage();
});

function hideSuggestions() {
    document.getElementById("suggestions-div").hidden = true;
    updateRoundedCorners();
}

function updateRoundedCorners() {
    if (!document.getElementById("suggestions-div").hidden || !document.getElementById("text-search-div").hidden) {
        document.getElementById("top-nav").classList.remove("nav-bar-rounded-corners");
    } else {
        document.getElementById("top-nav").classList.add("nav-bar-rounded-corners");
    }
}

document.getElementById("search").addEventListener("input", () => {
    doSearch();
});

document.getElementById("search").addEventListener("focusin", () => {
    doSearch();
});

function fetchBook(id) {
    return fetch("/api/books/" + id).then(r => r.json().then(j => {
        return new Book(j["id"], j["title"], j["subject"], j["page-count"]);
    }));
}

function loadBookFromUrl() {
    const params = new URL(location.href).searchParams;
    if (params.has("book")) fetchBook(params.get("book")).then(b => selectBook(b, parseInt(params.get("page"))));
}

loadBookFromUrl();

addEventListener("resize", () => resizeBook());

function applyScaling(scaledWrapper) {
    const scaledContent = scaledWrapper.getElementsByClassName('scaled-content')[0];
    scaledContent.style.transform = 'scale(1, 1)';

    const {width: cw, height: ch} = scaledContent.getBoundingClientRect();
    const {width: ww, height: wh} = scaledWrapper.getBoundingClientRect();

    const scaleAmount = Math.min(ww / cw, wh / ch);

    scaledContent.style.transform = `scale(${scaleAmount}, ${scaleAmount})`;
}

function resizeBook() {
    applyScaling(document.getElementById("book-display"));
}

function textSearch(url, changePage = false) {
    unhighlight();
    if (getQuery().length === 0) return;

    fetch(url).then(r => r.json().then(json => {
        const results = json["results"];
        if (changePage && results.length > 0) {
            setPage(results[0]["page"], true, () => {
                highlightAll(results);
                updateResultCount(json["total-results"]);
            });
        } else {
            highlightAll(results);
            updateResultCount(json["total-results"]);
        }
    }));
}

function updateResultCount(count) {
    document.getElementById("result-count").innerText = count;
}

function highlightAll(results) {
    for (let result of results) {
        let start = result["highlight"]["start"];
        highlight(start, start + result["highlight"]["length"]);
    }
}

function getQuery() {
    return document.getElementById("text-search-input").value;
}

function searchOnCurrentPage() {
    textSearch(`/api/text-search?query=${getQuery()}&book=${book.id}&page=${currentPage}`);
}

function searchOnNextPage() {
    textSearch(`/api/text-search?query=${getQuery()}&book=${book.id}&page=${currentPage}&location=after`, true);
}

function searchOnPreviousPage() {
    textSearch(`/api/text-search?query=${getQuery()}&book=${book.id}&page=${currentPage}&location=before`, true)
}

function highlight(start, end) {
    let curPos = 0;
    for (let element of document.getElementsByClassName("page")[0].children) {
        if (curPos <= end && curPos >= start) {
            highlightElement(element);
        }
        curPos += element.innerHTML.length + 1;
    }
}

function highlightElement(element) {
    const newNode = element.cloneNode(true);
    element.parentNode.append(newNode)
    newNode.classList.add("highlight");
}

function unhighlight() {
    const elements = document.getElementsByClassName("highlight");
    for (let i = elements.length - 1; i >= 0; i--) {
        elements[i].remove();
    }
}

function toggleTextSearch() {
    const element = document.getElementById("text-search-div");
    element.hidden = !element.hidden;
}

function showSummary() {
    document.getElementById("summary-popup").hidden = false;
    const summaryElement = document.getElementById("summary");
    summaryElement.innerText = "Eine Sekunde...";

    fetch(`/api/books/${book.id}/${currentPage}/summary`).then(r => r.text().then(text => {
        summaryElement.innerText = text;
    }));
}

function closeSummary() {
    document.getElementById("summary-popup").hidden = true;
}

window.addEventListener('popstate', () => {
    loadBookFromUrl();
}, false);
