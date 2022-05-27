#!/bin/bash

if [ -z $WEBSOCKET_URL ] || [ -z $REST_URL ] || [ $WEBSOCKET_URL == "UNSET" ] || [ $REST_URL == "UNSET" ]
    then
        echo "WEBSOCKET_URL and REST_URL most both be set."
        exit 1
fi

cp config_template.js config.js
sed -i -e "s|WEBSOCKET_URL|${WEBSOCKET_URL}|g" -e "s|REST_URL|${REST_URL}|g" config.js

python3 -m http.server 8080 