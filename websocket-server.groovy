from('vertx-websocket://chat-server').to('log:info')
    .setHeader('CamelVertxWebsocket.sendToAll',simple('true'))
    .to('vertx-websocket://chat-server')
