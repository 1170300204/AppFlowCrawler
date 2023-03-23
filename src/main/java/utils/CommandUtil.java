package utils;

import io.appium.java_client.android.nativekey.AndroidKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
            int exitValue = p.waitFor();
            if (0 != exitValue) {
                log.error("call shell failed. error code is :" + exitValue);
            }
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "GBK"));
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

    public static void executeCmdNormal(String cmd) {
        BufferedReader br = null;
        String res = "";
        try {
            Process p;
            p = Runtime.getRuntime().exec(cmd);
            int exitValue = p.waitFor();
            if (0 != exitValue) {
                log.error("call shell failed. error code is :" + exitValue);
            }
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
            res = output.toString().trim();
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to execute cmd : " + cmd);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        log.info(res);
    }

    public static void executeWithPath(String cmd, String path) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(new File(path));
            Process p = pb.start();
            final InputStream is1 = p.getInputStream();
            //获取进城的错误流
            final InputStream is2 = p.getErrorStream();
            //启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流
            new Thread(() -> {
                BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));
                try {
                    String line1 = null;
                    while ((line1 = br1.readLine()) != null) {
                        if (line1 != null){}
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    try {
                        is1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(() -> {
                BufferedReader br2 = new  BufferedReader(new  InputStreamReader(is2));
                try {
                    String line2 = null ;
                    while ((line2 = br2.readLine()) !=  null ) {
                        if (line2 != null){}
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    try {
                        is2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to execute cmd : " + cmd);
        }
    }

    public static void startTcpdump(String pcapFileName) throws IOException {
        log.info("Starting Tcpdump ...");
//        CommandUtil.executeCmd("mkdir \"" + ConfigUtil.getPcapDir() + "\"");
        File pcapDir = new File(ConfigUtil.getPcapDir());
        if (!pcapDir.exists()) {
            pcapDir.mkdirs();
        }
        DriverUtil.sleep(0.5);
        CommandUtil.executeCmd("adb -s " + ConfigUtil.getUdid() + " shell killall tcpdump ");
        DriverUtil.sleep(0.8);
        CommandUtil.executeCmdWithoutOutput("adb -s " + ConfigUtil.getUdid() + " shell tcpdump -i wlan0 -p -s 0 -w /sdcard/pcaps/" + pcapFileName + ".pcap &");
        DriverUtil.sleep(0.5);
    }

    public static void endTcpdump(String pcapFileName) {
        log.info("Ending Tcpdump ...");
        CommandUtil.executeCmd("adb -s " + ConfigUtil.getUdid() + " shell killall tcpdump ");
        DriverUtil.sleep(1);
        log.info("Moving pcap file to output Dir ...");
        CommandUtil.executeCmd("adb -s " + ConfigUtil.getUdid() + " pull /sdcard/pcaps/" + pcapFileName + ".pcap \"" + ConfigUtil.getPcapDir() + pcapFileName + ".pcap\"");
        DriverUtil.sleep(1);
    }


}
