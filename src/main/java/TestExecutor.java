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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TestExecutor {
    private XMLParser test = null;

    static  private WebDriver driver;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private TestLogger logger = new TestLogger();
    private CsvWriter csvWriter = new CsvWriter();

    public TestExecutor(XMLParser test){
        this.test= test;
        ChromeOptions options = new ChromeOptions();
        options.addExtensions(new File("D:/Documentos/Mestrado/dissertação/chromeDriver.crx"));
        //options.addArguments("–load-extension=" + "/Users/ruipedroduarte/Downloads/chromeDriver.crx");
        options.addArguments("--auto-open-devtools-for-tabs");

        ChromeOptions caps = new ChromeOptions();
        //DesiredCapabilities caps = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        options.merge(caps);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        NodeList actions= test.getDocument().getElementsByTagName("selenese");


        try {
            System.out.println("Actions length: " +actions.getLength());
            for (int i = 0; i < actions.getLength(); i++) {

                NodeList childNodes = actions.item(i).getChildNodes();
                //System.out.println("Node 1: " +childNodes.item(1).getTextContent());
               // System.out.println("Node 3: " +childNodes.item(3).getTextContent());
               // System.out.println("Node 5: " +childNodes.item(5).getTextContent());
                executeCommand(
                        childNodes.item(1).getTextContent(),
                        childNodes.item(3).getTextContent(),
                        childNodes.item(5).getTextContent()
                );
            }
        }catch(Exception e){ e.printStackTrace(); }

        System.out.println("Total time: " +logger.calculateTotalTime());
        System.out.println("Complete KLM Input result: " +logger.getCompleteKLMInput());
        logger.calculateTotalOperators(logger.getCompleteKLMInput(), KLMModel.instance().getOperatorsTimes());


        if (logger.getOperatorsCount().size() > 0 ) {
            // save these values in a csv file
            csvWriter.SaveKLMString(logger.getCompleteKLMInput());
            csvWriter.SaveStatistics(logger.getOperatorsCount());

            // go through the array and return the operators with their percentage
            for(int i=0; i <logger.getOperatorsCount().size(); i++) {
                System.out.println("Operator: " + logger.getOperatorsCount().get(i).operator + "- Total: " + logger.getOperatorsCount().get(i).count
                        + "- Percentage: " + logger.getOperatorsCount().get(i).percentage + "%" + "- TimePerOperator: " + logger.getOperatorsCount().get(i).timePerOperator + "s");
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
                System.out.println("Opening page " + target);
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
            case "editContent": //new command found in a browse
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
        WebElement element = null;
        String[] keyValue;
        String[] KeyForSplit;

        if (target.contains("="))
        {
            keyValue = target.split("=",2);
            System.out.println(" Keyvalue 0: "+  keyValue[0]);
            System.out.println(" Keyvalue 1: " +keyValue[1]);
            if (keyValue[0].equals("id"))
            {
                System.out.println(" Id: " +keyValue[1]);
                KeyForSplit = keyValue[1].split("]",2);
                element = driver.findElement(By.id(KeyForSplit[0]));
            }
            else if (keyValue[0].equals("name"))
            {
                System.out.println(" Name: " +keyValue[1]);
                element = driver.findElement(By.name(keyValue[1]));
            }
            else if (keyValue[0].contains("button"))
            {
                System.out.println(" Button: " +keyValue[1]);
                element = driver.findElement(By.xpath(target));
            }
            else if (keyValue[0].equals("xpath"))
            {
                System.out.println("Xpath Name: " +keyValue[1]);
                element = driver.findElement(By.xpath(keyValue[1]));
            }
            else if (target.contains("div") || target.contains("form") || target.contains("span") || target.contains("href"))
            {
                System.out.println("Menu Name: " +target);
                element = driver.findElement(By.xpath(target));
            }
            else if (keyValue[0].equals("link"))
            {
                System.out.println("Link Name: " +keyValue[1]);
                element = driver.findElement(By.linkText(keyValue[1]));
            }
            else
                throw new AssertionError("target should be id,name,xpath, or link");

        }
        else //target does not contain "=", then extract from path
        {
            if (target.contains("div") || target.contains("form") || target.contains("span") || target.contains("href"))
            {
                System.out.println("Menu Name 2: " +target);
                element = driver.findElement(By.xpath(target));
            }
            else if (target.contains("button"))
            {
                System.out.println(" Button: " +target);
                element = driver.findElement(By.xpath(target));
            }
        }
        return element;


    }

}
