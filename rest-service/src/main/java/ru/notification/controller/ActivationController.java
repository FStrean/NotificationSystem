package ru.notification.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.notification.service.UserActivationService;

@RequestMapping("/user")
@Controller
public class ActivationController {
    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    @GetMapping("/activation")
    public String activation(@RequestParam("id") String id, Model model) {
        var res = userActivationService.activation(id);
        if (res) {
            //TODO сделать так, чтобы при активации/не активации сообщение о результате отправлял бот, а ссылка активации открывала бота
            model.addAttribute("message", "Ваша учетная запись активирована");
            return "success";
        }
        //TODO поработать над ошибками и сделать advice controller
        model.addAttribute("message", "Ошибка во время активации вашей учетной записи!");
        return "error";
    }
}
