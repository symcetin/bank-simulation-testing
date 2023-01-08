package com.cydeo.banksimulation.service;

import com.cydeo.banksimulation.dto.SmsRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public interface SmsService {
    String sendSms(SmsRequestDTO smsRequestDTO);

}
