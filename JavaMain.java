import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Main {
    static Random rand = new Random();
    static double balance = 1000.00;
    static int savedPin = 1234;
    static double sendMoneyFee = 5.0;
    static double nonMkashFee = 10.0;
    static double cashOutFee = 10.0;

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: SQLite JDBC driver not found. Please add sqlite-jdbc.jar to your classpath.");
            System.exit(1);
        }

        SQLiteConnector.initializeDatabase();

        // Ensure the Scanner object is properly initialized and used consistently
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Fix input handling by clearing the input buffer after reading integers
        while (true) {
            System.out.println("\n=== Mkash ===");
            System.out.println("1. Send Money");
            System.out.println("2. Send Money to Non-Mkash User");
            System.out.println("3. Mobile Recharge");
            System.out.println("4. Payment");
            System.out.println("5. Cash Out");
            System.out.println("6. Pay Bill");
            System.out.println("7. Add Money");
            System.out.println("8. Download Mkash App");
            System.out.println("9. My Mkash");
            System.out.println("10. Reset PIN");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            String dateStr = dateFormat.format(new Date());

            switch (choice) {
                case 0 -> {
                    System.out.println("Thank you for using Mkash!");
                    System.exit(0);
                }
                case 1 -> sendMoney(scanner, dateStr);
                case 2 -> {
                    String phone = getValidMobileNumber(scanner, "Enter Receiver Phone Number: ");
                    System.out.print("Enter Amount: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                        continue;
                    }
                    System.out.print("Enter PIN: ");
                    int pin;
                    try {
                        pin = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN. Please enter a valid number.");
                        continue;
                    }
                    if (pin == savedPin && amount + nonMkashFee <= balance) {
                        balance -= (amount + nonMkashFee);
                        System.out.printf("Sent Tk %.2f to Non-Mkash %s. Fee Tk %.2f. Balance Tk %.2f%n", amount, phone, nonMkashFee, balance);
                        saveTransaction(dateStr, "Send Non-Mkash", amount, phone, "-", nonMkashFee, balance);
                    } else {
                        System.out.println(pin != savedPin ? "Incorrect PIN." : "Insufficient balance.");
                    }
                }
                case 3 -> {
                    String mobile = getValidMobileNumber(scanner, "Enter Mobile Number: ");
                    System.out.print("Enter Amount: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                        continue;
                    }
                    System.out.print("Enter PIN: ");
                    int pin;
                    try {
                        pin = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN. Please enter a valid number.");
                        continue;
                    }
                    if (pin == savedPin && amount <= balance) {
                        balance -= amount;
                        System.out.printf("Recharged Tk %.2f to %s. Balance Tk %.2f%n", amount, mobile, balance);
                        saveTransaction(dateStr, "Mobile Recharge", amount, mobile, "-", 0.0, balance);
                    } else {
                        System.out.println(pin != savedPin ? "Incorrect PIN." : "Insufficient balance.");
                    }
                }
                case 4 -> {
                    System.out.print("Enter Merchant ID: ");
                    String merchant = scanner.nextLine();
                    System.out.print("Enter Payment Amount: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                        continue;
                    }
                    System.out.print("Enter PIN: ");
                    int pin;
                    try {
                        pin = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN. Please enter a valid number.");
                        continue;
                    }
                    if (pin == savedPin && amount <= balance) {
                        balance -= amount;
                        System.out.printf("Paid Tk %.2f to %s. Balance Tk %.2f%n", amount, merchant, balance);
                        saveTransaction(dateStr, "Payment", amount, merchant, "-", 0.0, balance);
                    } else {
                        System.out.println(pin != savedPin ? "Incorrect PIN." : "Insufficient balance.");
                    }
                }
                case 5 -> {
                    String agent = getValidMobileNumber(scanner, "Enter Agent Number: ");
                    System.out.print("Enter Amount: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                        continue;
                    }
                    System.out.print("Enter PIN: ");
                    int pin;
                    try {
                        pin = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN. Please enter a valid number.");
                        continue;
                    }
                    if (pin == savedPin && amount + cashOutFee <= balance) {
                        balance -= (amount + cashOutFee);
                        System.out.printf("Cashed Out Tk %.2f to Agent %s. Fee Tk %.2f. Balance Tk %.2f%n", amount, agent, cashOutFee, balance);
                        saveTransaction(dateStr, "Cash Out", amount, agent, "-", cashOutFee, balance);
                    } else {
                        System.out.println(pin != savedPin ? "Incorrect PIN." : "Insufficient balance.");
                    }
                }
                case 6 -> {
                    System.out.print("Enter Bill Type (Electricity/Water/Gas): ");
                    String billType = scanner.nextLine();
                    System.out.print("Enter Account Number: ");
                    String accountNumber = scanner.nextLine();
                    System.out.print("Enter Amount: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                        continue;
                    }
                    System.out.print("Enter PIN: ");
                    int pin;
                    try {
                        pin = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN. Please enter a valid number.");
                        continue;
                    }
                    if (pin == savedPin && amount <= balance) {
                        balance -= amount;
                        System.out.printf("Paid Tk %.2f for %s bill (Account: %s). Balance Tk %.2f%n", 
                            amount, billType, accountNumber, balance);
                        saveTransaction(dateStr, "Bill Payment", amount, accountNumber, billType, 0.0, balance);
                    } else {
                        System.out.println(pin != savedPin ? "Incorrect PIN." : "Insufficient balance.");
                    }
                }
                case 7 -> {
                    System.out.print("Enter Amount to Add: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a valid number.");
                        continue;
                    }
                    if (amount > 0) {
                        balance += amount;
                        System.out.printf("Added Tk %.2f to your account. New Balance Tk %.2f%n", amount, balance);
                        saveTransaction(dateStr, "Add Money", amount, "Self", "-", 0.0, balance);
                    } else {
                        System.out.println("Amount must be greater than 0.");
                    }
                }
                case 8 -> {
                    System.out.println("\n=== Download Mkash App ===");
                    System.out.println("1. Android");
                    System.out.println("2. iOS");
                    System.out.print("Select your device type: ");
                    int deviceChoice;
                    try {
                        deviceChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid choice.");
                        continue;
                    }
                    switch (deviceChoice) {
                        case 1 -> System.out.println("Download link for Android: https://play.google.com/store/apps/details?id=com.bkash.customerapp");
                        case 2 -> System.out.println("Download link for iOS: https://apps.apple.com/bd/app/bkash/id1037754988");
                        default -> System.out.println("Invalid choice.");
                    }
                }
                case 9 -> {
                    System.out.println("\n=== My Mkash ===");
                    System.out.printf("Current Balance: Tk %.2f%n", balance);
                    System.out.println("\nRecent Transactions:");
                    
                    try (Connection conn = SQLiteConnector.getConnection();
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY id DESC LIMIT 10")) {
                        
                        boolean hasTransactions = false;
                        while (rs.next()) {
                            hasTransactions = true;
                            String date = rs.getString("date");
                            String type = rs.getString("type");
                            double amount = rs.getDouble("amount");
                            String receiver = rs.getString("receiver");
                            String reference = rs.getString("reference");
                            double fee = rs.getDouble("fee");
                            double balance = rs.getDouble("balance");
                            
                            System.out.printf("%s - %s: Tk %.2f to %s | Ref: %s | Fee: Tk %.2f | Balance: Tk %.2f%n",
                                    date, type, amount, receiver, reference, fee, balance);
                        }
                        
                        if (!hasTransactions) {
                            System.out.println("No transactions found.");
                        }
                    } catch (SQLException e) {
                        System.out.println("Error reading transactions: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case 10 -> {
                    System.out.println("\n=== Reset PIN ===");
                    System.out.print("Enter Current PIN: ");
                    int currentPin;
                    try {
                        currentPin = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN format. Please enter a 4-digit number.");
                        continue;
                    }

                    if (currentPin != savedPin) {
                        System.out.println("Incorrect current PIN. Please try again.");
                        continue;
                    }

                    System.out.print("Enter New PIN (4 digits): ");
                        int newPin;
                        try {
                            newPin = Integer.parseInt(scanner.nextLine());
                        if (newPin < 1000 || newPin > 9999) {
                            System.out.println("PIN must be a 4-digit number.");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN format. Please enter a 4-digit number.");
                        continue;
                    }

                        System.out.print("Confirm New PIN: ");
                    int confirmPin;
                        try {
                        confirmPin = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN format. Please enter a 4-digit number.");
                            continue;
                        }

                    if (newPin != confirmPin) {
                        System.out.println("PINs do not match. Please try again.");
                        continue;
                    }

                    savedPin = newPin;
                    System.out.println("PIN has been successfully reset.");
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void sendMoney(Scanner scanner, String dateStr) {
        System.out.print("Enter Receiver Account: ");
        String account = scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
            return;
        }
        System.out.print("Enter Reference: ");
        String ref = scanner.nextLine();
        System.out.print("Enter PIN: ");
        int pin;
        try {
            pin = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid PIN. Please enter a valid number.");
            return;
        }

        if (pin == savedPin && amount + sendMoneyFee <= balance) {
            balance -= (amount + sendMoneyFee);
            System.out.printf("Sent Tk %.2f to %s. Ref: %s. Fee Tk %.2f. Balance Tk %.2f%n", amount, account, ref, sendMoneyFee, balance);
            saveTransaction(dateStr, "Send Money", amount, account, ref, sendMoneyFee, balance);
        } else {
            System.out.println(pin != savedPin ? "Incorrect PIN." : "Insufficient balance.");
        }
    }

    private static void saveTransaction(String date, String type, double amount, String receiver, String ref, double fee, double balance) {
        String sql = "INSERT INTO transactions (date, type, amount, receiver, reference, fee, balance) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = SQLiteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, date);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setString(4, receiver);
            stmt.setString(5, ref);
            stmt.setDouble(6, fee);
            stmt.setDouble(7, balance);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isValidMobileNumber(String number) {
        // Remove any spaces or special characters
        number = number.replaceAll("[^0-9]", "");
        
        // Check if number is exactly 11 digits
        if (number.length() != 11) {
            System.out.println("Error: Mobile number must be exactly 11 digits.");
            return false;
        }
        
        // Check if number starts with 01
        if (!number.startsWith("01")) {
            System.out.println("Error: Mobile number must start with '01'.");
            return false;
        }
        
        // Check if 3rd digit is 3,6,7, or 9
        char thirdDigit = number.charAt(2);
        if (thirdDigit != '3' && thirdDigit != '6' && thirdDigit != '7' && thirdDigit != '9') {
            System.out.println("Error: Third digit must be 3, 6, 7, or 9.");
            return false;
        }
        
        // Check if all characters are digits
        if (!number.matches("[0-9]+")) {
            System.out.println("Error: Mobile number can only contain digits.");
            return false;
        }
        
        return true;
    }

    private static String getValidMobileNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String number = scanner.nextLine().trim();
            
            if (isValidMobileNumber(number)) {
                return number;
            }
            
            System.out.println("\nPlease enter a valid mobile number with the following format:");
            System.out.println("- Must start with '01'");
            System.out.println("- Third digit must be 3, 6, 7, or 9");
            System.out.println("- Must be exactly 11 digits");
            System.out.println("- Can only contain numbers\n");
        }
    }
}
