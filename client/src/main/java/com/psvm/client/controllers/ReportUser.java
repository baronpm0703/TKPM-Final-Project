package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ReportUser extends SocketTalk {
    public ReportUser(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String userId) {
        super(socket, socketIn, socketOut, SocketTalk.REPORT_CODE_REPORT_USER, Map.of(
                "reporterId", LocalData.getCurrentUsername(),
                "reportedId", userId
        ));
    }
}

