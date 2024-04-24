package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class DeleteConversation extends SocketTalk {
    public DeleteConversation(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conId) {
        super(socket, socketIn, socketOut, SocketTalk.CONV_CODE_DEL_CONV, Map.of(
                "conId", conId
        ));
    }
}
