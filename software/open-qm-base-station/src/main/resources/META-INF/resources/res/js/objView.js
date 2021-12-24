

function displayKeywordsIn(container, keywords){
    keywords.forEach(function(keyword){
        console.log("Keyword: " + keyword);
        container.append(
            $('<span class="badge bg-light text-dark m-2 user-select-all">'+keyword+'</span> ')
        );
    });
}

function processKeywordDisplay(display, container, keywords){
    if(keywords.length > 0){
        console.log("had keywords");
        viewKeywordsTextSection.show();
        displayKeywordsIn(viewKeywordsText, keywords);
    }
}

function displayAttsIn(container, attributes){
    Object.entries(attributes).forEach(entry => {
        const [key, val] = entry;
        console.log("Att: " + key + "/" + val);
        viewAttsSectionText.append($('<span class="badge bg-light text-dark m-2"><span class="user-select-all">'+key + '</span> <i class="fas fa-equals"></i> <code class="user-select-all">'+ val + '</code></span> '));
    });
}

function processAttDisplay(display, container, attributes){
    if(Object.keys(attributes).length > 0){
        console.log("had attributes");
        viewAttsSection.show();

        displayAttsIn(container, attributes)
    }
}

function displayObjHistory(container, historyList){
    console.log("Displaying image history.");
    historyList.forEach(function(curEvent){
        var curhistRow = $("<tr></tr>");

        curhistRow.append($("<td>"+curEvent.timestamp+"</td>"));
        var userTd = $('<td data-user-id="'+curEvent.userId+'">'+curEvent.userId+"</td>")
        curhistRow.append(userTd);
        curhistRow.append($("<td>"+curEvent.type+"</td>"));
        curhistRow.append($("<td>"+curEvent.description+"</td>"));

        container.append(curhistRow);

        doRestCall({
                url: "/api/user/" + curEvent.userId,
                method: "GET",
                async: true,
                done: function(data) {
                    console.log("Response from create request: " + JSON.stringify(data));
                    userTd.text(data.username);
                },
                fail: function(data) {
                    console.warn("Bad response from user data get request: " + JSON.stringify(data));
                    addMessageToDiv(imageViewMessages, "danger", "Failed to get info on user: " + data.responseText, "Failed", null);
                }
            });
    });
}