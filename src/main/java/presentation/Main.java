package presentation;

import util.DBConnection;

public class Main {
	public static void main(String[] args) {
		DBConnection.initializeDatabase();
		System.out.println("Ket noi CSDL thanh cong (" + DBConnection.getConnectionSummary() + ").");
		new AuthUI().run();
	}
}
