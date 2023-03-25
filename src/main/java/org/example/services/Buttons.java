package org.example.services;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

public class Buttons {
        public static InlineKeyboardMarkup getKeyboard(String yesText, String noText) {
        InlineKeyboardButton b1 = new InlineKeyboardButton(yesText)
                .callbackData("Y");
        InlineKeyboardButton b2 = new InlineKeyboardButton(noText)
                .callbackData("N");

        return new InlineKeyboardMarkup().addRow(b1, b2);
    }
}
