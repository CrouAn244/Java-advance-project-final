package presentation.admin;

import java.util.ArrayList;
import java.util.List;
import model.Equipment;
import service.AdminService;
import util.ConsoleHelper;
import util.TablePrinter;
import util.Validator;

public class AdminEquipmentManagementUI {
	private final AdminService adminService;

	public AdminEquipmentManagementUI(AdminService adminService) {
		this.adminService = adminService;
	}

	public void run() {
		boolean running = true;
		while (running) {
			System.out.println();
			System.out.println(ConsoleHelper.ANSI_CYAN + "================= QUAN LY THIET BI DI DONG =================" + ConsoleHelper.ANSI_RESET);
			System.out.println("1. Xem danh sach thiet bi");
			System.out.println("2. Them thiet bi");
			System.out.println("3. Sua thiet bi");
			System.out.println("4. Xoa thiet bi");
			System.out.println("5. Cap nhat so luong kha dung");
			System.out.println("0. Quay lai");
			System.out.println(ConsoleHelper.ANSI_CYAN + "==================================================" + ConsoleHelper.ANSI_RESET);

			int choice = ConsoleHelper.promptIntInRange("Chon chuc nang: ", 0, 5);
			switch (choice) {
				case 1:
					showEquipments();
					break;
				case 2:
					createEquipment();
					break;
				case 3:
					updateEquipment();
					break;
				case 4:
					deleteEquipment();
					break;
				case 5:
					updateEquipmentAvailableQuantity();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void showEquipments() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= DANH SACH THIET BI =================" + ConsoleHelper.ANSI_RESET);
		List<Equipment> equipments = adminService.getAllEquipments();
		if (equipments.isEmpty()) {
			System.out.println("Chua co thiet bi nao.");
		} else {
			List<String[]> rows = new ArrayList<>();
			for (Equipment equipment : equipments) {
				rows.add(new String[]{
						String.valueOf(equipment.getId()),
						equipment.getName(),
						String.valueOf(equipment.getTotalQuantity()),
						String.valueOf(equipment.getAvailableQuantity()),
						equipment.getStatus()
				});
			}
			TablePrinter.printTable(
					new String[]{"ID", "Ten thiet bi", "Tong so luong", "So luong kha dung", "Trang thai"},
					new int[]{4, 20, 14, 18, 12},
					rows
			);
		}
		ConsoleHelper.waitForEnter();
	}

	private void createEquipment() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= THEM THIET BI =================" + ConsoleHelper.ANSI_RESET);
		String name = ConsoleHelper.promptWithValidation("Ten thiet bi: ", value -> Validator.requireNotBlank(value, "Ten thiet bi"));
		int totalQuantity = ConsoleHelper.promptPositiveInt("Tong so luong: ");
		int availableQuantity = promptAvailableQuantityNotGreaterThanTotal(totalQuantity, "So luong kha dung: ");

		try {
			Equipment equipment = adminService.createEquipment(name, totalQuantity, availableQuantity);
			System.out.println("Them thiet bi thanh cong. ID: " + equipment.getId());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Them thiet bi that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void updateEquipment() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= SUA THIET BI =================" + ConsoleHelper.ANSI_RESET);
		int equipmentId = promptExistingEquipmentId("Nhap ID thiet bi can sua: ");
		String name = ConsoleHelper.promptWithValidation("Ten thiet bi moi: ", value -> Validator.requireNotBlank(value, "Ten thiet bi"));
		int totalQuantity = ConsoleHelper.promptPositiveInt("Tong so luong moi: ");
		int availableQuantity = promptAvailableQuantityNotGreaterThanTotal(totalQuantity, "So luong kha dung moi: ");

		try {
			adminService.updateEquipment(equipmentId, name, totalQuantity, availableQuantity);
			System.out.println("Cap nhat thiet bi thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Cap nhat thiet bi that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void deleteEquipment() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= XOA THIET BI =================" + ConsoleHelper.ANSI_RESET);
		int equipmentId = promptExistingEquipmentId("Nhap ID thiet bi can xoa: ");

		try {
			adminService.deleteEquipment(equipmentId);
			System.out.println("Xoa thiet bi thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Xoa thiet bi that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void updateEquipmentAvailableQuantity() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= CAP NHAT SO LUONG KHA DUNG =================" + ConsoleHelper.ANSI_RESET);
		int equipmentId = promptExistingEquipmentId("Nhap ID thiet bi: ");
		Equipment target = findEquipmentById(equipmentId);
		String availableRaw = ConsoleHelper.promptWithValidation("Nhap so luong kha dung moi: ", value -> {
			int parsed;
			try {
				parsed = Integer.parseInt(value.trim());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("So luong kha dung phai la so nguyen.");
			}
			if (parsed < 0) {
				throw new IllegalArgumentException("So luong kha dung khong duoc am.");
			}
			if (target != null && parsed > target.getTotalQuantity()) {
				throw new IllegalArgumentException("So luong kha dung khong duoc lon hon tong so luong la " + target.getTotalQuantity() + ".");
			}
			return String.valueOf(parsed);
		});
		int availableQuantity = Integer.parseInt(availableRaw);

		try {
			Equipment equipment = adminService.updateEquipmentAvailableQuantity(equipmentId, availableQuantity);
			System.out.println("Cap nhat thanh cong. Thiet bi " + equipment.getName() + " co so luong kha dung moi la "
					+ equipment.getAvailableQuantity() + ".");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Cap nhat thiet bi that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private int promptExistingEquipmentId(String label) {
		while (true) {
			int id = ConsoleHelper.promptPositiveInt(label);
			if (findEquipmentById(id) != null) {
				return id;
			}
			System.out.println("Canh bao: Khong tim thay thiet bi voi ID " + id + ". Vui long nhap lai.");
		}
	}

	private Equipment findEquipmentById(int id) {
		List<Equipment> equipments = adminService.getAllEquipments();
		for (Equipment equipment : equipments) {
			if (equipment.getId() != null && equipment.getId() == id) {
				return equipment;
			}
		}
		return null;
	}

	private int promptAvailableQuantityNotGreaterThanTotal(int totalQuantity, String label) {
		while (true) {
			int availableQuantity = ConsoleHelper.promptNonNegativeInt(label);
			if (availableQuantity <= totalQuantity) {
				return availableQuantity;
			}
			System.out.println("Canh bao: So luong kha dung khong duoc lon hon tong so luong (" + totalQuantity + "). Vui long nhap lai.");
		}
	}
}
