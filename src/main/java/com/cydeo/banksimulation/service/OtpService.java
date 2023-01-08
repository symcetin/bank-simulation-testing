package com.cydeo.banksimulation.service;

import com.cydeo.banksimulation.dto.OtpDTO;
import com.cydeo.banksimulation.entity.Account;

public interface OtpService {
    OtpDTO createOtpSendSms(Account account);

    void confirmOtp(Integer otpCode, Long otpId);
}
