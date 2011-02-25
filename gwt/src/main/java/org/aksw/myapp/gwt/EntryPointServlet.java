package org.aksw.myapp.gwt;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.aksw.myapp.gwt.client.WorkerService;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Enumeration;


public class EntryPointServlet extends RemoteServiceServlet implements WorkerService {
    private static final Logger logger = Logger.getLogger(EntryPointServlet.class);

    @Override
    public String test() {
        return "test string";
    }

    @Override
    public String testLog() {
       
        String message = "I'm a log message";
        logger.error(message);
        String appenders = "";
        Enumeration<Appender> apps = Logger.getRootLogger().getAllAppenders();
        while (apps.hasMoreElements()) {
            Appender a = apps.nextElement();
            appenders += "\n" + a.toString();
        }
        appenders+="\n"+Logger.getRootLogger().toString();
        return "The following message was written to the log file::\n" + message + "\nusing the following appenders\n" + appenders;
    }

    @Override
    public String testFile() {


        String ret = "";
        ApplicationContext c = new ClassPathXmlApplicationContext("spring.xml");



        FileTest ft = (FileTest) c.getBean("fileTest");
        FileTest ftuserhome = (FileTest) c.getBean("fileTest2");
        FileTest ftcat = (FileTest) c.getBean("fileTestcatalina");

        ret+="\n"+ft.toString();
        ret+="\n"+ftuserhome.toString();
        ret+="\n"+ftcat.toString();


        return ret;
    }


}
