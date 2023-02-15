package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {

    public static Logger log = LoggerFactory.getLogger(LoggerUtil.class);

    public static void info(String string) {
        if (null != string)
            log.info(string);
    }

    public static void info(Object object) {
        if (null != object)
            log.info(String.valueOf(object));
    }

    public static String getMethodName() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTraceElements[2];
        return "----- Method " + stackTraceElement.getMethodName() + " -----";
    }

}
