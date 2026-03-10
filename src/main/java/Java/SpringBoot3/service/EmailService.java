package Java.SpringBoot3.service;

import Java.SpringBoot3.enumeration.VerificationType;

public interface EmailService {
    void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType verificationType);

}
