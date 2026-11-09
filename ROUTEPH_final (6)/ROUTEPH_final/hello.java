import java.util.Scanner;

public class hello {

    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n======================================");
            System.out.println("       COMMUTER ROUTE GUIDE");
            System.out.println("======================================");
            System.out.println("1. Start Route Search");
            System.out.println("2. Notifications");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            int choice = input.nextInt();
            input.nextLine();

            if (choice == 3) {
                System.out.println("\nThank you for using Commuter Route Guide!");
                return;
            }

            if (choice == 2) {
                showNotifications();
                continue;
            }

            if (choice == 1) {
                startRouteSearch();
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    // START ROUTE SEARCH
    static void startRouteSearch() {

        System.out.println("\n======================================");
        System.out.println("           YOUR LOCATION");
        System.out.println("======================================");

        String location =
                "Polytechnic University of the Philippines " +
                "1016 Anonas Street, Sta. Mesa, Manila, Philippines 1016";

        System.out.println(location);

        System.out.print("\nEnter your location: ");
        String userLocation = input.nextLine();

        System.out.println("\n======================================");
        System.out.println("         YOUR DESTINATION");
        System.out.println("======================================");
        System.out.println("Available destinations:");
        System.out.println("- Caloocan");
        System.out.println("- Bulacan");

        while (true) {

            System.out.print("\nEnter your destination: ");
            String destination = input.nextLine().trim();

            if (destination.equalsIgnoreCase("Caloocan")) {

                chooseRoute("Caloocan");
                return;

            } else if (destination.equalsIgnoreCase("Bulacan")) {

                chooseRoute("Bulacan");
                return;

            } else {

                System.out.println("\nDestination not available.");
                System.out.println("Please enter only Caloocan or Bulacan.");
            }
        }
    }

    // ROUTE SELECTION
    static void chooseRoute(String destination) {

        // Caloocan has 2 routes, Bulacan has 3 routes
        int maxRoutes = destination.equalsIgnoreCase("Caloocan") ? 2 : 3;

        while (true) {

            System.out.println("\n======================================");
            System.out.println("          " + destination.toUpperCase());
            System.out.println("======================================");

            System.out.println("1. First Route");
            System.out.println("2. Second Route");
            if (maxRoutes == 3) {
                System.out.println("3. Third Route");
            }
            System.out.println("4. Change Destination");
            System.out.println("5. Exit");
            System.out.println("6. Notifications");

            System.out.print("\nChoose: ");
            int routeChoice = input.nextInt();
            input.nextLine();

            if (routeChoice == 5) {
                System.out.println("\nProcess cancelled.");
                return;
            }

            if (routeChoice == 4) {
                System.out.println("\nReturning to destination selection...");
                startRouteSearch();
                return;
            }

            if (routeChoice == 6) {
                showNotifications();
                continue;
            }

            if (routeChoice < 1 || routeChoice > maxRoutes) {
                System.out.println("Invalid route.");
                continue;
            }

            showRouteDetails(destination, routeChoice);
        }
    }

    // ROUTE DETAILS
    static void showRouteDetails(String destination, int routeChoice) {

        System.out.println("\n======================================");
        System.out.println("             ROUTE DETAILS");
        System.out.println("======================================");

        System.out.println("From:");
        System.out.println("PUP Sta. Mesa");

        System.out.println("\nTo:");
        System.out.println(destination);

        System.out.println("\nSelected Route: Route " + routeChoice);

        if (destination.equalsIgnoreCase("Caloocan")) {

            // ===== CALOOCAN (2 ROUTES) =====
            if (routeChoice == 1) {

                System.out.println("\n1st Route South Caloocan");
                System.out.println("PUP Sta. Mesa -> LRT-2 -> LRT-1 -> Tricycle -> South Caloocan");

                System.out.println("\nTransportation:");
                System.out.println("1. Walk/ride to LRT-2 Pureza station.   (TRAIN)");
                System.out.println("2. Ride LRT-2 Pureza Train toward LRT-2 Recto Station.   (Train)");
                System.out.println("3. Transfer to LRT-1 Doroteo Jose Station.   (Train)");
                System.out.println("4. Ride LRT-1 Doroteo Jose Train toward LRT-1 5th Avenue Station  (Train)");
                System.out.println("5. Get off at LRT-1 5th Avenue Station and take a ride tricycle C3 Caloocan. (Walk)");
                System.out.println("6. Tricycle to Landaska  (E-Bike)");
                System.out.println("7. Landaska to your Destination");

            } else {

                System.out.println("\n2nd Route South Caloocan");
                System.out.println("PUP Sta. Mesa -> LRT-2 -> Jeep -> Tricycle -> E-BIKE -> South Caloocan");

                System.out.println("\nTransportation:");
                System.out.println("1. Walk/ride to LRT-2 Pureza station.   (TRAIN)");
                System.out.println("2. Ride LRT-2 Pureza Train toward LRT-2 Recto Station.   (Train)");
                System.out.println("3. Exit LRT-2 Recto Station and walk toward to take a ride Jeep   (Walk)");
                System.out.println("4. Jeep to C3 Caloocan  (Jeep)");
                System.out.println("5. C3 Caloocan to Landaska (Tricycle)");
                System.out.println("6. Landask to your Destination  (E-Bike)");

            }

        } else {

            // ===== BULACAN (3 ROUTES) =====
            if (routeChoice == 1) {

                System.out.println("\n1st Route Bulacan");
                System.out.println("PUP Sta. Mesa -> LRT-2 -> LRT-1 -> Monumento -> Bulacan");

                System.out.println("\nTransportation:");
                System.out.println("1. Walk/ride to LRT-2 Pureza station.   (TRAIN)");
                System.out.println("2. Ride LRT-2 Pureza Train toward LRT-2 Recto Station.   (Train)");
                System.out.println("3. Transfer to LRT-1 Doroteo Jose Station.   (Walk)");
                System.out.println("4. Ride LRT-1 Doroteo Jose Train toward LRT-1 Monumento Station (Train)");
                System.out.println("5. Get off at LRT-1 Monumento Station and head to the back of Victory Mall. (Walk)");
                System.out.println("6. Take a ride in Meycauayan Jeep Terminal  (Jeep)");
                System.out.println("7. After the jeepney reaches the last stop, walk toward your destination.  (Walk)");

            } else if (routeChoice == 2) {

                System.out.println("\n2nd Route Bulacan");
                System.out.println("PUP Sta. Mesa -> LRT-2  -> BUS SANTRANS TERMINAL -> Bulacan");

                System.out.println("\nTransportation:");
                System.out.println("1. Walk/ride to LRT-2 Pureza station.   (TRAIN)");
                System.out.println("2. Ride LRT-2 Pureza Train toward LRT-2 Recto Station.   (Train)");
                System.out.println("3. Exit LRT-2 Recto Station and walk toward to BUS Santrans Terminal   (Walk)");
                System.out.println("4. Take a ride Bus to duhat (Marilao Exit)  (Bus)");
                System.out.println("5. Take a ride Tricycle duhat to your Destination  (Tricycle)");

            } else {

                System.out.println("\n3rd Route Bulacan");
                System.out.println("PUP Sta. Mesa -> Cubao -> Caloocan -> Bulacan");

                System.out.println("\nTransportation:");
                System.out.println("1. Walk/ride to LRT-2 Pureza station.  (Train)");
                System.out.println("2. Ride LRT-2 Pureza Train toward LRT-2 Cubao Station.   (Train)");
                System.out.println("3. Exit LRT-2 Cubao Station and take a ride BUS to SM FAIRVIEW  (BUS)");
                System.out.println("4. Exit in SM FAIRVIEW then take a ride Jeep to Kaliwa Bagong Silang  (Jeep)");
                System.out.println("5. Exit in Bagong Silang last stop and take a ride tricycle to your Destination  (Tricycle)");

            }
        }

        // COMMENT SECTION
        System.out.println("\n======================================");
        System.out.println("               COMMENT");
        System.out.println("======================================");

        System.out.print("Enter your comment: ");
        String comment = input.nextLine();

        if (!comment.isEmpty()) {
            System.out.println("Comment posted successfully!");
        } else {
            System.out.println("No comment entered.");
        }

        // OPTIONS
        while (true) {

            System.out.println("\n======================================");
            System.out.println("              OPTIONS");
            System.out.println("======================================");
            System.out.println("1. Print Route Details");
            System.out.println("2. Choose Another Route");
            System.out.println("3. Change Destination");
            System.out.println("4. Notifications");
            System.out.println("5. Exit");

            System.out.print("Choose: ");
            int option = input.nextInt();
            input.nextLine();

            if (option == 1) {

                printRoute(destination, routeChoice, comment);

            } else if (option == 2) {

                return;

            } else if (option == 3) {

                startRouteSearch();
                return;

            } else if (option == 4) {

                showNotifications();

            } else if (option == 5) {

                System.out.println("\nProcess cancelled.");
                System.exit(0);

            } else {

                System.out.println("Invalid option.");
            }
        }
    }

    // PRINT ROUTE
    static void printRoute(String destination, int routeChoice, String comment) {

        System.out.println("\n======================================");
        System.out.println("            PRINTED ROUTE");
        System.out.println("======================================");

        System.out.println("YOUR LOCATION:");
        System.out.println("Polytechnic University of the Philippines");
        System.out.println("1016 Anonas Street, Sta. Mesa, Manila, Philippines 1016");

        System.out.println("\nYOUR DESTINATION:");
        System.out.println(destination);

        System.out.println("\nSELECTED ROUTE:");
        System.out.println("Route " + routeChoice);

        System.out.println("\nCOMMENT:");
        System.out.println(comment);

        System.out.println("\n======================================");
        System.out.println("Route details ready for printing.");
        System.out.println("======================================");
    }

    // NOTIFICATIONS
    static void showNotifications() {

        System.out.println("\n======================================");
        System.out.println("           NOTIFICATIONS");
        System.out.println("======================================");

        System.out.println("No new notifications.");
        System.out.println("When someone replies to your route");
        System.out.println("comment, the reply will appear here.");
    }
}
