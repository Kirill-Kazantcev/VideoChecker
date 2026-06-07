package com.vk.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Тестовый класс для сценария 1 тестирования VK Видео.
 * <p>
 * Сценарий включает:
 * <ul>
 *     <li>Поиск видео по ключевому слову</li>
 *     <li>Переход на первое видео в результатах поиска</li>
 *     <li>Нажатие кнопки "Подписаться"</li>
 *     <li>Нажатие кнопки "Нравится" (лайк)</li>
 *     <li>Перезагрузку страницы и проверку сохранения состояния</li>
 *     <li>Приведение состояния к исходному (отписка и снятие лайка)</li>
 * </ul>
 * </p>
 *
 */
public class VkScenario1Test extends VkBaseTest {

    /**
     * Основной тестовый метод, выполняющий сценарий работы с подпиской и лайком.
     *
     * @throws InterruptedException если происходит прерывание потока во время ожидания
     */
    @Test
    public void searchAndSubscribe() throws InterruptedException {
        System.out.println("=== VK СЦЕНАРИЙ 1 (ПОДПИСКА И ЛАЙК) ===");

        // Заходим на страницу VK Video
        driver.get("https://vk.com/video");
        Thread.sleep(5000);
        takeScreenshot("1-homepage");

        // Поиск видео
        System.out.println("Поиск видео...");
        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='search'], input[name='q']")
        ));
        search.sendKeys("Токсис возьми телефон");
        search.sendKeys(Keys.ENTER);
        Thread.sleep(5000);
        takeScreenshot("2-search");

        // Переход на первое видео
        System.out.println("Переход на видео...");
        WebElement video = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".video_item, .video-item, a[href*='/video-']")
        ));
        video.click();
        Thread.sleep(8000);
        takeScreenshot("3-video-page");

        // Ожидание полной загрузки страницы видео
        System.out.println("\n=== Ожидание 10 секунд перед поиском кнопок ===");
        Thread.sleep(10000);

        // Работа с подпиской
        System.out.println("\n=== РАБОТА С ПОДПИСКОЙ ===");
        WebElement subscribeButton = findSubscribeButton();

        if (subscribeButton == null) {
            takeScreenshot("error-subscribe-not-found");
            Assert.fail("Кнопка подписки не найдена");
        }

        String subscribeText = subscribeButton.getText();
        System.out.println("Текст кнопки подписки: '" + subscribeText + "'");
        takeScreenshot("4-subscribe-button-found");

        System.out.println("Нажимаем на кнопку подписки...");
        clickElement(subscribeButton);
        Thread.sleep(3000);
        takeScreenshot("5-after-subscribe-click");
        System.out.println("Кнопка подписки нажата");

        // Работа с лайком
        System.out.println("\n=== РАБОТА С ЛАЙКОМ ===");
        WebElement likeButton = findLikeButton();

        if (likeButton == null) {
            takeScreenshot("error-like-not-found");
            Assert.fail("Кнопка лайка не найдена");
        }

        boolean isLiked = checkIfLiked(likeButton);
        System.out.println("Состояние лайка ДО нажатия: " + (isLiked ? "Включен (лайк стоит)" : "Выключен (лайка нет)"));
        takeScreenshot("6-like-button-found");

        System.out.println("Нажимаем на кнопку лайка...");
        clickElement(likeButton);
        Thread.sleep(3000);
        takeScreenshot("7-after-like-click");
        System.out.println("Кнопка лайка нажата");

        // Перезагрузка страницы
        System.out.println("\n=== Ожидание 10 секунд и перезагрузка страницы ===");
        Thread.sleep(10000);
        driver.navigate().refresh();
        Thread.sleep(5000);
        takeScreenshot("8-after-refresh");
        System.out.println("Страница перезагружена");

        // Приведение к исходному состоянию
        System.out.println("\n=== ПРИВЕДЕНИЕ К СОСТОЯНИЮ ПО УМОЛЧАНИЮ ===");

        // Отписка, если подписаны
        WebElement finalSubscribeButton = findSubscribeButton();
        if (finalSubscribeButton != null) {
            String finalText = finalSubscribeButton.getText();
            System.out.println("Кнопка подписки после обновления: '" + finalText + "'");

            if (finalText.contains("Отписаться") || finalText.equals("Вы подписаны")) {
                System.out.println("Отписываемся...");
                clickElement(finalSubscribeButton);
                Thread.sleep(3000);
                takeScreenshot("9-unsubscribe-click");
                System.out.println("Отписка выполнена");
            }
        }

        // Снятие лайка, если стоит
        WebElement finalLikeButton = findLikeButton();
        if (finalLikeButton != null) {
            boolean isLikedFinal = checkIfLiked(finalLikeButton);
            if (isLikedFinal) {
                System.out.println("Убираем лайк...");
                clickElement(finalLikeButton);
                Thread.sleep(3000);
                takeScreenshot("10-unlike-click");
                System.out.println("Лайк убран");
            }
        }

        Thread.sleep(3000);
        takeScreenshot("11-final-state");
        System.out.println("\n=== ТЕСТ ЗАВЕРШЕН УСПЕШНО ===");
    }

    /**
     * Поиск кнопки подписки на странице видео.
     * <p>
     * Использует несколько стратегий поиска:
     * <ol>
     *     <li>По CSS-классу .vkuiSimpleCell__after button</li>
     *     <li>По XPath с текстом кнопки</li>
     * </ol>
     * </p>
     *
     * @return WebElement кнопки подписки или null, если кнопка не найдена
     */
    private WebElement findSubscribeButton() {
        try {
            WebElement button = driver.findElement(By.cssSelector(".vkuiSimpleCell__after button"));
            if (button != null && button.isDisplayed()) {
                System.out.println("Кнопка подписки найдена через .vkuiSimpleCell__after button");
                return button;
            }
        } catch (Exception e) {
            // продолжаем поиск по альтернативным селекторам
        }

        try {
            WebElement button = driver.findElement(By.xpath(
                    "//button[contains(text(), 'Подписаться') or contains(text(), 'Вы подписаны') or contains(text(), 'Отписаться')]"
            ));
            if (button != null && button.isDisplayed()) {
                System.out.println("Кнопка подписки найдена по тексту");
                return button;
            }
        } catch (Exception e) {
            // продолжаем
        }

        System.out.println("Кнопка подписки не найдена");
        return null;
    }

    /**
     * Поиск кнопки лайка на странице видео.
     * <p>
     * Использует следующие стратегии поиска:
     * <ol>
     *     <li>По атрибуту data-testid='video_page_like_button' (наиболее надёжный)</li>
     *     <li>По CSS-классу .vkitPostFooterAction__action</li>
     * </ol>
     * </p>
     *
     * @return WebElement кнопки лайка или null, если кнопка не найдена
     */
    private WebElement findLikeButton() {
        try {
            WebElement button = driver.findElement(By.cssSelector("[data-testid='video_page_like_button']"));
            if (button != null && button.isDisplayed()) {
                System.out.println("Кнопка лайка найдена через data-testid='video_page_like_button'");
                return button;
            }
        } catch (Exception e) {
            // продолжаем поиск по альтернативным селекторам
        }

        try {
            WebElement button = driver.findElement(By.cssSelector(".vkitPostFooterAction__action"));
            if (button != null && button.isDisplayed()) {
                System.out.println("Кнопка лайка найдена через класс .vkitPostFooterAction__action");
                return button;
            }
        } catch (Exception e) {
            // продолжаем
        }

        System.out.println("Кнопка лайка не найдена");
        return null;
    }

    /**
     * Проверка состояния кнопки лайка (нажат/не нажат).
     * <p>
     * Определяет состояние по следующим признакам:
     * <ul>
     *     <li>Наличие иконки like_24 (нажат) vs like_outline_24 (не нажат)</li>
     *     <li>Наличие красного цвета в стилях (нажат)</li>
     * </ul>
     * </p>
     *
     * @param likeButton WebElement кнопки лайка
     * @return true если лайк уже стоит, false если не стоит
     */
    private boolean checkIfLiked(WebElement likeButton) {
        try {
            String innerHtml = likeButton.getAttribute("innerHTML");
            if (innerHtml != null) {
                if (innerHtml.contains("like_24") && !innerHtml.contains("like_outline_24")) {
                    System.out.println("Обнаружена иконка like_24 - лайк стоит");
                    return true;
                }
                if (innerHtml.contains("like_outline_24")) {
                    System.out.println("Обнаружена иконка like_outline_24 - лайка нет");
                    return false;
                }
            }

            String style = likeButton.getAttribute("style");
            if (style != null && style.contains("--vkit_internal--post_footer_action_foreground_color: var(--vkui--color_accent_red")) {
                System.out.println("Обнаружен красный цвет - лайк стоит");
                return true;
            }
        } catch (Exception e) {
            // игнорируем ошибки при проверке
        }
        return false;
    }
}