package tig.grpc.server.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigServiceGrpc;
import io.grpc.stub.StreamObserver;
import tig.grpc.server.data.dao.UsersDAO;
import tig.grpc.server.data.dao.FileDAO;

public class TigServiceImpl extends TigServiceGrpc.TigServiceImplBase {
    private final static Logger logger = Logger.getLogger(TigServiceImpl.class);

    @Override
    public void register(Tig.LoginRequest request, StreamObserver<Tig.StatusReply> responseObserver) {
        logger.info(String.format("Register username:%s", request.getUsername()));

        Tig.StatusReply.Builder builder = Tig.StatusReply.newBuilder();
        UsersDAO.insertUser(request.getUsername(), request.getPassword());

        builder.setCode(Tig.StatusCode.OK);

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(Tig.LoginRequest request, StreamObserver<Tig.LoginReply> responseObserver) {

    }

    @Override
    public void logout(Tig.SessionRequest request, StreamObserver<Empty> responseObserver) {

    }

    @Override
    public void deleteFile(Tig.FileRequest request, StreamObserver<Tig.StatusReply> responseObserver) {

    }

    @Override
    public void accessControlFile(Tig.OperationRequest request, StreamObserver<Tig.StatusReply> responseObserver) {

    }

    @Override
    public void listFiles(Tig.SessionRequest request, StreamObserver<Tig.ListFilesReply> responseObserver) {

    }

    @Override
    public StreamObserver<Tig.FileChunk> uploadFile(StreamObserver<Tig.StatusReply> responseObserver) {
        return new StreamObserver<Tig.FileChunk>() {

            private ByteString file = ByteString.EMPTY;
            private String filename;

            @Override
            public void onNext(Tig.FileChunk value) {
                filename = value.getFileName();
                file.concat(value.getContent());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Tig.StatusReply.newBuilder().setCode(Tig.StatusCode.OK).build());
                FileDAO.fileUpload(filename, file);

            }

        };

    }

    @Override
    public StreamObserver<Tig.FileChunk> editFile(StreamObserver<Tig.StatusReply> responseObserver) {
        return null;
    }

    @Override
    public void downloadFile(Tig.FileRequest request, StreamObserver<Tig.FileChunk> responseObserver) {

    }

}