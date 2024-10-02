package concertticketing;
import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

// ConcertTicket class
class ConcertTicket {
    private int ticketId;
    private String concertName;
    private int price;1
    
    private int availableTickets;
    private String customerName;

    public ConcertTicket(String customerName, int ticketId, String concertName, int price, int availableTickets) {
        this.customerName = customerName;
        this.ticketId = ticketId;
        this.concertName = concertName;
        this.price = price;
        this.availableTickets = availableTickets;
    }

    // Getters and setters
    public int getTicketId() {
        return ticketId;
    }

    public String getConcertName() {
        return concertName;
    }

    public int getPrice() {
        return price;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}

public class ConcertTicketing {
    static Scanner scanner = new Scanner(System.in);
    static Connection connection; // Connection to SQLite database

    public static void main(String[] args) {
        try {
            // Connect to SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:ConcertTicket.db");
            System.out.println("Connected to SQLite database.");

            // Create tables if they don't exist
            createTables();

            int choice;
            do {
                displayMenu();
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                switch (choice) {
                    case 1:
                        addTicket();
                        break;
                    case 2:
                        viewTickets();
                        break;
                    case 3:
                        updateTicket();
                        break;
                    case 4:
                        deleteTicket();
                        break;
                    case 5:
                        System.out.println("Exiting the system...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } while (choice != 5);

            // Close the connection
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static void displayMenu() {
        System.out.println("\nConcert Ticketing System");
        System.out.println("1. Add Ticket");
        System.out.println("2. View Tickets");
        System.out.println("3. Update Ticket");
        System.out.println("4. Delete Ticket");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    static void createTables() {
        try {
            Statement statement = connection.createStatement();
            // Create concerts table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS concerts (" +
                "concertId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "concertName TEXT UNIQUE)"
            );

            // Create tickets table with foreign key
            statement.execute(
                "CREATE TABLE IF NOT EXISTS tickets (" +
                "customerName TEXT NOT NULL, " +
                "ticketId INTEGER PRIMARY KEY, " +
                "concertId INTEGER NOT NULL, " +
                "price INTEGER NOT NULL, " +
                "availableTickets INTEGER, " +
                "FOREIGN KEY (concertId) REFERENCES concerts(concertId))"
            );

            System.out.println("Tables created or already exist.");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    static void addTicket() {
        try {
            System.out.print("Enter Customer Name: ");
            String customerName = scanner.nextLine();

            System.out.print("Enter Ticket ID: ");
            int ticketId = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            System.out.print("Enter Concert Name: ");
            String concertName = scanner.nextLine();

            System.out.print("Enter Price: ");
            int price = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            System.out.print("Enter Available Tickets: ");
            int availableTickets = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            // Input Validation
            if (price <= 0) {
                System.out.println("Invalid price. Price must be greater than 0.");
                return; // Exit the method if the price is invalid
            }
            if (availableTickets < 0) {
                System.out.println("Invalid available tickets. Available tickets cannot be negative.");
                return; // Exit the method if availableTickets is invalid
            }

            // Get concertId from the concerts table
            int concertId = getConcertId(concertName); // Add this method

            if (concertId == -1) {
                System.out.println("Concert not found. Adding new concert...");
                concertId = addConcert(concertName);
            }

            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO tickets (customerName, ticketId, concertId, price, availableTickets) VALUES (?, ?, ?, ?, ?)"
            );
            statement.setString(1, customerName);
            statement.setInt(2, ticketId);
            statement.setInt(3, concertId); // Set concertId
            statement.setInt(4, price);
            statement.setInt(5, availableTickets);
            statement.executeUpdate();
            System.out.println("Ticket added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding ticket: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.err.println("Invalid input. Please enter valid numbers for ticket ID, price, and available tickets.");
            scanner.nextLine(); // Clear the invalid input from the scanner
        }
    }

    // Method to get concertId from the concerts table
    static int getConcertId(String concertName) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT concertId FROM concerts WHERE concertName = ?"
            );
            statement.setString(1, concertName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("concertId");
            }
        } catch (SQLException e) {
            System.err.println("Error getting concert ID: " + e.getMessage());
        }
        return -1; // Return -1 if concert not found
    }

    // Method to add a new concert to the database
    // Method to add a new concert to the database
static int addConcert(String concertName) {
    try {
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO concerts (concertName) VALUES (?)"
        );
        statement.setString(1, concertName);
        statement.executeUpdate();
        // Get the newly generated concertId (assuming AUTOINCREMENT)
        statement = connection.prepareStatement(
            "SELECT last_insert_rowid()"
        );
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1); // Return the generated concertId
        }
    } catch (SQLException e) {
        System.err.println("Error adding concert: " + e.getMessage());
    }
    return -1; // Return -1 if adding the concert failed
}

    static void viewTickets() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tickets");

            if (resultSet.isBeforeFirst()) {
                System.out.println("\nAvailable Tickets:");
                System.out.println("---------------------------------------------------");
                System.out.println("Customer Name | Ticket ID | Concert Name | Price | Available Tickets");
                System.out.println("---------------------------------------------------");

                while (resultSet.next()) {
                    String customerName = resultSet.getString("customerName");
                    int ticketId = resultSet.getInt("ticketId");
                    int concertId = resultSet.getInt("concertId");
                    int price = resultSet.getInt("price");
                    int availableTickets = resultSet.getInt("availableTickets");
                    // Retrieve concertName from concerts table
                    String concertName = getConcertName(concertId);
                    System.out.println(customerName + " | " + ticketId + " | " + concertName + " | " + price + " | " + availableTickets);
                }

                System.out.println("---------------------------------------------------");
            } else {
                System.out.println("No tickets available.");
            }
        } catch (SQLException e) {
            System.err.println("Error viewing tickets: " + e.getMessage());
        }
    }

    // Method to get concertName from the concerts table
    static String getConcertName(int concertId) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT concertName FROM concerts WHERE concertId = ?"
            );
            statement.setInt(1, concertId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("concertName");
            }
        } catch (SQLException e) {
            System.err.println("Error getting concert name: " + e.getMessage());
        }
        return "Concert not found"; // Return "Concert not found" if concert not found
    }

    static void updateTicket() {
        try {
            System.out.print("Enter Ticket ID to update: ");
            int ticketIdToUpdate = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            System.out.print("Enter new available tickets: ");
            int newAvailableTickets = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            // Optionally ask for customer name update as well
            System.out.print("Enter new Customer Name (leave blank to keep current): ");
            String newCustomerName = scanner.nextLine();

            PreparedStatement statement = connection.prepareStatement(
                "UPDATE tickets SET availableTickets = ?, customerName = ? WHERE ticketId = ?"
            );

            statement.setInt(1, newAvailableTickets);
            if (!newCustomerName.isEmpty()) {
                statement.setString(2, newCustomerName);
            } else {
                statement.setString(2, null); // Set to null if no new name
            }
            statement.setInt(3, ticketIdToUpdate);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Ticket updated successfully!");
            } else {
                System.out.println("Ticket not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating ticket: " + e.getMessage());
        }
    }

    static void deleteTicket() {
        try {
            System.out.print("Enter Ticket ID to delete: ");
            int ticketIdToDelete = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM tickets WHERE ticketId = ?"
            );
            statement.setInt(1, ticketIdToDelete);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Ticket deleted successfully!");
            } else {
                System.out.println("Ticket not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting ticket: " + e.getMessage());
        }
    }
}