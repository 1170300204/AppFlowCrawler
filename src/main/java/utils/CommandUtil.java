package utils;

import io.appium.java_client.android.nativekey.AndroidKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class CommandUtil {

    public static Logger log = LoggerFactory.getLogger(CommandUtil.class);

    public static String executeCmd(String[] commandStr) {
        return executeCmd(commandStr, true);
    }

    public static void executeCmdWithoutOutput(String command) {
        String[] cmd = new String[3];
        cmd[0] = "cmd.exe";
        cmd[1] = "/C";
        cmd[2] = command;

        try {
            log.info("ExecuteCmd : " + Arrays.asList(cmd));
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Fail to start Tcpdump");
        }
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

    public static void startTcpdump(String pcapFileName) {
        log.info("Starting Tcpdump ...");
        CommandUtil.executeCmd("adb -s " + ConfigUtil.getUdid() + " shell killall tcpdump ");
        DriverUtil.sleep(0.8);
        CommandUtil.executeCmdWithoutOutput("adb -s " + ConfigUtil.getUdid() + " shell tcpdump -i wlan0 -p -s 0 -w /sdcard/pcaps/" + pcapFileName + ".pcap &");
        DriverUtil.sleep(0.5);
    }

    public static void endTcpdump(String pcapFileName) {
        log.info("Ending Tcpdump ...");
        CommandUtil.executeCmd("mkdir \"" + ConfigUtil.getPcapDir() + "\"");
        DriverUtil.sleep(0.5);
        CommandUtil.executeCmd("adb -s " + ConfigUtil.getUdid() + " shell killall tcpdump ");
        DriverUtil.sleep(1);
        log.info("Moving pcap file to output Dir ...");
        CommandUtil.executeCmd("adb -s " + ConfigUtil.getUdid() + " pull /sdcard/pcaps/" + pcapFileName + ".pcap \"" + ConfigUtil.getPcapDir() + pcapFileName + ".pcap\"");
        DriverUtil.sleep(1);
    }


}
