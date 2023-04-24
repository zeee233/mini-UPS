package org.example.utils;

import java.io.IOException;
import java.net.Socket;

import com.google.protobuf.*;

public class CommHelper {
    public static <T extends GeneratedMessageV3.Builder<?>> boolean sendMSG(T builder, Socket socket) {
        try {
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(socket.getOutputStream());
            codedOutputStream.writeUInt32NoTag(builder.build().toByteArray().length);
            builder.build().writeTo(codedOutputStream);
            codedOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T extends GeneratedMessageV3.Builder<?>> boolean recvMSG(T builder, Socket socket) {
        try {
            CodedInputStream codedInputStream = CodedInputStream.newInstance(socket.getInputStream());
            int length = codedInputStream.readRawVarint32();
            int parseLimit = codedInputStream.pushLimit(length);
            builder.mergeFrom(codedInputStream);
            codedInputStream.popLimit(parseLimit);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
