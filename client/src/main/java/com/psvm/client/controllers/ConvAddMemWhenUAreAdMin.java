package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ConvAddMemWhenUAreAdMin extends SocketTalk {
    public ConvAddMemWhenUAreAdMin(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conId, String userId) {
        super(socket, socketIn, socketOut, SocketTalk.CONV_CODE_ADD_MEM_WHEN_U_ARE_ADMIN, Map.of(
                // Leave this empty
                "conId", conId,
                "adMin", LocalData.getCurrentUsername(),
                "userId", userId
        ));
    }
}


