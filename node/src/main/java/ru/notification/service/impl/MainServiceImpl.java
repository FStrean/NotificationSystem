package ru.notification.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.notification.dao.AppUserDAO;
import ru.notification.dao.RawDataDAO;
import ru.notification.entity.AppUser;
import ru.notification.entity.RawData;
import ru.notification.service.AppUserService;
import ru.notification.service.MainService;
import ru.notification.service.ProducerService;
import ru.notification.service.enums.ServiceCommand;
import ru.notification.youtube.ChannelManager;
import ru.notification.youtube.OAuthManager;

import java.util.Arrays;
import java.util.stream.Collectors;

import static ru.notification.entity.enums.UserState.*;
import static ru.notification.service.enums.ServiceCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final AppUserService appUserService;
    private final ChannelManager channelManager;
    private final OAuthManager oAuthManager;

    private final String helpList = generateHelpList();

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, AppUserService appUserService, ChannelManager channelManager, OAuthManager oAuthManager) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.appUserService = appUserService;
        this.channelManager = channelManager;
        this.oAuthManager = oAuthManager;
    }

    private String generateHelpList() {
        return "Список доступных команд: " + Arrays.stream(ServiceCommand.values()).map(
                command -> "\n" + command.getValue() + " - " + command.getDescription() + ";")
                .collect(Collectors.joining());
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";
        var serviceCommand = ServiceCommand.fromValue(text);
        try {
            if (CANCEL.equals(serviceCommand)) {
                output = cancelProcess(appUser);
            } else if (BASIC_STATE.equals(userState)) {
                output = processServiceCommand(appUser, serviceCommand);
            } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
                output = appUserService.setEmail(appUser, text);
            } else if (WAIT_FOR_YOUTUBE_LINK_STATE.equals(userState)) {
                output = channelManager.addSubscriptionToNotifyFromUrl(appUser, text);
            } else {
                log.debug("Unknown user state: " + userState);
                output = "Необработанное состояние пользователя " + userState + ", пожалуйста, свяжитесь с поддержкой!";
            }
        } catch (Exception e) {
            output = "Произошла неизвестная ошибка! Пожалуйста свяжитесь с поддержкой для ее решения!";
            log.error(e);
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, ServiceCommand cmd) {
        if (REGISTRATION.equals(cmd)) {
            return appUserService.registerUser(appUser);
        } else if (HELP.equals(cmd)) {
            return helpList;
        } else if (START.equals(cmd)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return processAnotherServiceCommand(appUser, cmd);
        }
    }

    private String processAnotherServiceCommand(AppUser appUser, ServiceCommand cmd) {
        var result = checkIfAllowedToProcess(appUser);
        if (!result.isEmpty()) {
            return result;
        }

        if (YOUTUBE_ADD.equals(cmd)) {
            appUser.setState(WAIT_FOR_YOUTUBE_LINK_STATE);
            appUserDAO.save(appUser);
            return "Введите ссылку на канал, о котором хотите получать уведомления";
        } else if (YOUTUBE_AUTHORIZE.equals(cmd)) {
            return "Перейдите по ссылке: " + oAuthManager.getAuthorizeLink(appUser);
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String checkIfAllowedToProcess(AppUser appUser) {
        var result = "";
        if(!appUser.getIsActive()) {
            result =  "Вы не зарегистрированы! Пройдите регистрацию, написав /registration";
        }

        return result;
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);

        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        var telegramUser = update.getMessage().getFrom();
        var optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (optional.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .chatId(update.getMessage().getChatId())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();

            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();

        rawDataDAO.save(rawData);
    }
}
