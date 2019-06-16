package ems;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Item;

/**
 * This is the Main processing loop for the Energy Management System Energy
 * polling service. The EMS is supposed to be started as systemd.process on a
 * raspberry pi. 1 Argument is required to start the server: The IP of the
 * OpenHAB Server.
 * 
 * Other Things needed for the Server: Internet Connection SQLite Installation
 * (sqlite3)
 * 
 * @author Alex
 *
 */
public class Main {

	public static void main(String[] args) {

		Connection dbCon = null;
		try {
			System.out.println("Starting the Energy Management System Server Version 1.0.");

			// Checking the input parameters
			if (args.length != 1) {
				System.out.println("Wrong Argument Count. Please refer to \"help\" for usage.");
				return;
			}
			if (args[0] == "help" || args[0] == "-h" || args[0] == "-help") {
				System.out.println("Energy Management System Version 0.1");
				System.out.println("Usage:\nsystemctl start ems@\"[IP-Address:Port]\".service");
				return;
			}

			// checking if the needed packages are installed(sqlite3)
			boolean isSqliteInstalled = false;
			System.out.println("Checking if all required packages are installed...");
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "dpkg -l | grep -w sqlite3");
			Process p = pb.start();
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("ii")) {
					isSqliteInstalled = true;
				}
			}
			p.waitFor(); // Let the process finish.
			br.close();
			if (!isSqliteInstalled) {
				System.out.println(
						"Package sqlite3 is not installed. Please install it first with sudo apt-get install sqlite3");
				return;
			}
			System.out.println("All required packages are installed.");

			// parsing the IP and port from args[0]
			System.out.println("Validating provided IP and Port....");
			if (!validate(args[0])) {
				System.out.println("Invalid IP provided. Please check the that the IP-Address you entered is correct.");
				return;
			}
			String ip = args[0].split(":")[0];
			int port = Integer.parseInt(args[0].split(":")[1]);
			System.out.println("Provided valid OpenHAB IP " + ip + " and Port " + port + ".");

			// Downloading all items and parsing them to a JSON Object.
			System.out.println("Checking OpenHAB Items for Items with energy data...");
			URL openHabURL = new URL("http://" + ip + ":" + port + "/rest/items");
			HttpURLConnection con = (HttpURLConnection) openHabURL.openConnection();
			con.setRequestMethod("GET");
			int status = con.getResponseCode();

			if (status != 200) {
				System.out.println("Something went wrong with the request. Statuscode " + status + ".");
				return;
			}

			BufferedReader responseReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String responseLine;
			while ((responseLine = responseReader.readLine()) != null) {
				sb.append(responseLine + "\n");
			}
			responseReader.close();
			String response = sb.toString();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Item items[] = gson.fromJson(response, Item[].class);

			// Filtering the Items for energy items.
			List<Item> energyItems = new ArrayList<>();
			for (Item item : items) {
				if (item.stateDescription != null) {
					if (item.stateDescription.pattern != null) {
						if (item.stateDescription.pattern.contains("kWh")) {
							energyItems.add(item);
						}
					}
				}
			}
			System.out.println("Found " + energyItems.size() + " Energy Items.");
			// System.out.println(gson.toJson(energyItems));

			// SETUP SQLITE STUFF HERE
			System.out.println("Connecting to the SQLite Database...");

			String dbUrl = "jdbc:sqlite:ems.db";
			dbCon = DriverManager.getConnection(dbUrl);
			System.out.println("Connection established.");
			System.out.println("Creating Table if first start...");
			String sql = "CREATE TABLE IF NOT EXISTS Energydata(name text NOT NULL, label text NOT NULL,"
					+ " value REAL NOT NULL," + " unit text NOT NULL," + " date TEXT NOT NULL);";
			Statement dbStatement = dbCon.createStatement();
			dbStatement.execute(sql);

			System.out.println("Created/Found the Table.");

			System.out.println("Starting to poll energy data from OpenHAB...");

			// Creating the loop for getting the new data and writing it to the database.
			while (true) {
				for (Item item : energyItems) {
					URL itemUrl = new URL(item.link);
					HttpURLConnection itemCon = (HttpURLConnection) itemUrl.openConnection();
					itemCon.setRequestMethod("GET");
					int itemStatus = itemCon.getResponseCode();

					if (itemStatus != 200) {
						System.out.println("Something went wrong with the request. Statuscode " + status + ".");
					}

					BufferedReader itemResponseReader = new BufferedReader(
							new InputStreamReader(itemCon.getInputStream()));
					StringBuilder itemSB = new StringBuilder();
					String itemResponseLine;
					while ((itemResponseLine = itemResponseReader.readLine()) != null) {
						itemSB.append(itemResponseLine + "\n");
					}
					itemResponseReader.close();
					String itemResponse = itemSB.toString();
					Gson itemGson = new GsonBuilder().setPrettyPrinting().create();
					Item dbItem = itemGson.fromJson(itemResponse, Item.class);
					insert(dbItem, dbCon);
				}
				Thread.sleep(2500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dbCon != null) {
					dbCon.close();
				}
			} catch (SQLException e2) {
			}
		}

	}

	private static void insert(Item item, Connection con) throws SQLException {
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
				+ "] Name: " + item.label + " Current energy: " + item.state + " "
				+ item.stateDescription.pattern.split(" ")[1]);
		String sql = "INSERT INTO Energydata(name, label, value, unit, date) VALUES(?,?,?,?,?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, item.name);
		pstmt.setString(2, item.label);
		if (item.state.equalsIgnoreCase("NULL")) {
			pstmt.setDouble(3, 0.0);
		} else {
			pstmt.setDouble(3, Double.parseDouble(item.state));
		}
		pstmt.setString(4, item.stateDescription.pattern.split(" ")[1]);
		pstmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		pstmt.executeUpdate();
	}

	private static final Pattern PATTERN = Pattern.compile("^"
			+ "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}|localhost|(([0-9]{1,3}\\.){3})[0-9]{1,3}):[0-9]{1,5}$");

	public static boolean validate(final String ip) {
		return PATTERN.matcher(ip).matches();
	}

}
