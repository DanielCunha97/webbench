import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TestExecutor {
    private XMLParser test = null;

    private WebDriver driver;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private TestLogger logger = new TestLogger();
    private CsvWriter csvWriter = new CsvWriter();

    public TestExecutor(XMLParser test){
        this.test= test;
        ChromeOptions options = new ChromeOptions();
        options.addExtensions(new File("D:/Documentos/Mestrado/dissertação/chromeDriver.crx"));
       // options.addArguments("–load-extension=" + "/Users/carloscunha/Downloads/chromeExt.crx");
        options.addArguments("--auto-open-devtools-for-tabs");

        DesiredCapabilities caps = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        options.merge(caps);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        NodeList actions= test.getDocument().getElementsByTagName("selenese");


        try {

            for (int i = 0; i < actions.getLength(); i++) {
                NodeList childNodes = actions.item(i).getChildNodes();
                executeCommand(
                        childNodes.item(1).getTextContent(),
                        childNodes.item(3).getTextContent(),
                        childNodes.item(5).getTextContent()
                );
            }
        }catch(Exception e){ e.printStackTrace(); }

        System.out.println("Total time: " +logger.calculateTotalTime());
        System.out.println("Complete KLM Input result: " +logger.getCompleteKLMInput());
        logger.calculateTotalOperators(logger.getCompleteKLMInput());


        if (logger.getOperatorsCount().size() > 0 ) {
            // save this values in a csv file
            csvWriter.SaveStatistics(logger.getOperatorsCount());
            // go through the array and return the operators with their percentage
            for(int i=0; i <logger.getOperatorsCount().size(); i++) {
                System.out.println("Operator: " + logger.getOperatorsCount().get(i).operator + "- Total: " + logger.getOperatorsCount().get(i).count
                        + "- Percentage: " + logger.getOperatorsCount().get(i).percentage + "%");
            }
        }

        Util.analyzeLog(driver);
    }


    private void executeCommand(String command, String target, String value){
        String[] keyValue = null;
        WebElement element = null;
        double distance;
        double size;

        switch(command){
            case "open":
                logger.addItem(new LogWebItem(KLMModel.instance().getPredictedTime("open",value,0.0d,0.0d),
                        KLMModel.instance().getKLMInput("open",value)));
                driver.get(target);
                break;
            case "click":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("click",distance,size),
                        KLMModel.instance().getKLMInput("click",null)));
                element.click();
                break;
            case "type":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(KLMModel.instance().getPredictedTime("type", value, distance, size),
                        KLMModel.instance().getKLMInput("type",value)));
                element.sendKeys(value);
                break;
            case "submit":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();

                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("submit", distance, size),
                        KLMModel.instance().getKLMInput("submit",null)));
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

                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("mouseOver", distance, size),
                        KLMModel.instance().getKLMInput("mouseOver",null)));

                Actions builder = new Actions(driver);
                builder.moveToElement(element).build().perform();

                break;
            case "select":
                element = findElement(target);

                distance = calculateDistanceFromLastPoint(element);
                size = element.getSize().getWidth()*element.getSize().getHeight();
                logger.addItem(new LogWebItem(element.getLocation().getX(),element.getLocation().getY(), size, KLMModel.instance().getPredictedTime("select", distance, size),
                        KLMModel.instance().getKLMInput("select",null) ));

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
