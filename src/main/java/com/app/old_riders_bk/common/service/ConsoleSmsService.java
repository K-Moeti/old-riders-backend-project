package com.app.old_riders_bk.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsoleSmsService {

    public void sendLoginOtpSms(String rawPhoneNumber, String otpCode) {
        String formattedNumber = normalizePhoneNumber(rawPhoneNumber);

        log.info("\n" +
                        "==========================================================\n" +
                        "  [SMS DISPATCH SIMULATOR]                                \n" +
                        "  To       : {}                                           \n" +
                        "  OTP Code : {}                                           \n" +
                        "  Message  : Your Old Riders code is: {}. Valid for 5m.   \n" +
                        "==========================================================",
                formattedNumber, otpCode, otpCode);
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.startsWith("0")) {
            return "+27" + clean.substring(1);
        }
        if (!clean.startsWith("+")) {
            return "+" + clean;
        }
        return clean;
    }
}