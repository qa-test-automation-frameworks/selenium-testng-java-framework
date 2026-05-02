package common.pageobject;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

public class CartPO extends BasePO {

    private final By productQuantityElement = By.cssSelector("[data-test='item-quantity']");

    private int getProductQuantityByIndex(int index) {
        return Integer.parseInt(getItemList().get(index).findElement(productQuantityElement).getText());
    }

    public void assertProductQuantityByIndex(int index, int expectedQuantity) {
        int actualQuantity = getProductQuantityByIndex(index);
        Assertions.assertThat(actualQuantity).isEqualTo(expectedQuantity);
    }
}
