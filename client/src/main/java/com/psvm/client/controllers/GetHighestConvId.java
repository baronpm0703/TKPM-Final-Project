package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class GetHighestConvId extends SocketTalk {
    public GetHighestConvId(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut) {
        super(socket, socketIn, socketOut, SocketTalk.CONV_CODE_GET_HIGHEST_ID, Map.of(
                // Leave this empty
        ));
    }
}


