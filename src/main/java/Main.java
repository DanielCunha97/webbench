import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        System.out.println(args[0]);
	    new TestExecutor(new XMLParser(args[0]));
    }
}
