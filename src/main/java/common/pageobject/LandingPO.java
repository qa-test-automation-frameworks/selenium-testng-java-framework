package common.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.assertj.core.api.Assertions;

@Slf4j
public class LandingPO extends BasePO {

    private final By primaryHeader = By.cssSelector("[data-test='primary-header'] div+div[class='header_label'] div");

    public void assertHeaderLabel(String expectedLabelText) {
        String actualText = getDriver().findElement(primaryHeader).getText();
        Assertions.assertThat(actualText).isEqualTo(expectedLabelText);
        log.info("Asserted Header label text: {}", actualText);
    }

    public void assertCartButtonVisible() {
        Assertions.assertThat(isCartButtonVisible()).isTrue();
        log.info("Asserted Cart button visible");
    }

    public void assertListItemsSize(int expectedListItemsCount) {
        int actualListItemsCount = getListItemsCount();
        Assertions.assertThat(actualListItemsCount).isEqualTo(expectedListItemsCount);
        log.info("Asserted List items size: {}", actualListItemsCount);
    }

}
