import com.google.protobuf.Empty;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerStreamTracer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

import java.net.InetSocketAddress;

public class Server {

    private static class DemoServiceImpl extends DemoServiceGrpc.DemoServiceImplBase {
        @Override
        public StreamObserver<Empty> demoBiDirectional(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {
                    responseObserver.onNext(Empty.getDefaultInstance());
                }

                @Override
                public void onError(Throwable t) {
                    if (t instanceof StatusRuntimeException) {
                        StatusRuntimeException e = (StatusRuntimeException) t;
                        System.out.println("server onError(): " + e.getStatus());
                    } else {
                        t.printStackTrace();
                    }
                    responseObserver.onError(Status.fromCode(Status.Code.CANCELLED).withDescription("Cancelling request because of error from client.").asException());
                }

                @Override
                public void onCompleted() {
                    System.out.println("server onComplete()");
                    responseObserver.onCompleted();
                }
            };
        }
    }

    private static class LoggingInterceptor implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            final ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                @Override
                public void close(Status status, Metadata trailers) {
                    System.out.println("interceptor close(): " + status);
                    super.close(status, trailers);
                }
            };
            return next.startCall(wrappedCall, headers);
        }
    }

    public static void main(String[] args) throws Exception {
        DemoServiceImpl demoService = new DemoServiceImpl();

        io.grpc.Server server = NettyServerBuilder.forAddress(new InetSocketAddress("localhost", 8980))
                .addStreamTracerFactory(new ServerStreamTracer.Factory() {
                    @Override
                    public ServerStreamTracer newServerStreamTracer(String fullMethodName, Metadata headers) {
                        return new ServerStreamTracer() {
                            @Override
                            public void streamClosed(Status status) {
                                System.out.println("Server saw stream closed on method " + fullMethodName + " with status " + status.toString());
                            }
                        };
                    }
                })
                .addService(demoService)
                .intercept(new LoggingInterceptor())
                .build();
        server.start();
        server.awaitTermination();
    }
}
