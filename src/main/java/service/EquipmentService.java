package service;

import dao.IEquipmentDAO;
import dao.impl.EquipmentDAOImpl;
import java.util.List;
import model.Equipment;

public class EquipmentService {
	private final IEquipmentDAO equipmentDAO;

	public EquipmentService() {
		this.equipmentDAO = new EquipmentDAOImpl();
	}

	public List<Equipment> getAllEquipments() {
		return equipmentDAO.findAll();
	}

	public Equipment updateAvailableQuantity(int equipmentId, int availableQuantity) {
		Equipment equipment = equipmentDAO.findById(equipmentId)
				.orElseThrow(() -> new IllegalArgumentException("Khong tim thay thiet bi voi ID " + equipmentId + "."));

		if (availableQuantity < 0) {
			throw new IllegalArgumentException("So luong kha dung khong duoc am.");
		}
		if (availableQuantity > equipment.getTotalQuantity()) {
			throw new IllegalArgumentException("So luong kha dung khong duoc lon hon tong so luong (" + equipment.getTotalQuantity() + ").");
		}

		boolean updated = equipmentDAO.updateAvailableQuantity(equipmentId, availableQuantity);
		if (!updated) {
			throw new IllegalArgumentException("Cap nhat so luong kha dung that bai.");
		}

		equipment.setAvailableQuantity(availableQuantity);
		equipment.setStatus(availableQuantity > 0 ? "SAN_SANG" : "HET_HANG");
		return equipment;
	}
}
