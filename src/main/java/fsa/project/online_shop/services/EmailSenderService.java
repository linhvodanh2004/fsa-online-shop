package fsa.project.online_shop.services;

import jakarta.mail.MessagingException;

public interface EmailSenderService {
    public void sendVerificationCode(String email, String username, String code) throws MessagingException;
}
