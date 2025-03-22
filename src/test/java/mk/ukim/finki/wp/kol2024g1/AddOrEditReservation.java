package mk.ukim.finki.wp.kol2024g1;

import mk.ukim.finki.wp.exam.util.ExamAssert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class AddOrEditReservation extends AbstractPage {
    private WebElement guestName;
    private WebElement dateCreated;
    private WebElement daysOfStay;
    private WebElement roomType;
    private WebElement hotel;
    private WebElement submit;

    public AddOrEditReservation(WebDriver driver) {
        super(driver);
    }

    public static ItemsPage add(WebDriver driver, String addPath, String guestName, String dateCreated, String daysOfStay, String roomType, String hotel) {
        get(driver, addPath);
        assertRelativeUrl(driver, addPath);

        AddOrEditReservation addOrEditReservation = PageFactory.initElements(driver, AddOrEditReservation.class);
        addOrEditReservation.assertNoError();
        addOrEditReservation.guestName.sendKeys(guestName);
        addOrEditReservation.dateCreated.sendKeys(dateCreated);
        addOrEditReservation.daysOfStay.sendKeys(daysOfStay);

        Select selectType = new Select(addOrEditReservation.roomType);
        selectType.selectByValue(roomType);

        Select selectHotel = new Select(addOrEditReservation.hotel);
        selectHotel.selectByValue(hotel);

        addOrEditReservation.submit.click();
        return PageFactory.initElements(driver, ItemsPage.class);
    }

    public static AddOrEditReservation getEditPage(WebDriver driver, WebElement editButton) {
        String href = editButton.getAttribute("href");
        System.out.println(href);
        editButton.click();
        assertAbsoluteUrl(driver, href);

        return PageFactory.initElements(driver, AddOrEditReservation.class);
    }

    public static ItemsPage update(WebDriver driver, AddOrEditReservation addOrEditReservation, String guestName, String dateCreated, String daysOfStay, String roomType, String hotel) {
        addOrEditReservation.guestName.clear();
        addOrEditReservation.guestName.sendKeys(guestName);
        addOrEditReservation.dateCreated.clear();
        addOrEditReservation.dateCreated.sendKeys(dateCreated);
        addOrEditReservation.daysOfStay.clear();
        addOrEditReservation.daysOfStay.sendKeys(daysOfStay);

        Select selectType = new Select(addOrEditReservation.roomType);
        selectType.selectByValue(roomType);

        Select selectHotel = new Select(addOrEditReservation.hotel);
        selectHotel.selectByValue(hotel);

        addOrEditReservation.submit.click();
        return PageFactory.initElements(driver, ItemsPage.class);
    }

    public void assertEditFormIsPrefilled(String guestName, String dateCreated, String daysOfStay, String roomType, String hotel) {
        ExamAssert.assertEquals("Guest name is not prefilled", guestName, this.guestName.getAttribute("value"));
        ExamAssert.assertEquals("Date created is not prefilled", dateCreated, this.dateCreated.getAttribute("value"));
        ExamAssert.assertEquals("Days of stay is not prefilled", daysOfStay, this.daysOfStay.getAttribute("value"));
        boolean checkRoomType = ExamAssert.assertNotEquals("Room type is not preselected", 0, new Select(this.roomType).getAllSelectedOptions().size());
        if (checkRoomType) {
            ExamAssert.assertEquals("Room type is not preselected", roomType, new Select(this.roomType).getFirstSelectedOption().getAttribute("value"));
        }
        boolean checkHotel = ExamAssert.assertNotEquals("Hotel is not preselected", 0, new Select(this.hotel).getAllSelectedOptions().size());
        if (checkHotel) {
            ExamAssert.assertEquals("Hotel is not preselected", hotel, new Select(this.hotel).getFirstSelectedOption().getAttribute("value"));
        }
    }
}