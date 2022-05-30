package org.example;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Bot {
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));
    private final static List<String> opponentWins = new ArrayList<>() {{
        add("01");
        add("12");
        add("20");
    }};

    public void serve() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();

        BaseRequest request = null;

        if(message != null && message.viaBot() != null && message.viaBot().username().equals("pojoGame_bot")) {
            InlineKeyboardMarkup replyMarkup = message.replyMarkup();
            if(replyMarkup == null) {
                return;
            }

            InlineKeyboardButton[][] buttons = replyMarkup.inlineKeyboard();

            if(buttons == null) {
                return;
            }

            InlineKeyboardButton button = buttons[0][0];
            Long chatId = message.chat().id();
            String senderName = message.from().firstName();
            String senderChose = button.callbackData();
            Integer messageId = message.messageId();

            request = new EditMessageText(chatId, messageId, message.text())
                    .replyMarkup(
                       new InlineKeyboardMarkup(
                            new InlineKeyboardButton("👻")
                                    .callbackData(String
                                            .format("%d %s %s %s", chatId, senderName, senderChose, "0")),
                            new InlineKeyboardButton("☠")
                                    .callbackData(String
                                            .format("%d %s %s %s", chatId, senderName, senderChose, "1")),
                            new InlineKeyboardButton("🤡")
                                    .callbackData(String
                                            .format("%d %s %s %s", chatId, senderName, senderChose, "2"))
                       )
                    );
        } else if(inlineQuery != null) {
            InlineQueryResultArticle boo = getPojo("boo", "👻Boo", "0");
            InlineQueryResultArticle skull = getPojo("skull", "☠Skull", "1");
            InlineQueryResultArticle clown = getPojo("clown", "🤡Clown", "2");

            request = new AnswerInlineQuery(inlineQuery.id(), boo, skull, clown);
        } else if(callbackQuery != null) {
            String[] data = callbackQuery.data().split(" ");
            Long chatId = Long.parseLong(data[0]);
            String senderName = data[1];
            String senderChose = data[2];
            String opponentChose = data[3];
            String opponentName = callbackQuery.from().firstName();

            if(senderChose.equals(opponentChose)) {
                request = new SendMessage(
                        chatId, "It`s a tie! \uD83D\uDCA9");

            } else if(opponentWins.contains(senderChose + opponentChose)) {
                request = new SendMessage(
                        chatId,
                        String.format(
                                "%s \uD83D\uDCA9 (%s) lost to %s \uD83D\uDCA9 (%s)",
                                senderName, senderChose,
                                opponentName, opponentChose
                            )
                    );
                } else {
                    request = new SendMessage(
                            chatId,
                            String.format(
                                    "%s \uD83D\uDCA9 (%s) lost to %s \uD83D\uDCA9 (%s)",
                                    opponentName, opponentChose,
                                    senderName, senderChose
                            )
                    );
                }
            }
            if (request != null) {
                bot.execute(request);
            }
    }

    private InlineQueryResultArticle getPojo(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(id, title, "I`m ready to fight!")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                            new InlineKeyboardButton("thinking...")
                                .callbackData(callbackData)));
    }
}
