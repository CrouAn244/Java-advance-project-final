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

	public Equipment createEquipment(String name, int totalQuantity, int availableQuantity) {
		String validatedName = normalizeEquipmentName(name);
		if (equipmentDAO.findByName(validatedName).isPresent()) {
			throw new IllegalArgumentException("Ten thiet bi da ton tai.");
		}
		validateQuantities(totalQuantity, availableQuantity);

		Equipment equipment = new Equipment();
		equipment.setName(validatedName);
		equipment.setTotalQuantity(totalQuantity);
		equipment.setAvailableQuantity(availableQuantity);
		equipment.setStatus(availableQuantity > 0 ? "SAN_SANG" : "HET_HANG");
		return equipmentDAO.save(equipment);
	}

	public Equipment updateEquipment(int equipmentId, String name, int totalQuantity, int availableQuantity) {
		Equipment existing = equipmentDAO.findById(equipmentId)
				.orElseThrow(() -> new IllegalArgumentException("Khong tim thay thiet bi voi ID " + equipmentId + "."));
		String validatedName = normalizeEquipmentName(name);
		equipmentDAO.findByName(validatedName).ifPresent(found -> {
			if (!found.getId().equals(existing.getId())) {
				throw new IllegalArgumentException("Ten thiet bi da ton tai.");
			}
		});

		validateQuantities(totalQuantity, availableQuantity);
		existing.setName(validatedName);
		existing.setTotalQuantity(totalQuantity);
		existing.setAvailableQuantity(availableQuantity);
		existing.setStatus(availableQuantity > 0 ? "SAN_SANG" : "HET_HANG");

		boolean updated = equipmentDAO.update(existing);
		if (!updated) {
			throw new IllegalArgumentException("Cap nhat thiet bi that bai.");
		}
		return existing;
	}

	public void deleteEquipment(int equipmentId) {
		boolean deleted = equipmentDAO.deleteById(equipmentId);
		if (!deleted) {
			throw new IllegalArgumentException("Khong tim thay thiet bi voi ID " + equipmentId + " de xoa.");
		}
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

	private String normalizeEquipmentName(String name) {
		String validatedName = name == null ? "" : name.trim();
		if (validatedName.isEmpty()) {
			throw new IllegalArgumentException("Ten thiet bi khong duoc de trong.");
		}
		return validatedName;
	}

	private void validateQuantities(int totalQuantity, int availableQuantity) {
		if (totalQuantity <= 0) {
			throw new IllegalArgumentException("Tong so luong phai lon hon 0.");
		}
		if (availableQuantity < 0) {
			throw new IllegalArgumentException("So luong kha dung khong duoc am.");
		}
		if (availableQuantity > totalQuantity) {
			throw new IllegalArgumentException("So luong kha dung khong duoc lon hon tong so luong.");
		}
	}
}
