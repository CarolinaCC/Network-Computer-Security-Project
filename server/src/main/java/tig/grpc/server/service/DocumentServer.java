package tig.grpc.server.service;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.server.api.CustomProtocolTigServiceImpl;
import tig.grpc.server.api.TigServiceImpl;
import tig.utils.db.PostgreSQLJDBC;
import tig.utils.interceptor.ExceptionHandler;
import tig.grpc.server.session.TokenCleanupThread;
import tig.utils.keys.KeyFileLoader;


import java.io.File;

public class DocumentServer {
    private static final Logger logger = Logger.getLogger(DocumentServer.class);

    public static void main(String[] args) throws Exception {
        System.out.println(DocumentServer.class.getSimpleName());

        // check arguments
        if (args.length < 7) {
            System.err.println("Argument(s) missing!");
            System.err.printf("<Usage> java %s port dbname certChainFile privateKeyFile privateKeyFilePCKS8 trustCertCollectionFile keyServerUrl%n", DocumentServer.class.getName());
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final BindableService impl = new TigServiceImpl();
        final BindableService customImpl = new CustomProtocolTigServiceImpl();


        //Initialize Postgres Connection
        PostgreSQLJDBC.setDbName(args[1]);
        PostgreSQLJDBC.getInstance();

        //Server Public Key Certificate
        File certChainFile = new File(args[2]);

        //Server Private Key
        File privateKeyFile = new File(args[3]);
        File privateKeyFileJava = new File(args[4]);
        CustomProtocolTigServiceImpl.privateKey = KeyFileLoader.loadPrivateKey(privateKeyFileJava);
        CustomProtocolTigServiceImpl.publicKey = KeyFileLoader.loadPublicKey(certChainFile);

        File trustCertCollection = new File(args[5]);

        //SslContext for Key Server
        SslContext context = GrpcSslContexts.forClient().trustManager(trustCertCollection)
                                                        .keyManager(certChainFile, privateKeyFile)
                                                        .build();

        //Connect to key server
        ManagedChannel channel = NettyChannelBuilder.forTarget(args[6])
                                                    .sslContext(context)
                                                    .build();
        TigKeyServiceGrpc.TigKeyServiceBlockingStub keyStub = TigKeyServiceGrpc.newBlockingStub(channel);
        TigServiceImpl.keyStub = keyStub;

        //Test Purposes only
        System.out.println("Connected and authenticated to key server successfully");
        Tig.HelloTigKeyRequest request = Tig.HelloTigKeyRequest.newBuilder().setRequest("Hello from tig server").build();
        System.out.println(keyStub.helloTigKey(request).getRequest());

        //Connect to backup server
        ManagedChannel bdChannel = NettyChannelBuilder.forTarget(args[6])
                .sslContext(context)
                .build();
        TigBackupServiceGrpc.TigBackupServiceBlockingStub backupStub = TigBackupServiceGrpc.newBlockingStub(bdChannel);
        TigServiceImpl.backupStub = backupStub;

        //Test Purposes only
        System.out.println("Connected and authenticated to backup server successfully");
        Tig.HelloTigBackupRequest bdRequest = Tig.HelloTigBackupRequest.newBuilder().setRequest("Hello from tig server").build();
        System.out.println(backupStub.helloTigBackup(bdRequest).getRequest());

        //Start server
        final Server server = NettyServerBuilder
                .forPort(port)
                .useTransportSecurity(certChainFile, privateKeyFile)
                .intercept(new ExceptionHandler())
                .addService(impl)
                .addService(customImpl)
                .build();

        server.start();

        logger.info("Server started");

        //So we can use CTRL-C when testing
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutdown Signal: Shutting down server");
                server.shutdownNow();
            }
        });

        //Cleanup hanging Session Tokens in the background
        new Thread(new TokenCleanupThread()).start();

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }
}
