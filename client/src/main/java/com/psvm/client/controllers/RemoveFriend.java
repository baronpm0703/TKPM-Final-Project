package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class RemoveFriend extends SocketTalk {
    public RemoveFriend(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String userId) {
        super(socket, socketIn, socketOut, SocketTalk.FRIEND_CODE_UNFRIEND, Map.of(
                "userId", LocalData.getCurrentUsername(),
                "friendId", userId
        ));
    }
}

