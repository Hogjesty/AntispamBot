package org.example;

import org.example.services.Bot;

public class Main {

    public static void main(String[] args) {
        String token = args[0];
        long chatId = Long.parseLong(args[1]);
        new Bot(token, chatId);
    }

}

