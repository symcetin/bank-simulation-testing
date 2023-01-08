package com.cydeo.banksimulation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private AccountDTO sender;
    private AccountDTO receiver;
    private BigDecimal amount;
    private String message;
    private Date creationDate;
}
