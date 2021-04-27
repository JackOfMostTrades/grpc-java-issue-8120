import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) throws Exception {
        ManagedChannel clientChannel = NettyChannelBuilder.forAddress("localhost", 8980).usePlaintext().build();
        DemoServiceGrpc.DemoServiceStub client = DemoServiceGrpc.newStub(clientChannel);

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<Empty> clientStream = client.demoBiDirectional(new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusRuntimeException) {
                    StatusRuntimeException e = (StatusRuntimeException) t;
                    System.out.println("client onError(): " + e.getStatus());
                } else {
                    t.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {
                System.out.println("client onComplete()");
            }
        });
        clientStream.onNext(Empty.getDefaultInstance());

        countDownLatch.await();
        System.exit(0);
    }
}
