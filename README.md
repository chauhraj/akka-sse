# Server Side Server Sent Events

When there is one way communication between Server and the client, then, SSE seems a reasonable option. The advantages are that
it is simple to implement and retry mechanism is inbuilt into it as long as the EventSource on the client side is not closed.

This is a working prototype where SSE will start publishing to the client side.
