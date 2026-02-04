// Layout Maintenance Application
// ---------------------------------------
// Types -  Villa, Apartment, Independent House, Open Site
// Total Sites - 35 Sites
// First 10 sites are of 40x60 ft size
// Next 10 sites are of 30x50 ft size
// Last 15 sites are of 30x40 ft size
// Open sites are charged 6Rs/sqft
// Occupied sites are charged 9Rs./sqft
 
// Admin	- 
// 	Can add/edit/remove the owner details and site details
// 	Can collect the maintenance and update
// 	Can see the pending details of all sites or the specific site
// 	Can approve/reject the site owners update about their own sites

// Site Owner -
// 	Can only see/update the details of his/her own site (but should be approved by Admin)

import java.sql.*;
import java.time.LocalDate;
import java.io.BufferedReader;
import java.io.InputStreamReader;

final class DB
{
    private static DB instance = null;
    private Connection con = null;

    private DB()
    {
        try
        {
            con = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/layout_maintainance",
                    "postgres",
                    "root1234"
            );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Connection getConnection()
    {
        if (instance == null)
        {
            instance = new DB();
        }
        return instance.con;
    }
}

final class InputService
{
    public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private InputService() {}

    public static String readLine()
    {
        try
        {
            return reader.readLine();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}

class UserUtil
{
    public static ResultSet fetchOwnerByName(Connection con, String fullName) throws SQLException
    {
        String sql = "SELECT user_id, full_name, role FROM users WHERE full_name = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, fullName);
        return ps.executeQuery();
    }

    public static void updateUserField(Connection con, int userId, String field, String newValue) throws SQLException
    {
        String sql = "UPDATE users SET " + field + " = ? WHERE user_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, newValue);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

}

class Auth
{
    public static CurrentUser login()
    {
        try
        {
            System.out.print("Enter full name: ");
            String fullName = InputService.readLine();

            System.out.print("Enter password: ");
            String password = InputService.readLine();

            Connection con = DB.getConnection();

            String sql = "SELECT user_id, role, full_name FROM users WHERE full_name = ? AND password_hash = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, fullName);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                String name = rs.getString("full_name");

                System.out.println("Login successful!");

                if ("ADMIN".equals(role))
                {
                    return new Admin(userId, name);
                }
                else
                {
                    String siteSql = "SELECT site_id, site_number FROM sites WHERE owner_id = ?";
                    PreparedStatement ps2 = con.prepareStatement(siteSql);
                    ps2.setInt(1, userId);

                    ResultSet rsSites = ps2.executeQuery();

                    System.out.println("Select a site to login:");

                    boolean hasSite = false;
                    while (rsSites.next())
                    {
                        hasSite = true;
                        int siteId = rsSites.getInt("site_id");
                        int siteNumber = rsSites.getInt("site_number");
                        System.out.println("Site ID: " + siteId + " | Site Number: " + siteNumber);
                    }

                    if (!hasSite)
                    {
                        System.out.println("No site assigned to this owner!");
                        return null;
                    }

                    System.out.print("Enter Site ID to continue: ");
                    int chosenSiteId = Integer.parseInt(InputService.readLine());

                    String checkSql = "SELECT 1 FROM sites WHERE site_id = ? AND owner_id = ?";
                    PreparedStatement psCheck = con.prepareStatement(checkSql);
                    psCheck.setInt(1, chosenSiteId);
                    psCheck.setInt(2, userId);

                    ResultSet rsCheck = psCheck.executeQuery();
                    if (!rsCheck.next())
                    {
                        System.out.println("Invalid site selection!");
                        return null;
                    }

                    Owner owner = new Owner(userId, name);
                    owner.setSelectedSiteId(chosenSiteId);
                    return owner;
                }
            }
            else
            {
                System.out.println("Invalid full name or password!");
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}

abstract class CurrentUser
{
    protected final int userId;
    protected final String username;

    protected CurrentUser(int userId, String username)
    {
        this.userId = userId;
        this.username = username;
    }

    public abstract void showMenu();
}


class Admin extends CurrentUser
{
    public Admin(int userId, String username)
    {
        super(userId, username);
    }

    public void updateSiteDetails()
    {
        try
        {
            Connection con = DB.getConnection();

            System.out.print("Enter Site ID to update: ");
            int siteId = Integer.parseInt(InputService.readLine());

            String fetchSql =
                "SELECT s.site_id, s.site_number, s.site_status, st.type_name " +
                "FROM sites s " +
                "JOIN site_types st ON s.site_type_id = st.site_type_id " +
                "WHERE s.site_id = ?";

            PreparedStatement psFetch = con.prepareStatement(fetchSql);
            psFetch.setInt(1, siteId);

            ResultSet rs = psFetch.executeQuery();

            if (!rs.next())
            {
                System.out.println("Site not found!");
                return;
            }

            System.out.println("----- Current Site Details -----");
            System.out.println("Site ID     : " + rs.getInt("site_id"));
            System.out.println("Site Number : " + rs.getInt("site_number"));
            System.out.println("Type        : " + rs.getString("type_name"));
            System.out.println("Status      : " + rs.getString("site_status"));
            System.out.println("--------------------------------");

            System.out.println("What do you want to update?");
            System.out.println("1. Site Type");
            System.out.println("2. Site Status");
            System.out.print("Enter choice: ");

            int choice = Integer.parseInt(InputService.readLine());

            if (choice == 1)
            {
                String typeSql = "SELECT site_type_id, type_name FROM site_types";
                PreparedStatement psType = con.prepareStatement(typeSql);
                ResultSet rsType = psType.executeQuery();

                System.out.println("Available Site Types:");
                while (rsType.next())
                {
                    System.out.println(
                        rsType.getInt("site_type_id") + " - " + rsType.getString("type_name")
                    );
                }

                System.out.print("Enter new Site Type ID: ");
                int newTypeId = Integer.parseInt(InputService.readLine());

                String updateSql =
                    "UPDATE sites SET site_type_id = ? WHERE site_id = ?";

                PreparedStatement psUpdate = con.prepareStatement(updateSql);
                psUpdate.setInt(1, newTypeId);
                psUpdate.setInt(2, siteId);

                int rows = psUpdate.executeUpdate();

                if (rows > 0)
                    System.out.println("Site type updated successfully!");
                else
                    System.out.println("Update failed!");
            }
            else if (choice == 2)
            {
                System.out.println("Select new status:");
                System.out.println("1. OPEN");
                System.out.println("2. OCCUPIED");
                System.out.print("Enter choice: ");

                int st = Integer.parseInt(InputService.readLine());

                String newStatus;
                if (st == 1)
                    newStatus = "OPEN";
                else if (st == 2)
                    newStatus = "OCCUPIED";
                else
                {
                    System.out.println("Invalid status choice!");
                    return;
                }

                String updateSql =
                    "UPDATE sites SET site_status = ? WHERE site_id = ?";

                PreparedStatement psUpdate = con.prepareStatement(updateSql);
                psUpdate.setString(1, newStatus);
                psUpdate.setInt(2, siteId);

                int rows = psUpdate.executeUpdate();

                if (rows > 0)
                    System.out.println("Site status updated successfully!");
                else
                    System.out.println("Update failed!");
            }
            else
            {
                System.out.println("Invalid choice!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void displayOwnerDetails()
    {
        System.out.println("Enter Owner Full name:");
        String fname = InputService.readLine();

        try
        {
            Connection con = DB.getConnection();

            String sql =
                "SELECT u.full_name, s.site_id, s.site_number, s.site_status, " +
                "       st.type_name, sz.length_ft, sz.width_ft " +
                "FROM sites s " +
                "JOIN users u ON s.owner_id = u.user_id " +
                "JOIN site_types st ON s.site_type_id = st.site_type_id " +
                "JOIN site_sizes sz ON s.size_id = sz.size_id " +
                "WHERE u.full_name = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, fname);

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            System.out.println("----- Owner Site Details -----");

            while (rs.next())
            {
                found = true;

                String ownerName = rs.getString("full_name");
                int siteId = rs.getInt("site_id");
                int siteNumber = rs.getInt("site_number");
                String status = rs.getString("site_status");
                String type = rs.getString("type_name");
                int length = rs.getInt("length_ft");
                int width = rs.getInt("width_ft");

                System.out.println("Owner       : " + ownerName);
                System.out.println("Site ID     : " + siteId);
                System.out.println("Site Number : " + siteNumber);
                System.out.println("Type        : " + type);
                System.out.println("Size        : " + length + " x " + width + " ft");
                System.out.println("Status      : " + status);
                System.out.println("-----------------------------");
            }

            if (!found)
            {
                System.out.println("No site found for owner: " + fname);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void editOwnerDetails()
    {
        System.out.print("Enter Owner Full Name to edit: ");
        String fname = InputService.readLine();

        try
        {
            Connection con = DB.getConnection();

            ResultSet rs = UserUtil.fetchOwnerByName(con, fname);

            if (!rs.next())
            {
                System.out.println("Owner not found!");
                return;
            }

            int userId = rs.getInt("user_id");
            String currentName = rs.getString("full_name");
            String currentRole = rs.getString("role");

            System.out.println("Current Details:");
            System.out.println("1. Full Name : " + currentName);
            System.out.println("2. Role      : " + currentRole);

            System.out.println("\nWhat do you want to edit?");
            System.out.println("1. Full Name");
            System.out.println("2. Role");
            System.out.print("Enter choice: ");

            int choice = Integer.parseInt(InputService.readLine());

            switch (choice)
            {
                case 1:
                    System.out.print("Enter new full name: ");
                    String newName = InputService.readLine();

                    UserUtil.updateUserField(con, userId, "full_name", newName);
                    System.out.println("Full name updated successfully!");
                    break;

                case 2:
                    System.out.print("Enter new role (ADMIN / OWNER): ");
                    String newRole = InputService.readLine().toUpperCase();

                    if (!newRole.equals("ADMIN") && !newRole.equals("OWNER"))
                    {
                        System.out.println("Invalid role!");
                        return;
                    }

                    UserUtil.updateUserField(con, userId, "role", newRole);
                    System.out.println("Role updated successfully!");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void seePendingdetails()
    {
        try
        {
            Connection con = DB.getConnection();

            String sql =
                "SELECT u.full_name, s.site_id, s.site_number, d.bill_year, d.total_amount, d.status " +
                "FROM maintenance_dues d " +
                "JOIN sites s ON d.site_id = s.site_id " +
                "JOIN users u ON s.owner_id = u.user_id " +
                "WHERE d.status = 'UNPAID' OR d.status = 'PARTIALLY_PAID' ";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            boolean found = false;

            System.out.println("----- Pending / Half-Paid Maintenance Details -----");

            while (rs.next())
            {
                found = true;

                String ownerName = rs.getString("full_name");
                int siteId = rs.getInt("site_id");
                int siteNumber = rs.getInt("site_number");
                int year = rs.getInt("bill_year");
                double totalAmount = rs.getDouble("total_amount");
                String status = rs.getString("status");

                System.out.println("Owner       : " + ownerName);
                System.out.println("Site ID     : " + siteId);
                System.out.println("Site Number : " + siteNumber);
                System.out.println("Year        : " + year);
                System.out.println("Total Amount: " + totalAmount);
                System.out.println("Status      : " + status);
                System.out.println("-----------------------------------------------");
            }

            if (!found)
            {
                System.out.println("No pending or half-paid maintenance records found...");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void viewUpdates()
    {
        try
        {
            Connection con = DB.getConnection();

            String sql = """
                SELECT 
                    su.request_id,
                    su.site_id,
                    su.requested_by,
                    su.requested_site_type_id,
                    st.type_name AS requested_type_name,
                    su.requested_status,
                    su.status
                FROM site_update_requests su
                LEFT JOIN site_types st 
                    ON su.requested_site_type_id = st.site_type_id
                WHERE su.status = 'PENDING'
                ORDER BY su.request_id
            """;

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            boolean found = false;

            System.out.println("----- Pending Update Requests -----");

            while (rs.next())
            {
                found = true;

                int requestId = rs.getInt("request_id");
                int siteId = rs.getInt("site_id");
                int requestedBy = rs.getInt("requested_by");

                Integer requestedTypeId = rs.getObject("requested_site_type_id", Integer.class);
                String requestedTypeName = rs.getString("requested_type_name");
                String requestedStatus = rs.getString("requested_status");

                System.out.println("Request ID  : " + requestId);
                System.out.println("Site ID     : " + siteId);
                System.out.println("Requested By: User ID " + requestedBy);

                if (requestedTypeId != null)
                {
                    System.out.println("Change Type : SITE TYPE");
                    System.out.println("New Type    : " + requestedTypeName);
                }
                else if (requestedStatus != null)
                {
                    System.out.println("Change Type : SITE STATUS");
                    System.out.println("New Status  : " + requestedStatus);
                }
                else
                {
                    System.out.println("Change Type : UNKNOWN (Invalid Request)");
                }

                System.out.println("-----------------------------------");
            }

            if (!found)
            {
                System.out.println("No Pending updation requests found...");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void acceptOrRejectUpdate()
    {
        System.out.print("Enter Request Id of request: ");
        int reqId = Integer.parseInt(InputService.readLine());

        try
        {
            Connection con = DB.getConnection();

            String sql = """
                SELECT request_id, site_id, requested_site_type_id, requested_status
                FROM site_update_requests
                WHERE request_id = ? AND status = 'PENDING'
            """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, reqId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
            {
                System.out.println("No PENDING request found with given request Id!");
                return;
            }

            int siteId = rs.getInt("site_id");
            int requestedTypeId = rs.getInt("requested_site_type_id");
            boolean isTypeNull = rs.wasNull();
            String requestedStatus = rs.getString("requested_status");

            System.out.println("1. Approve");
            System.out.println("2. Reject");
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(InputService.readLine());

            if (choice == 2)
            {
                String rejectSql = "UPDATE site_update_requests SET status = 'REJECTED' WHERE request_id = ?";
                PreparedStatement psReject = con.prepareStatement(rejectSql);
                psReject.setInt(1, reqId);
                psReject.executeUpdate();

                System.out.println("Request REJECTED...");
                return;
            }

            if (choice != 1)
            {
                System.out.println("Invalid choice!");
                return;
            }

            if (requestedStatus != null)
            {
                // Check site type
                String checkTypeSql = """
                    SELECT st.type_name
                    FROM sites s
                    JOIN site_types st ON s.site_type_id = st.site_type_id
                    WHERE s.site_id = ?
                """;

                PreparedStatement psType = con.prepareStatement(checkTypeSql);
                psType.setInt(1, siteId);
                ResultSet rsType = psType.executeQuery();

                if (!rsType.next())
                {
                    System.out.println("Site not found!");
                    return;
                }

                String currentTypeName = rsType.getString("type_name");

                if ("OPEN".equals(requestedStatus) && !"Open Site".equalsIgnoreCase(currentTypeName))
                {
                    System.out.println("Cannot set status to OPEN for non 'Open Site' type!...");
                    System.out.println("Request remains PENDING...");
                    return;
                }

                String updateSiteSql = "UPDATE sites SET site_status = ? WHERE site_id = ?";
                PreparedStatement psUpdateSite = con.prepareStatement(updateSiteSql);
                psUpdateSite.setString(1, requestedStatus);
                psUpdateSite.setInt(2, siteId);
                psUpdateSite.executeUpdate();
            }
            else
            {
                if (isTypeNull)
                {
                    System.out.println("Invalid request: No type or status provided!");
                    return;
                }

                String updateTypeSql = "UPDATE sites SET site_type_id = ? WHERE site_id = ?";
                PreparedStatement psUpdateType = con.prepareStatement(updateTypeSql);
                psUpdateType.setInt(1, requestedTypeId);
                psUpdateType.setInt(2, siteId);
                psUpdateType.executeUpdate();
            }

    
            String approveSql = "UPDATE site_update_requests SET status = 'APPROVED' WHERE request_id = ?";
            PreparedStatement psApprove = con.prepareStatement(approveSql);
            psApprove.setInt(1, reqId);
            psApprove.executeUpdate();

            System.out.println("Request APPROVED and site updated successfully!");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void showMenu()
    {
        while (true)
        {
            try
            {
                System.out.println("\n===== ADMIN MENU =====");
                System.out.println("1. Display Owner Details");
                System.out.println("2. Edit Owner Details");
                System.out.println("3. See Pending Details (All)");
                System.out.println("4. View Update Requests");
                System.out.println("5. Accept / Reject Update");
                System.out.println("6. Logout");
                System.out.print("Enter choice: ");

                int choice = Integer.parseInt(InputService.reader.readLine());

                switch (choice)
                {
                    case 1:
                        displayOwnerDetails();
                        break;

                    case 2:
                        editOwnerDetails();
                        break;

                    case 3:
                        seePendingdetails();
                        break;

                    case 4:
                        viewUpdates();
                        break;

                    case 5:
                        acceptOrRejectUpdate();
                        break;

                    case 6:
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Invalid choice! Try again.");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}

class Owner extends CurrentUser
{
    public Owner(int userId, String username)
    {
        super(userId, username);
    }

    private int selectedSiteId;


    public void setSelectedSiteId(int siteId)
    {
        this.selectedSiteId = siteId;
    }

    public int getSelectedSiteId()
    {
        return this.selectedSiteId;
    }

    public void viewSiteDetails()
    {
        try
        {
            Connection con = DB.getConnection();

            String sql =
                "SELECT s.site_id, s.site_number, s.site_status, " +
                "       st.type_name, sz.length_ft, sz.width_ft " +
                "FROM sites s " +
                "JOIN site_types st ON s.site_type_id = st.site_type_id " +
                "JOIN site_sizes sz ON s.size_id = sz.size_id " +
                "WHERE s.owner_id = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, this.userId);

            ResultSet rs = ps.executeQuery();

            System.out.println("----- Your Site Details -----");

            boolean found = false;

            while (rs.next())
            {
                found = true;

                int siteId = rs.getInt("site_id");
                int siteNumber = rs.getInt("site_number");
                String status = rs.getString("site_status");
                String type = rs.getString("type_name");
                int length = rs.getInt("length_ft");
                int width = rs.getInt("width_ft");

                System.out.println("Site ID     : " + siteId);
                System.out.println("Site Number : " + siteNumber);
                System.out.println("Type        : " + type);
                System.out.println("Size        : " + length + " x " + width + " ft");
                System.out.println("Status      : " + status);
                System.out.println("-----------------------------");
            }

            if (!found)
            {
                System.out.println("No site assigned to you yet.");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void requestSiteDetailsUpdation()
    {
        System.out.println("\n--- Request Site Detail Update ---");
        System.out.println("1. Request Site Type Change");
        System.out.println("2. Request Site Status Change");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");

        int choice = Integer.parseInt(InputService.readLine());

        if (choice == 0)
        {
            return;
        }

        int siteId = this.getSelectedSiteId();

        try
        {
            Connection con = DB.getConnection();

            Integer requestedTypeId = null;
            String requestedStatus = null;

            if (choice == 1)
            {
                String typeSql = "SELECT site_type_id, type_name FROM site_types";
                PreparedStatement psType = con.prepareStatement(typeSql);
                ResultSet rsType = psType.executeQuery();

                System.out.println("Available Site Types:");
                while (rsType.next())
                {
                    System.out.println(
                        rsType.getInt("site_type_id") + " - " + rsType.getString("type_name")
                    );
                }

                System.out.print("Enter new Site Type ID: ");
                requestedTypeId = Integer.parseInt(InputService.readLine());
            }
            else if (choice == 2)
            {
                System.out.println("Select new status:");
                System.out.println("1. OPEN");
                System.out.println("2. OCCUPIED");
                System.out.print("Enter choice: ");

                int st = Integer.parseInt(InputService.readLine());

                if (st == 1)
                    requestedStatus = "OPEN";
                else if (st == 2)
                    requestedStatus = "OCCUPIED";
                else
                {
                    System.out.println("Invalid status choice!");
                    return;
                }
            }
            else
            {
                System.out.println("Invalid choice!");
                return;
            }

            String insertSql =
                "INSERT INTO site_update_requests " +
                "(site_id, requested_by, requested_site_type_id, requested_status, status) " +
                "VALUES (?, ?, ?, ?, 'PENDING')";

            PreparedStatement psInsert = con.prepareStatement(insertSql);
            psInsert.setInt(1, siteId);
            psInsert.setInt(2, this.userId);

            if (requestedTypeId != null)
                psInsert.setInt(3, requestedTypeId);
            else
                psInsert.setNull(3, Types.INTEGER);

            if (requestedStatus != null)
                psInsert.setString(4, requestedStatus);
            else
                psInsert.setNull(4, Types.VARCHAR);

            psInsert.executeUpdate();

            System.out.println("Update request submitted successfully!");
            System.out.println("Waiting for admin approval...");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void makeMaintaincePayment()
    {
        int currentYear = LocalDate.now().getYear();
        int siteId = this.getSelectedSiteId();

        try
        {
            Connection con = DB.getConnection();
            String dueSql =
                "SELECT due_id, total_amount, status " +
                "FROM maintenance_dues " +
                "WHERE site_id = ? AND bill_year = ?";

            PreparedStatement psDue = con.prepareStatement(dueSql);
            psDue.setInt(1, siteId);
            psDue.setInt(2, currentYear);

            ResultSet rsDue = psDue.executeQuery();

            if (!rsDue.next())
            {
                System.out.println("No maintenance due found for this site for year " + currentYear);
                return;
            }

            int dueId = rsDue.getInt("due_id");
            double totalAmount = rsDue.getDouble("total_amount");
            String status = rsDue.getString("status");

            String paidSql =
                "SELECT SUM(paid_amount) AS paid_sum " +
                "FROM maintenance_payments " +
                "WHERE due_id = ?";

            PreparedStatement psPaid = con.prepareStatement(paidSql);
            psPaid.setInt(1, dueId);

            ResultSet rsPaid = psPaid.executeQuery();

            double paidSoFar = 0.0;
            if (rsPaid.next())
            {
                double temp = rsPaid.getDouble("paid_sum");
                if (!rsPaid.wasNull())
                {
                    paidSoFar = temp;
                }
            }

            double remaining = totalAmount - paidSoFar;

            System.out.println("----- Maintenance Details -----");
            System.out.println("Year           : " + currentYear);
            System.out.println("Total Amount   : " + totalAmount);
            System.out.println("Paid So Far    : " + paidSoFar);
            System.out.println("Remaining Due  : " + remaining);
            System.out.println("Status         : " + status);
            System.out.println("--------------------------------");

            if (remaining <= 0)
            {
                System.out.println("Maintenance already fully paid for this year ✅");
                return;
            }

            System.out.print("Enter amount to pay: ");
            double payAmount = Double.parseDouble(InputService.readLine());

            if (payAmount <= 0)
            {
                System.out.println("Invalid amount!");
                return;
            }

            if (payAmount > remaining)
            {
                System.out.println("You cannot pay more than remaining amount!");
                return;
            }

            String insertPaySql =
                "INSERT INTO maintenance_payments (due_id, paid_amount, payment_date) " +
                "VALUES (?, ?, CURRENT_DATE)";

            PreparedStatement psInsert = con.prepareStatement(insertPaySql);
            psInsert.setInt(1, dueId);
            psInsert.setDouble(2, payAmount);
            psInsert.executeUpdate();

            double newRemaining = remaining - payAmount;

            String newStatus;
            if (newRemaining == 0)
            {
                newStatus = "PAID";
            }
            else
            {
                newStatus = "PARTIALLY_PAID";
            }

            String updateStatusSql =
                "UPDATE maintenance_dues SET status = ? WHERE due_id = ?";

            PreparedStatement psUpdate = con.prepareStatement(updateStatusSql);
            psUpdate.setString(1, newStatus);
            psUpdate.setInt(2, dueId);
            psUpdate.executeUpdate();

            System.out.println("Payment successful");
            System.out.println("New Remaining Due: " + newRemaining);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void showMenu()
    {
        while (true)
        {
            try
            {
                System.out.println("\n===== OWNER MENU =====");
                System.out.println("1. View My Site Details");
                System.out.println("2. Request Site Details Update");
                System.out.println("3. Pay Maintenance");
                System.out.println("4. Logout");
                System.out.print("Enter choice: ");

                int choice = Integer.parseInt(InputService.readLine());

                switch (choice)
                {
                    case 1:
                        viewSiteDetails();
                        break;

                    case 2:
                        requestSiteDetailsUpdation();
                        break;

                    case 3:
                        makeMaintaincePayment();
                        break;

                    case 4:
                        System.out.println("Logging out...");
                        return;

                    default:
                        System.out.println("Invalid choice! Try again.");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}


class Menu
{

}


public class MainApplication
{
    public static void main(String[] args)
    {
        System.out.println("===== Layout Maintenance Application =====");

        while (true)
        {
            CurrentUser user = Auth.login();

            if (user != null)
            {
                user.showMenu();
            }
            else
            {
                System.out.println("Login failed. Try again.\n");
            }
        }
    }
}


// javac MainApplication.java
// java -cp ".;d:\Basic Java Training Codes and Notes\Jan 28 SQL\postgresql-42.7.9.jar" MainApplication.java