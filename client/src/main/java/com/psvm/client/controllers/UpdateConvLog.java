package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class UpdateConvLog extends SocketTalk {
    public UpdateConvLog(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conId, String userId, int logType) {
        super(socket, socketIn, socketOut, SocketTalk.CONV_CODE_UPDATE_CONV_LOG, Map.of(
                // Leave this empty
                "conId", conId,
                "userId", userId,
                "logType", logType
        ));
    }
}
