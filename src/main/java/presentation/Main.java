package presentation;

import util.DBConnection;

public class Main {
	public static void main(String[] args) {
		try {
			DBConnection.initializeDatabase();
			System.out.println("Ket noi CSDL thanh cong (" + DBConnection.getConnectionSummary() + ").");
			new AuthUI().run();
		} catch (IllegalStateException e) {
			System.out.println("Khoi dong that bai: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Da xay ra loi khong mong muon: " + e.getMessage());
		}
	}
}
