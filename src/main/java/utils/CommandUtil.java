package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class CommandUtil {

    public static Logger log = LoggerFactory.getLogger(CommandUtil.class);

    public static String executeCmd(String[] commandStr) {
        return executeCmd(commandStr, true);
    }

    public static String executeCmd(String[] commandStr, boolean show) {

        if (show) {
            log.info("ExecuteCmd : " + Arrays.asList(commandStr));
        }

        BufferedReader br = null;
        String res = "";

        try {
            Process p;
            p = Runtime.getRuntime().exec(commandStr);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
            res = output.toString().trim();
            if (show) {
                log.info("Command output : \n" + res);
            }
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            log.info("Fail to Execute Command : " + Arrays.toString(commandStr) + e.getMessage());
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

    public static String executeCmd(String commandStr) {
        return executeCmd(commandStr, true);
    }

    public static String executeCmd(String commandStr, boolean show) {
        String[] cmd = new String[3];
        cmd[0] = "cmd.exe";
        cmd[1] = "/C";
        cmd[2] = commandStr;

        return executeCmd(cmd, show);
    }

}
