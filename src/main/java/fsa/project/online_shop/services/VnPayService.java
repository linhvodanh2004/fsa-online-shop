package fsa.project.online_shop.services;

import fsa.project.online_shop.dtos.VnPayRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VnPayService {
    public String createVnPayPayment(VnPayRequest vnPayRequest, String ipAddress, String txnRef);
    public int vnPayReturn(HttpServletRequest request);
}
