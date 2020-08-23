package com.stfalcon.chatkit.sample.utils;

import com.stfalcon.chatkit.sample.common.data.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageHistory {
    private static  MessageHistory instance;

    private List<Message> messages;

    public static synchronized MessageHistory getInstance() {
        if (instance != null) {
            instance = new MessageHistory();
        }
        return instance;
    }

    private MessageHistory(){
        messages = new ArrayList<>();
    }


}
