package com.example.demo.service;

import com.example.demo.config.SeleniumConfig;
import com.example.demo.model.ScrapModel;
import com.example.demo.repository.ScrapRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class ScrapService {

    @Autowired
    private ScrapRepository scrapRepository;
    private WebDriver driver;

    public List<ScrapModel> getAll() {

        List data = new ArrayList();

        try {
            data = scrapRepository.findAll();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public String deleteNews(String id){
        try {
            Optional<ScrapModel>object=scrapRepository.findById(id);
            if (object.isPresent()){
                scrapRepository.delete(object.get());
                return "Dokument o id " + id + " został zwrócony";
            }else {
                return "Dokument o id " + id + " nie istnieje";
            }

        }catch (Exception e){
            return "Błąd " + e.getMessage();
        }
    }

    private List<WebElement> getFromOnet() {
        driver = new SeleniumConfig().webDriver();
        driver.get("https://www.onet.pl");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement acceptButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".cmp-button_button.cmp-intro_acceptAll"))
        );
        acceptButton.click();

        wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a h3"))
        );

        List<WebElement> elements = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("a:has(h3)"))
        );

        return elements;
    }

    public List<String> scrapByTitle(String query) {
        List<String> results = new ArrayList<>();

        try {
            List<WebElement> elements = this.getFromOnet();
            for (WebElement element : elements) {
                WebElement h3Element = element.findElement(By.tagName("h3"));
                String image = "";
                String titleText = h3Element.getText();
                String link = element.getAttribute("href");

                try {
                    WebElement imageElement = element.findElement(By.tagName("img"));
                    image = imageElement.getAttribute("src");
                } catch (Exception ignored) {}


                if (titleText.toLowerCase().contains(query.toLowerCase())) {
                    results.add(titleText);

                    ScrapModel model = new ScrapModel();
                    model.setTitle(titleText);
                    model.setSource(driver.getCurrentUrl());
                    model.setLink(link);
                    model.setDate(LocalDateTime.now());
                    model.setImage(image);

                    boolean exists = scrapRepository.existsByTitle(titleText);
                    if (!exists) {
                        scrapRepository.save(model);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            driver.quit();
            System.out.println("zamykam");
        }

        return results;
    }

    public List<String> scrapAll() {
        List<String> allTitles = new ArrayList<>();
        try {
            List<WebElement> elements = this.getFromOnet();

            for (WebElement element : elements) {
                WebElement h3Element = element.findElement(By.tagName("h3"));
                String titleText = h3Element.getText();
                String link = element.getAttribute("href");

                allTitles.add(titleText + " - " + link);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            driver.quit();
            System.out.println("zamykam");
        }

        return allTitles;
    }

}