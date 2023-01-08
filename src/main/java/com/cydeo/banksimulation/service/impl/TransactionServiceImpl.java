package com.cydeo.banksimulation.service.impl;

import com.cydeo.banksimulation.dto.AccountDTO;
import com.cydeo.banksimulation.dto.TransactionDTO;
import com.cydeo.banksimulation.entity.Account;
import com.cydeo.banksimulation.entity.Transaction;
import com.cydeo.banksimulation.enums.AccountStatus;
import com.cydeo.banksimulation.enums.AccountType;
import com.cydeo.banksimulation.exception.*;
import com.cydeo.banksimulation.mapper.TransactionMapper;
import com.cydeo.banksimulation.repository.TransactionRepository;
import com.cydeo.banksimulation.service.AccountService;
import com.cydeo.banksimulation.service.TransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Value("${under_construction}")
    private boolean underConstruction;

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(AccountService accountService, TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public TransactionDTO makeTransfer(BigDecimal amount, Date creationDate, AccountDTO sender, AccountDTO receiver, String message) {

        if (!underConstruction) {
            validateAccounts(sender, receiver);
            checkAccountOwnerShip(sender, receiver);
            executeBalanceAndUpdateIfRequired(amount, sender, receiver);
            TransactionDTO transactionDTO = new TransactionDTO(sender, receiver, amount, message, creationDate);

            transactionRepository.save(transactionMapper.convertToEntity(transactionDTO));

            return transactionDTO;

        } else {
            throw new UnderConstructionException("Make transfer is not possible for now. Please try again later");
        }

    }

    public void checkAccountVerification(AccountDTO sender){
        if (!sender.getOtpVerified()){
            throw new AccountNotVerifiedException("account not verified yet.");
        }
    }

    private void executeBalanceAndUpdateIfRequired(BigDecimal amount, AccountDTO sender, AccountDTO receiver) {

        if (checkSenderBalance(sender, amount)) {
            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));

            AccountDTO senderAcc = accountService.retrieveById(sender.getId());
            senderAcc.setBalance(sender.getBalance());

            accountService.createNewAccount(senderAcc);

            AccountDTO receiverAcc = accountService.retrieveById(receiver.getId());
            receiverAcc.setBalance(receiver.getBalance());

            accountService.createNewAccount(receiverAcc);

        } else {
            throw new BalanceNotSufficientException("Balance is not enough for this transaction");
        }

    }

    private boolean checkSenderBalance(AccountDTO sender, BigDecimal amount) {
        return accountService.retrieveById(sender.getId()).getBalance().subtract(amount).compareTo(BigDecimal.ZERO) > 0;
    }

    private void validateAccounts(AccountDTO sender, AccountDTO receiver) {

        if (sender == null || receiver == null) {
            throw new BadRequestException("Sender or receiver can not be null");
        }

        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("Sender account needs to be different from recaiver account");
        }

        if (sender.getAccountStatus().equals(AccountStatus.DELETED)) {
            throw new BadRequestException("Sender account is deleted, you can not send money from this account");
        }

        if (receiver.getAccountStatus().equals(AccountStatus.DELETED)) {
            throw new BadRequestException("Receiver account is deleted, you can not send money to this account");
        }

        AccountDTO senderAccount = accountService.retrieveById(sender.getId());
        AccountDTO receiverAccount = accountService.retrieveById(receiver.getId());
        checkAccountVerification(senderAccount);
        checkAccountVerification(receiverAccount);

    }

    private void checkAccountOwnerShip(AccountDTO sender, AccountDTO receiver) {

        if ((sender.getAccountType().equals(AccountType.SAVINGS) ||
                receiver.getAccountType().equals(AccountType.SAVINGS))
                && !sender.getUserId().equals(receiver.getUserId())) {
            throw new AccountOwnerShipException("When one of the account type is SAVINGS, sender and receiver has tobe same person");
        }

    }

    @Override
    public List<TransactionDTO> findAll() {
        return transactionRepository.findAll().stream().map(transactionMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> retrieveLastTransactions() {
        List<Transaction> transactionList = transactionRepository.findLastTenTransactions();
        return transactionList.stream().map(transactionMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> findTransactionListByAccountId(Long id) {
        return transactionRepository.findTransactionListById(id).stream().map(transactionMapper::convertToDto).collect(Collectors.toList());
    }

}