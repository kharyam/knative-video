#!/bin/bash

# Use demo magic script (https://github.com/paxtonhare/demo-magic) to simulate typing in the terminal via its 'pe' command
. ~/bin/demo-magic.sh

# Update OCP banner
oc apply -f ConsoleNotification.yaml

clear

pe "oc new-project chat-server"
oc project chat-server

pe "kn channel create chat-channel --type  messaging.knative.dev:v1beta1:KafkaChannel"

pe "kn channel delete chat-channel"

pe "vim chat-channel.yaml"

pe "oc create -f chat-channel.yaml"

pe "kamel install"

pe "kamel run chat-log.yaml --logs"

pe "kamel run chat-rest.groovy --trait knative-service.min-scale=1 --logs"

pe 'kn service list'

# Retreive the URL for the chat-rest integration
chat_rest_url=$(kn service list | grep chat-rest | awk '{print $2}' | sed s/https/http/)/message/ 

pe "curl -X PUT -H 'content-type: application/text' -d 'D. Vader: I am your father' $chat_rest_url"

pe 'kamel run websocket-server.groovy --trait knative-service.enabled=true --trait knative-service.min-scale=1 --logs'

# Retreive the URL for the websocket server 
websocket_url=$(kn service list | grep websocket-server | awk '{print $2}' | sed s/https/ws/)/chat-server

# Create chat-websocket.yaml if it does not exist
if [ ! -f chat-websocket.yaml ]; then
  sed -e "s|WEBSOCKET_SERVER|$websocket_url|g" -e 's|ws://||g' chat-websocket-template.yaml > chat-websocket.yaml
fi

pe 'kamel run chat-websocket.yaml --trait knative-service.min-scale=1 --logs'

pe "kn service create chat-webapp --image=quay.io/kharyam/chat-webapp:latest --scale=1..5 -e WEBSOCKET_URL=$websocket_url -e REST_URL=$chat_rest_url"

pe "kamel run ChatGoogleSheets.java \
-e CLIENT_ID=$CLIENT_ID \
-e CLIENT_SECRET=$CLIENT_SECRET \
-e REFRESH_TOKEN=$REFRESH_TOKEN \
-e SPREADSHEET_ID=$SPREADSHEET_ID --trait knative-service.min-scale=1 --logs"

