package tk.chaber.sfn2021rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import tk.chaber.sfn2021rest.db.RegisterUserDto;
import tk.chaber.sfn2021rest.db.UserAlreadyExistsException;
import tk.chaber.sfn2021rest.db.UserDto;
import tk.chaber.sfn2021rest.db.UserService;
import tk.chaber.sfn2021rest.db.entities.User;
import tk.chaber.sfn2021rest.db.entities.VerificationToken;
import tk.chaber.sfn2021rest.response.Error;
import tk.chaber.sfn2021rest.response.EventResponding;
import tk.chaber.sfn2021rest.response.FailedResponse;
import tk.chaber.sfn2021rest.response.SuccessResponse;
import tk.chaber.sfn2021rest.socket.EventsEnum;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;

@RestController
public class RegistrationController {
    @Autowired
    private UserService service;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @PostMapping(path = "/user/register", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    EventResponding registerUserAccount(@RequestBody @Valid RegisterUserDto userDto, HttpServletRequest request){
        try{
            User registered = service.registerNewUserAccount(userDto);

            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(appUrl, registered));
        }catch(UserAlreadyExistsException ex){
            System.out.println("USER EXISTS");
            return new FailedResponse(EventsEnum.REGISTER_USER, Error.USER_ALREADY_EXISTS);
        }
        return new SuccessResponse(EventsEnum.REGISTER_USER);
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(WebRequest request, @RequestParam("token") String token){
        Locale locale = request.getLocale();

        VerificationToken verificationToken = service.getVerificationToken(token);

        if(verificationToken == null){
            //token is null
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0){
            //Out of time
        }

        user.setEnabled(true);
        service.saveRegisteredUser(user);
        return "redirect:/";
    }
}
