package org.example.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.example.protoc.UpsAmazon.AUCommunication;

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

    public static <T extends GeneratedMessageV3> AUCommunication.Builder recvMSG2(Socket socket) {
        try {
            // CodedInputStream codedInputStream =
            // CodedInputStream.newInstance(socket.getInputStream());
            // int length = codedInputStream.readRawVarint32();
            // int parseLimit = codedInputStream.pushLimit(length);
            // builder.mergeFrom(codedInputStream,
            // ExtensionRegistryLite.getEmptyRegistry());
            // codedInputStream.popLimit(parseLimit);
            // return true;
            InputStream input = socket.getInputStream();
            CodedInputStream codedInput = CodedInputStream.newInstance(input);
            AUCommunication.Builder builder = AUCommunication.newBuilder();
            builder.mergeFrom(codedInput, ExtensionRegistryLite.getEmptyRegistry());
            return builder;
        } catch (IOException e) {
            e.printStackTrace();
            // return false;
            return null;
        }
    }

    public static AUCommunication.Builder receiveAUCommunication(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        int msgLen = codedInputStream.readRawVarint32();
        byte[] wholeMsg = codedInputStream.readRawBytes(msgLen);
        return AUCommunication.parseFrom(wholeMsg).toBuilder();
    }
}
