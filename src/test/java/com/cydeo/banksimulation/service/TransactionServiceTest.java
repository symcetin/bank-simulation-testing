package com.cydeo.banksimulation.service;

import com.cydeo.banksimulation.dto.AccountDTO;
import com.cydeo.banksimulation.enums.AccountStatus;
import com.cydeo.banksimulation.enums.AccountType;
import com.cydeo.banksimulation.exception.*;
import com.cydeo.banksimulation.mapper.TransactionMapper;
import com.cydeo.banksimulation.repository.TransactionRepository;
import com.cydeo.banksimulation.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private AccountService accountService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    public void should_make_transfer(){
        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,true,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.ACTIVE,true,124L,AccountType.CHECKINGS);

        //when we call account service, I need to manually return sender and receiver
        when(accountService.retrieveById(1L)).thenReturn(sender);
        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));

        //that means will not get exception
        assertNull(throwable);
    }

    @Test
    public void should_throw_bad_request_exception_when_sender_account_is_null(){
        AccountDTO sender = null;
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.ACTIVE,true,124L,AccountType.CHECKINGS);

        //when we call account service, I need to manually return sender and receiver
//        when(accountService.retrieveById(1L)).thenReturn(sender);
//        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));
        //assert that we got exception
        assertNotNull(throwable);
        //assert exception type is correct
        assertInstanceOf(BadRequestException.class,throwable);
        //verify exception message
        BadRequestException badRequestException = (BadRequestException) throwable;
        assertEquals("Sender or receiver can not be null", badRequestException.getMessage());

    }

    @Test
    public void should_throw_bad_request_exception_when_receiver_account_is_null(){
        AccountDTO sender = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.ACTIVE,true,124L,AccountType.CHECKINGS);
        AccountDTO receiver = null;

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));
        //assert that we got exception
        assertNotNull(throwable);
        //assert exception type is correct
        assertInstanceOf(BadRequestException.class,throwable);
        //verify exception message
        BadRequestException badRequestException = (BadRequestException) throwable;
        assertEquals("Sender or receiver can not be null", badRequestException.getMessage());

    }


    //write a unit test for sender and receiver id is same
    @Test
    public void should_throw_bad_request_exception_when_sender_and_receiver_is_same(){
        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,true,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(1L,new BigDecimal(300),AccountStatus.ACTIVE,true,124L,AccountType.CHECKINGS);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));

        //that means will get some exception
        assertNotNull(throwable);

        assertInstanceOf(BadRequestException.class,throwable);

        BadRequestException badRequestException = (BadRequestException) throwable;
        assertEquals("Sender account needs to be different from receiver account",badRequestException.getMessage());
    }

    @Test
    public void should_throw_bad_request_exception_when_sender_account_is_deleted(){
        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.DELETED,true,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.ACTIVE,true,124L,AccountType.CHECKINGS);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));

        //that means will get some exception
        assertNotNull(throwable);

        assertInstanceOf(BadRequestException.class,throwable);

        BadRequestException badRequestException = (BadRequestException) throwable;
        assertEquals("Sender account is deleted, you can not send money from this account",badRequestException.getMessage());
    }

    @Test
    public void should_throw_bad_request_exception_when_receiver_account_is_deleted(){
        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,true,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.DELETED,true,124L,AccountType.CHECKINGS);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));

        //that means will get some exception
        assertNotNull(throwable);

        assertInstanceOf(BadRequestException.class,throwable);

        BadRequestException badRequestException = (BadRequestException) throwable;
        assertEquals("Receiver account is deleted, you can not send money to this account",badRequestException.getMessage());
    }

    @Test
    public void should_throw_account_not_verified_exception_when_sender_account_is_not_verified(){
        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,false,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.ACTIVE,true,124L,AccountType.CHECKINGS);

        when(accountService.retrieveById(1L)).thenReturn(sender);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));

        //that means will get some exception
        assertNotNull(throwable);

        assertInstanceOf(AccountNotVerifiedException.class,throwable);

        AccountNotVerifiedException accountNotVerifiedException = (AccountNotVerifiedException) throwable;

        assertEquals("account not verified yet.",accountNotVerifiedException.getMessage());
    }

    @Test
    public void should_throw_account_not_verified_exception_when_receiver_account_is_not_verified(){
        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,true,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.ACTIVE,false,124L,AccountType.CHECKINGS);

        when(accountService.retrieveById(1L)).thenReturn(sender);
        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));

        //that means will get some exception
        assertNotNull(throwable);

        assertInstanceOf(AccountNotVerifiedException.class,throwable);

        AccountNotVerifiedException accountNotVerifiedException = (AccountNotVerifiedException) throwable;

        assertEquals("account not verified yet.",accountNotVerifiedException.getMessage());

    }

    @Test
    public void should_throw_account_ownership_exception_when_sender_account_is_saving_but_user_id_is_different(){
        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,true,123L,AccountType.SAVINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(300),AccountStatus.ACTIVE,true,124L,AccountType.CHECKINGS);

        when(accountService.retrieveById(1L)).thenReturn(sender);
        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(()->transactionService.makeTransfer(BigDecimal.TEN,new Date(),sender,receiver,"some message"));

        //assert that we got exception
        assertNotNull(throwable);
        //verify exception type is correct
        assertInstanceOf(AccountOwnerShipException.class,throwable);
        //verify exception message
        AccountOwnerShipException accountOwnerShipException = (AccountOwnerShipException) throwable;
        assertEquals("When one of the account type is SAVINGS, sender and receiver has to be same person",accountOwnerShipException.getMessage());
    }

    @Test
    public void should_throw_account_ownership_exception_when_receiver_account_is_saving_but_user_id_is_different(){

        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,true,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(150),AccountStatus.ACTIVE,true,124L,AccountType.SAVINGS);

        when(accountService.retrieveById(1L)).thenReturn(sender);
        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(() -> transactionService.makeTransfer(BigDecimal.TEN, new Date(), sender, receiver, "some message"));
        //assert that we got exception
        assertNotNull(throwable);
        //verify exception type is correct
        assertInstanceOf(AccountOwnerShipException.class,throwable);
        //verify exception message
        AccountOwnerShipException accountOwnerShipException = (AccountOwnerShipException) throwable;
        assertEquals("When one of the account type is SAVINGS, sender and receiver has to be same person",accountOwnerShipException.getMessage());
    }

    @Test
    public void should_work_when_sender_and_receiver_account_is_saving_and_user_id_is_same(){

        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(250),AccountStatus.ACTIVE,true,123L,AccountType.SAVINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(150),AccountStatus.ACTIVE,true,123L,AccountType.SAVINGS);

        when(accountService.retrieveById(1L)).thenReturn(sender);
        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(() -> transactionService.makeTransfer(BigDecimal.TEN, new Date(), sender, receiver, "some message"));
        //assert that we don't get exception
        assertNull(throwable);

    }

    @Test
    public void should_throw_balance_not_sufficient_exception_when_sender_balance_is_not_enough(){

        AccountDTO sender = prepareAccountDTO(1L,new BigDecimal(5),AccountStatus.ACTIVE,true,123L,AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L,new BigDecimal(150),AccountStatus.ACTIVE,true,123L,AccountType.CHECKINGS);

        when(accountService.retrieveById(1L)).thenReturn(sender);
        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(() -> transactionService.makeTransfer(BigDecimal.TEN, new Date(), sender, receiver, "some message"));
        //assert that we got exception
        assertNotNull(throwable);
        //verify exception type is correct
        assertInstanceOf(BalanceNotSufficientException.class,throwable);

        BalanceNotSufficientException balanceNotSufficientException = (BalanceNotSufficientException) throwable;

        assertEquals(balanceNotSufficientException.getMessage(),"Balance is not enough for this transaction");

    }

    @Test
    public void should_make_transfer_when_sender_balance_is_equal_to_amount() {
        AccountDTO sender = prepareAccountDTO(1L, new BigDecimal(10), AccountStatus.ACTIVE, true, 123L, AccountType.CHECKINGS);
        AccountDTO receiver = prepareAccountDTO(2L, new BigDecimal(150), AccountStatus.ACTIVE, true, 124L, AccountType.CHECKINGS);

        when(accountService.retrieveById(1L)).thenReturn(sender);
        when(accountService.retrieveById(2L)).thenReturn(receiver);

        Throwable throwable = catchThrowable(() -> transactionService.makeTransfer(BigDecimal.TEN, new Date(), sender, receiver, "some message"));
        //assert that we got exception
        assertNull(throwable);
    }

    private AccountDTO prepareAccountDTO(Long id, BigDecimal balance,
                              AccountStatus accountStatus,
                              boolean verified, Long userId,
                              AccountType accountType){

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(id);
        accountDTO.setBalance(balance);
        accountDTO.setAccountStatus(accountStatus);
        accountDTO.setOtpVerified(verified);
        accountDTO.setUserId(userId);
        accountDTO.setAccountType(accountType);
        accountDTO.setCreationDate(new Date());
        accountDTO.setPhoneNumber("3456543234");

return accountDTO;
    }
}
