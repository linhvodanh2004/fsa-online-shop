package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Order;
import jakarta.mail.MessagingException;

public interface EmailSenderService {
    void sendVerificationCode(String email, String username, String code) throws MessagingException;

    void notifyOrderPending(Order order) throws MessagingException;

    void notifyOrderTransit(Order order) throws MessagingException;

    void notifyOrderDelivered(Order order) throws MessagingException;

    void notifyOrderCancelled(Order order) throws MessagingException;
}