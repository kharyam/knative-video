#!/bin/bash

# https://github.com/paxtonhare/demo-magic
. ~/bin/demo-magic.sh

# Update OCP banner
oc patch ConsoleNotification security-notice -p '{"spec":{"backgroundColor":"purple","text":"Camel-K, Knative, Kafka Demo"}}' --type=merge

clear

pe "oc new-project chat-server"
oc project chat-server

pe "kn channel create chat-channel --type  messaging.knative.dev:v1beta1:KafkaChannel"

pe "kamel install"

pe "kamel run chat-log.yaml --logs"

pe "kamel run chat-rest.groovy --logs"

pe 'kn service list'

pe 'curl -kvvv -X PUT -H "content-type: application/text" -d "D. Vader: I am your father" https://chat-rest-chat-server.apps-crc.testing/message/'

pe 'kamel run websocket-server.groovy --logs'

pe 'oc create -f websocket-server-svc.yaml'

pe 'oc expose svc/websocket-server'

pe 'kamel run chat-websocket.yaml --logs'

pe 'kn service create chat-webapp --image=image-registry.openshift-image-registry.svc:5000/chat-server/chat-webapp@sha256:2aef187431ca973e9668f40dd87e22856c7fb923521496f6c096af67bb8fbfcb --scale=1..5 -e WEBSOCKET_URL=ws://websocket-server-chat-server.apps-crc.testing/chat-server -e REST_URL=http://chat-rest-chat-server.apps-crc.testing/message/'

pe "kamel run ChatGoogleSheets.java \
-e CLIENT_ID=$CLIENT_ID \
-e CLIENT_SECRET=$CLIENT_SECRET \
-e REFRESH_TOKEN=$REFRESH_TOKEN \
-e SPREADSHEET_ID=$SPREADSHEET_ID --logs"

