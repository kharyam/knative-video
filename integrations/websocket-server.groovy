from('vertx-websocket:localhost:8080/chat-server').to('log:info')
    .setHeader('CamelVertxWebsocket.sendToAll',simple('true'))
    .to('vertx-websocket:localhost:8080/chat-server')
