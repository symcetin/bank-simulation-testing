package com.cydeo.banksimulation.service.impl;

import com.cydeo.banksimulation.dto.SmsRequestDTO;
import com.cydeo.banksimulation.service.SmsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(value = "mock.smsservice.enable", havingValue = "true", matchIfMissing = false)
@Service
public class MockSmsServiceImpl implements SmsService{
    @Override
    public String sendSms(SmsRequestDTO smsRequestDTO) {
        return "ok";
    }
}
