package com.psvm.server.view.RegisterChartControl;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class UserRequest extends SocketTalk {
    public UserRequest(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String field) {
        super(clientSocket, socketIn, socketOut, SocketTalk.READ_CODE_USER_DATA, Map.of(
                "field", field //CreationDate
        ));
    }
}
