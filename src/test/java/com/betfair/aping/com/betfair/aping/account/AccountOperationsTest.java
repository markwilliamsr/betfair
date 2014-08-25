package com.betfair.aping.com.betfair.aping.account;

import com.betfair.aping.JsonRpcTestBase;
import com.betfair.aping.entities.AccountFundsResponse;
import com.betfair.aping.exceptions.APINGException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by markwilliams on 25/08/2014.
 */
public class AccountOperationsTest extends JsonRpcTestBase {

    @Test
    public void getAvailableToBetBalanceTest() throws APINGException, Exception {
        AccountFundsResponse accountFundsResponse = getAvailableToBetBalance();
        Assert.assertNotNull(accountFundsResponse);
    }

    private AccountFundsResponse getAvailableToBetBalance() throws APINGException {
        AccountFundsResponse accountFundsResponse = jsonOperations.getAccountFunds(applicationKey, sessionToken);
        return accountFundsResponse;
    }
}
