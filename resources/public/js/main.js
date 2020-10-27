var exampleSocket = new WebSocket("ws://127.0.0.1:8009/ws")

exampleSocket.onopen = function (event) {
    exampleSocket.send(JSON.stringify({"DD":1})); 
};
