This is an self-contained reproducer for [this issue](https://github.com/grpc/grpc-java/issues/8120).

To run this reproducer, start the Server class in one shell: `./gradlew server`

In a separate shell, run the Client class: `./gradlew client`

In the server's shell, you will see the following logged:

```
Server saw stream closed on method DemoService/DemoBiDirectional with status Status{code=UNAVAILABLE, description=connection terminated for unknown reason, cause=null}
server onError(): Status{code=CANCELLED, description=client cancelled, cause=null}
interceptor close(): Status{code=CANCELLED, description=Cancelling request because of error from client., cause=null}
```
