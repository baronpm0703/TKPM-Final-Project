package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class GetConversationInfo extends SocketTalk {
    public GetConversationInfo(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conId) {
        super(socket, socketIn, socketOut, SocketTalk.CONV_CODE_GET_MEM_INFO, Map.of(
                "conId", conId
        ));
    }
}

