import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MACChanger {
    private static final Logger logger = Logger.getLogger("MACChangerLog");
    private static String originalMAC = null;

    static {
        try {
            FileHandler fh = new FileHandler("mac_changer.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentMAC(String interfaceName) {
        try {
            Process process = Runtime.getRuntime().exec("ifconfig " + interfaceName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Pattern pattern = Pattern.compile("ether ([0-9a-f]{2}(:[0-9a-f]{2}){5})");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to get current MAC address for " + interfaceName + ": " + e.getMessage());
        }
        return null;
    }

    public static boolean isInterfaceUp(String interfaceName) {
        try {
            Process process = Runtime.getRuntime().exec("ifconfig " + interfaceName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("UP")) {
                    return true;
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to check if interface " + interfaceName + " is up: " + e.getMessage());
        }
        return false;
    }

    public static void changeMAC(String interfaceName, String newMAC, boolean verbose) {
        try {
            String currentMAC = getCurrentMAC(interfaceName);
            if (verbose) {
                System.out.println("[+] Changing MAC address for " + interfaceName + " from " + currentMAC + " to " + newMAC);
            }
            logger.info("Changing MAC address for " + interfaceName + " from " + currentMAC + " to " + newMAC);

            boolean isInterfaceUp = isInterfaceUp(interfaceName);
            if (!isInterfaceUp) {
                logger.severe("Interface " + interfaceName + " is not up. Cannot change MAC address.");
                return;
            }

            Process downProcess = Runtime.getRuntime().exec("ifconfig " + interfaceName + " down");
            downProcess.waitFor();
            Process changeProcess = Runtime.getRuntime().exec("ifconfig " + interfaceName + " hw ether " + newMAC);
            changeProcess.waitFor();
            Process upProcess = Runtime.getRuntime().exec("ifconfig " + interfaceName + " up");
            upProcess.waitFor();

            String changedMAC = getCurrentMAC(interfaceName);
            if (changedMAC != null && changedMAC.equals(newMAC)) {
                if (verbose) {
                    System.out.println("[+] MAC address successfully changed to " + newMAC);
                }
                logger.info("MAC address successfully changed to " + newMAC);
            } else {
                if (verbose) {
                    System.out.println("[-] Failed to change MAC address to " + newMAC + ". Current MAC is " + changedMAC);
                }
                logger.severe("Failed to change MAC address to " + newMAC + ". Current MAC is " + changedMAC);
            }
        } catch (IOException | InterruptedException e) {
            logger.severe("Error changing MAC address for " + interfaceName + ": " + e.getMessage());
            if (verbose) {
                System.out.println("[-] Error changing MAC address: " + e.getMessage());
            }
        }
    }

    public static String generateRandomMAC() {
        Random rand = new Random();
        byte[] mac = new byte[6];
        rand.nextBytes(mac);
        mac[0] = (byte) (mac[0] & (byte) 254); // set broadcast bit to 0
        StringBuilder sb = new StringBuilder(18);
        for (byte b : mac) {
            if (sb.length() > 0) {
                sb.append(":");
            }
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean validateMAC(String mac) {
        return mac.matches("[0-9a-f]{2}(:[0-9a-f]{2}){5}");
    }

    public static void macFlood(String interfaceName, int duration, double interval, boolean verbose) {
        long endTime = System.currentTimeMillis() + (duration * 1000);
        while (System.currentTimeMillis() < endTime) {
            String newMAC = generateRandomMAC();
            changeMAC(interfaceName, newMAC, verbose);
            try {
                Thread.sleep((long) (interval * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (!System.getProperty("user.name").equals("root")) {
                System.out.println("[-] This script must be run as root to change the MAC address.");
                System.exit(1);
            }

            if (args.length == 0) {
                System.out.println("[-] Please specify an interface using -i or --interface option.");
                System.exit(1);
            }

            String interfaceName = null;
            boolean manual = false, random = false, verbose = false, restore = false, flood = false;
            int duration = 60;
            double interval = 1.0;

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-i":
                    case "--interface":
                        interfaceName = args[++i];
                        break;
                    case "-m":
                    case "--manual":
                        manual = true;
                        break;
                    case "-r":
                    case "--random":
                        random = true;
                        break;
                    case "-v":
                    case "--verbose":
                        verbose = true;
                        break;
                    case "--restore":
                        restore = true;
                        break;
                    case "--flood":
                        flood = true;
                        break;
                    case "--duration":
                        duration = Integer.parseInt(args[++i]);
                        break;
                    case "--interval":
                        interval = Double.parseDouble(args[++i]);
                        break;
                    default:
                        System.out.println("[-] Unknown option: " + args[i]);
                        System.exit(1);
                }
            }

            originalMAC = getCurrentMAC(interfaceName);
            if (originalMAC == null) {
                System.out.println("[-] Could not get the current MAC address for " + interfaceName);
                System.exit(1);
            }

            if (verbose) {
                System.out.println("[+] Current MAC address for " + interfaceName + ": " + originalMAC);
            }

            if (restore) {
                changeMAC(interfaceName, originalMAC, verbose);
                System.out.println("[+] MAC address restored to the original.");
                return;
            }

            if (flood) {
                if (verbose) {
                    System.out.println("[+] Flooding the network with random MAC addresses for " + duration + " seconds.");
                }
                macFlood(interfaceName, duration, interval, verbose);
                return;
            }

            String newMAC = null;

            if (manual) {
                System.out.print("Enter the new MAC address (e.g., 00:11:22:33:44:55): ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                newMAC = reader.readLine().trim();
                if (!validateMAC(newMAC)) {
                    System.out.println("[-] Invalid MAC address format.");
                    System.exit(1);
                }
            } else if (random) {
                newMAC = generateRandomMAC();
                if (verbose) {
                    System.out.println("Generated MAC address: " + newMAC);
                }
                System.out.print("Do you want to use this MAC address? (y/n): ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String response = reader.readLine().trim();
                while (!response.equalsIgnoreCase("y")) {
                    newMAC = generateRandomMAC();
                    if (verbose) {
                        System.out.println("Generated MAC address: " + newMAC);
                    }
                    System.out.print("Do you want to use this MAC address? (y/n): ");
                    response = reader.readLine().trim();
                }
            } else {
                System.out.println("[-] Please specify a mode: manual (-m) or random (-r).");
                System.exit(1);
            }

            changeMAC(interfaceName, newMAC, verbose);
        } catch (IOException e) {
            logger.severe("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
