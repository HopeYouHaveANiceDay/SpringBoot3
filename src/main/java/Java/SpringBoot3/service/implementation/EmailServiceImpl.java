package Java.SpringBoot3.service.implementation;

import Java.SpringBoot3.enumeration.VerificationType;
import Java.SpringBoot3.exception.ApiException;
import Java.SpringBoot3.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    //bring Mail sender after inject dependency
    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("KatieFan@gmail.com"); //should be a real gmail
            message.setTo(email);
            message.setText(getEmailMessage(firstName, verificationUrl, verificationType));
            // StringUtils.capitalize(...)
            //      StringUtils 是 Apache Commons Lang 库中的一个工具类，里面提供了很多字符串处理方法。
            //      capitalize 方法的作用是：将传入字符串的首字母变为大写，其余部分保持不变。
            //      举例：StringUtils.capitalize("account") → "Account"。
            // %s => is account or password
            message.setSubject(String.format("HopeYouHaveANiceDay - %s Verification Email", StringUtils.capitalize(verificationType.getType())));
            mailSender.send(message);
            log.info("Email sent to {}", firstName);
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }

    private String getEmailMessage(String firstName, String verificationUrl, VerificationType verificationType) {
        switch (verificationType) {
            case PASSWORD -> { return  "Hello " + firstName + "\n\nReset password request.Please click the link below to reset your password. \n\n" + verificationUrl + "\n\nThe Support Team"; }
            case ACCOUNT -> { return  "Hello " + firstName + "\n\nYour new account has been created.Please click the link below to verify your account. \n\n" + verificationUrl + "\n\nThe Support Team"; }
            default -> throw new ApiException("Unable to send email. Email type unknown");

        }
    }
}
