
function copyText(buttonClicked, textContainerId){
    navigator.clipboard.writeText($("#"+textContainerId).text());

    buttonClicked = $(buttonClicked);
    let oldContent = buttonClicked.html();
    // console.log("old content: " + oldContent);
    buttonClicked.html('<i class="fa-solid fa-clipboard-check"></i>');
    setTimeout(
        function (){
            // console.log("Setting copy symbol back.");
            buttonClicked.html(oldContent);
        },
        5_000
    );
}
