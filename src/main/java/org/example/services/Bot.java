package org.example.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.ChatPermissions;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.BanChatMember;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.example.dtos.UserDto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Bot {
    private Long chatId;
    private TelegramBot telegramBot;
//    private BannedMembersDao dao;

    Set<UserDto> uncheckedUsers = Collections.synchronizedSet(new HashSet<>());

    public Bot(String token, Long chatId) {
        this.chatId = chatId;
        this.telegramBot = new TelegramBot(token);
//        this.dao = new BannedMembersDaoImpl();
        runListener();
    }

    private void runListener() {
        this.telegramBot.setUpdatesListener(updates -> {
            CompletableFuture.runAsync(() -> {
                updates.forEach(this::startCheck);
                updates.forEach(this::finishCheck);
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void startCheck(Update update) {
        if (update == null || update.message() == null || update.message().newChatMembers() == null || update.message().newChatMembers().length == 0) {
            return;
        }

        User user = update.message().newChatMembers()[0];
        setCanSendMessage(user, false);

        String message = Library.questionText;
        SendMessage sendMessage = new SendMessage(chatId, message)
                .replyMarkup(Buttons.getKeyboard(Library.yesText, Library.noText))
                .replyToMessageId(update.message().messageId());

        SendResponse execute = this.telegramBot.execute(sendMessage);

        UserDto dto = UserDto.builder()
                .userId(user.id())
                .username(user.firstName())
                .joinMessageId(update.message().messageId())
                .questionMessageId(execute.message().messageId())
                .build();

        waitAnswer(dto);

        uncheckedUsers.add(dto);
    }

    private void finishCheck(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        if (callbackQuery == null) {
            return;
        }
        Long userId = callbackQuery.from().id();
        Integer integer = callbackQuery.message().messageId();

        UserDto userDto = uncheckedUsers.stream()
                .filter(dto -> dto.getUserId().equals(userId) && dto.getQuestionMessageId().equals(integer))
                .findAny()
                .orElse(null);

        if (userDto == null) {
            return;
        }

        String data = callbackQuery.data();
        if (data.equals("Y")) {
            congratMember(update, userDto);
        } else if (data.equals("N")) {
            kickMember(update, userDto);
        }
        uncheckedUsers.remove(userDto);
        telegramBot.execute(new DeleteMessage(chatId, userDto.getQuestionMessageId()));
    }

    private void waitAnswer(UserDto dto) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(120_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!uncheckedUsers.contains(dto)) {
                return;
            }

            uncheckedUsers.remove(dto);
            telegramBot.execute(new DeleteMessage(chatId, dto.getQuestionMessageId()));
            telegramBot.execute(new BanChatMember(chatId, dto.getUserId()));
            sendReply(Library.slowpokeText, dto.getJoinMessageId());
//            dao.insert(dto);
        });
    }

    private void congratMember(Update update, UserDto dto) {
        User from = update.callbackQuery().from();
        setCanSendMessage(from, true);
        sendReply(Library.congratText, dto.getJoinMessageId());
    }

    private void kickMember(Update update, UserDto dto) {
        Long id = update.callbackQuery().from().id();
        telegramBot.execute(new BanChatMember(chatId, id));
        sendReply(Library.banText, dto.getJoinMessageId());
//        dao.insert(dto);
    }

    private void setCanSendMessage(User user, boolean isCan) {
        ChatPermissions chatPermissions = new ChatPermissions()
                .canSendMessages(isCan);
        RestrictChatMember restrictChatMember = new RestrictChatMember(chatId, user.id(), chatPermissions);
        telegramBot.execute(restrictChatMember);
    }

    private void sendReply(String text, int messageId) {
        this.telegramBot.execute(new SendMessage(chatId, text).replyToMessageId(messageId));
    }
}
