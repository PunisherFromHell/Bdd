package ru.netology.web.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.*;


public class MoneyTransferTest {
    DashboardPage dashboardPage;
    DataHelper.CardInfo firstCardInfo;
    DataHelper.CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;


    @BeforeEach
    void setup() {
     //   ChromeOptions options = new ChromeOptions();
      //  options.addArguments("--start-maximized");
      //  Map<String, Object> prefs = new HashMap<String, Object>();
      //  prefs.put("credentials_enable_service", false);
       // prefs.put("password_manager_enabled", false);
      //  options.setExperimentalOption("prefs", prefs);
      //  Configuration.browserCapabilities = options;


            var loginPage = open("http://localhost:9999", LoginPage.class);
        var authInfo = getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = getVerificationCode();
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = getFirstCardInfo();
        secondCardInfo = getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        secondCardBalance = dashboardPage.getCardBalance(1);
    }

    @Test
    void shouldTransferToSecond() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        dashboardPage.reloadDashboardPage();
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        var actualBalanceSecondCard = dashboardPage.getCardBalance(1);
        assertAll(() -> assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                () -> assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));

    }

    @Test
    void shouldTransferToFirst() {
        var amount = generateValidAmount(secondCardBalance);
        var expectedBalanceFirstCard = firstCardBalance + amount;
        var expectedBalanceSecondCard = secondCardBalance - amount;
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), secondCardInfo);
        dashboardPage.reloadDashboardPage();
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        var actualBalanceSecondCard = dashboardPage.getCardBalance(1);
        assertAll(() -> assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                () -> assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
    }

    @Test
    void shouldShowErrorMessageWhenInvalidAmount() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        transferPage.findErrorMessage("Недостаточно средств на счету");
        dashboardPage.reloadDashboardPage();
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
        assertAll(() -> assertEquals(firstCardBalance, actualBalanceFirstCard),
                () -> assertEquals(secondCardBalance, actualBalanceSecondCard));
    }
}