import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.w3c.dom.NodeList;

import java.util.concurrent.TimeUnit;

public class TestExecutor {
    private XMLParser test = null;

    private WebDriver driver;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private TestLogger logger = new TestLogger();

    public TestExecutor(XMLParser test){
        this.test= test;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("–load-extension=" + "/Users/carloscunha/Downloads/chromeExt");
        options.addArguments("--auto-open-devtools-for-tabs");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        NodeList actions= test.getDocument().getElementsByTagName("selenese");

        for(int i= 0; i< actions.getLength(); i++){
            NodeList childNodes = actions.item(i).getChildNodes();
            executeCommand(
                    childNodes.item(1).getTextContent(),
                    childNodes.item(3).getTextContent(),
                    childNodes.item(5).getTextContent()
            );
        }


        System.out.println("Total time: " +logger.calculateTotalTime());

        Util.analyzeLog(driver);
    }


    private void executeCommand(String command, String target, String value){
        String[] keyValue = null;
        WebElement element = null;
        double distance;
        double size;

        switch(command){
            case "open":
                logger.addItem(new LogWebItem(KLMModel.instance().getPredictedTime("open",value,0.0d,0.0d)));
                driver.get(target);
                break;
            case "click":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("click",distance,size)));
                element.click();
                break;
            case "type":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(KLMModel.instance().getPredictedTime("type", value, distance, size)));
                element.sendKeys(value);
                break;
            case "submit":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("submit", distance, size)));
                element.click();

                break;
            case "close":
               /* element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("close", distance, size)));*/
                //close is not implemented yet because of selectWindow problems
                driver.close();

                break;
            case "mouseOver":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("mouseOver", distance, size)));

                Actions builder = new Actions(driver);
                builder.moveToElement(element).build().perform();

                break;
            case "select":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();
                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("select", distance, size)));

                new Select(element).selectByVisibleText(value.split("=",2)[1]);
                break;
        }
    }

    private double calculateDistanceFromLastPoint(WebElement element) {
        double lastX = logger.getLastWebItem().getCoordX()!=null?logger.getLastWebItem().getCoordX():0;
        double lastY = logger.getLastWebItem().getCoordY()!=null?logger.getLastWebItem().getCoordY():0;

        double distance = Math.sqrt(
                            Math.pow(element.getLocation().getX()-lastX,2)+
                            Math.pow(element.getLocation().getY()-lastY,2)
                        );

        return distance;
    }

    private WebElement findElement(String target) {
        WebElement element;
        String[] keyValue;
        keyValue = target.split("=",2);

        switch(keyValue[0]){
            case "id":
                element = driver.findElement(By.id(keyValue[1]));
                break;
            case "name":
                element = driver.findElement(By.name(keyValue[1]));
                break;
            case "xpath":
                element = driver.findElement(By.xpath(keyValue[1]));
                break;
            case "link":
                element = driver.findElement(By.linkText(keyValue[1]));
                break;
            default:
                throw new AssertionError("target should be id,name,xpath, or link");

        }

        return element;
    }

}
