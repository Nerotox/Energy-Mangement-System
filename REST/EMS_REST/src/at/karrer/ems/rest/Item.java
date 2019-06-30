package at.karrer.ems.rest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

@Path("/item")
public class Item {

	@Path("{name}")
	@GET
	@Produces("application/json")
	public Response getItemDataFromLabelAllTime(@PathParam("name") String name)
			throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection dbCon = null;
		String dbUrl = "jdbc:sqlite:/home/pi/EMS/ems.db";
		dbCon = DriverManager.getConnection(dbUrl);
		PreparedStatement pstmt = dbCon.prepareStatement("SELECT * from energydata where name = ?");
		pstmt.setString(1, name);
		ResultSet rs = pstmt.executeQuery();
		JSONArray json = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			int colCount = rsmd.getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= colCount; i++) {
				String colName = rsmd.getColumnName(i);
				obj.put(colName, rs.getObject(colName));
			}
			json.put(obj);
		}
		return Response.status(200).entity(json.toString()).build();
	}

	@Path("{name}/{date}")
	@GET
	@Produces("application/json")
	public Response getItemDataFromLabelWithStartDate(@PathParam("name") String name, @PathParam("date") String date)
			throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection dbCon = null;
		String dbUrl = "jdbc:sqlite:/home/pi/EMS/ems.db";
		dbCon = DriverManager.getConnection(dbUrl);
		PreparedStatement pstmt = dbCon.prepareStatement("SELECT * from energydata where name = ? and date > ?");
		pstmt.setString(1, name);
		pstmt.setString(2, date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8) + " "
				+ date.substring(8, 10) + ":" + date.substring(10, 12) + ":" + date.substring(12, 14));
		ResultSet rs = pstmt.executeQuery();
		JSONArray json = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			int colCount = rsmd.getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= colCount; i++) {
				String colName = rsmd.getColumnName(i);
				obj.put(colName, rs.getObject(colName));
			}
			json.put(obj);
		}
		return Response.status(200).entity(json.toString()).build();
	}

	@Path("{name}/{startdate}/{enddate}")
	@GET
	@Produces("application/json")
	public Response getItemDataFromLabelWithStartDate(@PathParam("name") String name,
			@PathParam("startdate") String startDate, @PathParam("enddate") String endDate)
			throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection dbCon = null;
		String dbUrl = "jdbc:sqlite:/home/pi/EMS/ems.db";
		dbCon = DriverManager.getConnection(dbUrl);
		PreparedStatement pstmt = dbCon
				.prepareStatement("SELECT * from energydata where name = ? and date between ? and ?");
		pstmt.setString(1, name);
		pstmt.setString(2,
				startDate.substring(0, 4) + "-" + startDate.substring(4, 6) + "-" + startDate.substring(6, 8) + " "
						+ startDate.substring(8, 10) + ":" + startDate.substring(10, 12) + ":"
						+ startDate.substring(12, 14));
		pstmt.setString(3, endDate.substring(0, 4) + "-" + endDate.substring(4, 6) + "-" + endDate.substring(6, 8) + " "
				+ endDate.substring(8, 10) + ":" + endDate.substring(10, 12) + ":" + endDate.substring(12, 14));
		ResultSet rs = pstmt.executeQuery();
		JSONArray json = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			int colCount = rsmd.getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= colCount; i++) {
				String colName = rsmd.getColumnName(i);
				obj.put(colName, rs.getObject(colName));
			}
			json.put(obj);
		}
		return Response.status(200).entity(json.toString()).build();
	}
	
	@Path("all")
	@GET
	@Produces("application/json")
	public Response getItems()
			throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection dbCon = null;
		String dbUrl = "jdbc:sqlite:/home/pi/EMS/ems.db";
		dbCon = DriverManager.getConnection(dbUrl);
		PreparedStatement pstmt = dbCon
				.prepareStatement("SELECT * from Items");

		ResultSet rs = pstmt.executeQuery();
		JSONArray json = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			int colCount = rsmd.getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= colCount; i++) {
				String colName = rsmd.getColumnName(i);
				obj.put(colName, rs.getObject(colName));
			}
			json.put(obj);
		}
		return Response.status(200).entity(json.toString()).build();
	}

}
