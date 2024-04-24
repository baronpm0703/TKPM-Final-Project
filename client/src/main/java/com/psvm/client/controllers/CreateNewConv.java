package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class CreateNewConv extends SocketTalk {
    public CreateNewConv(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String newConId, String conName, boolean isGroup ) {
        super(socket, socketIn, socketOut, SocketTalk.CONV_CODE_CREATE_NEW_CONV, Map.of(
                "newConId", newConId,
                "conName", conName,
                "type", isGroup
        ));
    }
}


