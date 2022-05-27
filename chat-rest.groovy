from('rest:options:/message:').setHeader("Access-Control-Allow-Origin",constant("*")).setHeader("Access-Control-Allow-Methods",constant("PUT")).setHeader("Access-Control-Allow-Headers",constant("Origin, X-Requested-With, Content-Type, Accept"));;
from('rest:put:/message:').setHeader("Access-Control-Allow-Origin",constant("*")).to('knative:channel/chat-channel');
