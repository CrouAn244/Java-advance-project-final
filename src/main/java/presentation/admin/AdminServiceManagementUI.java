package presentation.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.Service;
import service.AdminService;
import util.ConsoleHelper;
import util.TablePrinter;
import util.Validator;

public class AdminServiceManagementUI {
	private final AdminService adminService;

	public AdminServiceManagementUI(AdminService adminService) {
		this.adminService = adminService;
	}

	public void run() {
		boolean running = true;
		while (running) {
			System.out.println();
			System.out.println(ConsoleHelper.ANSI_CYAN + "================= QUAN LY DICH VU DI KEM =================" + ConsoleHelper.ANSI_RESET);
			System.out.println("1. Xem danh sach dich vu");
			System.out.println("2. Them dich vu");
			System.out.println("3. Sua dich vu");
			System.out.println("4. Xoa dich vu");
			System.out.println("0. Quay lai");
			System.out.println(ConsoleHelper.ANSI_CYAN + "==================================================" + ConsoleHelper.ANSI_RESET);

			int choice = ConsoleHelper.promptIntInRange("Chon chuc nang: ", 0, 4);
			switch (choice) {
				case 1:
					showServices();
					break;
				case 2:
					createService();
					break;
				case 3:
					updateService();
					break;
				case 4:
					deleteService();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void showServices() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= DANH SACH DICH VU =================" + ConsoleHelper.ANSI_RESET);
		List<Service> services = adminService.getAllServices();
		if (services.isEmpty()) {
			System.out.println("Chua co dich vu nao.");
		} else {
			List<String[]> rows = new ArrayList<>();
			for (Service service : services) {
				rows.add(new String[]{
						String.valueOf(service.getId()),
						service.getName(),
						service.getPrice().toPlainString()
				});
			}
			TablePrinter.printTable(
					new String[]{"ID", "Ten dich vu", "Gia"},
					new int[]{4, 28, 13},
					rows
			);
		}
		ConsoleHelper.waitForEnter();
	}

	private void createService() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= THEM DICH VU =================" + ConsoleHelper.ANSI_RESET);
		String name = ConsoleHelper.promptWithValidation("Ten dich vu: ", value -> Validator.requireNotBlank(value, "Ten dich vu"));
		BigDecimal price = promptPrice("Gia dich vu: ");

		try {
			Service service = adminService.createService(name, price);
			System.out.println("Them dich vu thanh cong. ID: " + service.getId());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Them dich vu that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void updateService() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= SUA DICH VU =================" + ConsoleHelper.ANSI_RESET);
		int serviceId = promptExistingServiceId("Nhap ID dich vu can sua: ");
		String name = ConsoleHelper.promptWithValidation("Ten dich vu moi: ", value -> Validator.requireNotBlank(value, "Ten dich vu"));
		BigDecimal price = promptPrice("Gia dich vu moi: ");

		try {
			adminService.updateService(serviceId, name, price);
			System.out.println("Cap nhat dich vu thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Cap nhat dich vu that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void deleteService() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= XOA DICH VU =================" + ConsoleHelper.ANSI_RESET);
		int serviceId = promptExistingServiceId("Nhap ID dich vu can xoa: ");

		try {
			adminService.deleteService(serviceId);
			System.out.println("Xoa dich vu thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Xoa dich vu that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private int promptExistingServiceId(String label) {
		while (true) {
			int id = ConsoleHelper.promptPositiveInt(label);
			if (findServiceById(id) != null) {
				return id;
			}
			System.out.println("Canh bao: Khong tim thay dich vu voi ID " + id + ". Vui long nhap lai.");
		}
	}

	private Service findServiceById(int id) {
		List<Service> services = adminService.getAllServices();
		for (Service service : services) {
			if (service.getId() != null && service.getId() == id) {
				return service;
			}
		}
		return null;
	}

	private BigDecimal promptPrice(String label) {
		while (true) {
			String raw = ConsoleHelper.prompt(label);
			try {
				BigDecimal price = new BigDecimal(raw.trim());
				if (price.compareTo(BigDecimal.ZERO) < 0) {
					throw new IllegalArgumentException("Gia khong duoc am.");
				}
				return price;
			} catch (NumberFormatException e) {
				System.out.println("Canh bao: Gia phai la so hop le.");
			} catch (IllegalArgumentException e) {
				System.out.println("Canh bao: " + e.getMessage());
			}
		}
	}
}
