package ru.notification.service.impl;

import org.springframework.stereotype.Service;
import ru.notification.dao.AppUserDAO;
import ru.notification.service.UserActivationService;
import ru.notification.utils.CryptoTool;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        //TODO сделать расшифровку id и проверять время, когда оно было создано и если прошло слишком много времени, то не активировать
        var userId = cryptoTool.idOf(cryptoUserId);
        if(userId.isEmpty()) {
            return false;
        }
        var optional = appUserDAO.findById(userId.get());
        if (optional.isPresent()) {
            var user = optional.get();
            user.setIsActive(true);
            appUserDAO.save(user);
            return true;
        }
        return false;
    }
}
