let webSocketUrl = config.webSocketUrl;
let restUrl = config.restUrl;

let socket = new WebSocket(webSocketUrl);


// handle incoming messages
socket.onmessage = function(event) {
  let incomingMessage = event.data;
  showMessage(incomingMessage);
};

socket.onclose = event => console.log(`Closed ${event.code}`);

// show message in chat area
function showMessage(message) {
  chatArea = document.getElementById('chatarea');

  if (typeof message == 'string') {
    chatArea.innerHTML += message + "\n";
    chatArea.scrollTo(0,chatArea.scrollHeight); 
    return;
  }

// Convert blob to string
const reader = new FileReader();
// This fires after the blob has been read/loaded.
reader.addEventListener('loadend', (e) => {
    const text = e.srcElement.result;
    showMessage(text);
});

// Start reading the blob as text.
reader.readAsText(message);

}

function sendMessage() {

    chatName = this.chatname.value;
    message = this.messagearea.value;

    if (chatName == null || chatName == "" || message == null || message == "") {
        alert("Please specify your name and message.");
        return false;
    } 
    let outgoingMessage = chatName + ": " + message;

    var xhr = new XMLHttpRequest();
    xhr.open("PUT", restUrl);
    xhr.setRequestHeader('Content-Type','application/text');

    xhr.onreadystatechange = function() {
      if (xhr.readyState == 4 && xhr.status != 200 && xhr.status != 202) {
        console.log(xhr.status);
        console.log(xhr.responseText);
      }
    }

    xhr.send(outgoingMessage);
    this.messagearea.value = ""
}