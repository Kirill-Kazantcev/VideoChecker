package com.vk.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Базовый класс для тестирования VK Видео.
 * <p>
 * Предоставляет общую функциональность:
 * <ul>
 *     <li>Настройку WebDriver с использованием профиля Chrome для сохранения авторизации</li>
 *     <li>Методы для создания скриншотов и сохранения HTML-страниц</li>
 *     <li>Методы для надёжного клика по элементам (с обходом перекрытий)</li>
 *     <li>Методы для маскировки автоматизации (stealth)</li>
 * </ul>
 * </p>
 */
public class VkBaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    protected static final String USER_DATA_DIR = System.getProperty("user.dir") + "/chrome-vk-profile";

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = getChromeOptions();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(45));
        js = (JavascriptExecutor) driver;
        applyStealth();
    }

    protected ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        Path profilePath = Paths.get(USER_DATA_DIR);

        if (!Files.exists(profilePath)) {
            try {
                Files.createDirectories(profilePath);
                System.out.println("Создан новый профиль VK: " + USER_DATA_DIR);
                System.out.println("При первом запуске войдите в VK вручную");
            } catch (IOException e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        } else {
            System.out.println("Используем профиль VK: " + USER_DATA_DIR);
        }

        options.addArguments("--user-data-dir=" + USER_DATA_DIR);
        options.addArguments("--profile-directory=Default");
        options.addArguments("--window-size=1280,720");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--lang=ru-RU");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        if ("true".equals(System.getProperty("CI"))) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }
        return options;
    }

    protected void applyStealth() {
        js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        js.executeScript("Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]})");
    }

    protected void takeScreenshot(String name) {
        try {
            File dir = new File("test-results/screenshots/");
            if (!dir.exists()) dir.mkdirs();
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, new File(dir, name + ".png"));
            System.out.println("Скриншот: " + name);
        } catch (IOException e) {
            System.err.println("Ошибка скриншота: " + e.getMessage());
        }
    }

    protected void savePageSource(String name) {
        try {
            File dir = new File("test-results/html/");
            if (!dir.exists()) dir.mkdirs();
            FileUtils.writeStringToFile(new File(dir, name + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
            System.out.println("HTML сохранён: " + name);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения HTML: " + e.getMessage());
        }
    }

    protected void clickElement(WebElement element) {
        try {
            element.click();
            System.out.println("Клик выполнен обычным способом");
        } catch (Exception e1) {
            try {
                js.executeScript("arguments[0].click();", element);
                System.out.println("Клик выполнен через JavaScript");
            } catch (Exception e2) {
                try {
                    js.executeScript("arguments[0].scrollIntoView(true);", element);
                    Thread.sleep(500);
                    element.click();
                    System.out.println("Клик выполнен после прокрутки");
                } catch (Exception e3) {
                    org.openqa.selenium.interactions.Actions actions =
                            new org.openqa.selenium.interactions.Actions(driver);
                    actions.moveToElement(element).click().perform();
                    System.out.println("Клик выполнен через Actions");
                }
            }
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("Браузер закрыт, профиль сохранён");
        }
    }
}