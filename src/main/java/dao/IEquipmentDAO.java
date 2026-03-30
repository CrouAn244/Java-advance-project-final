package dao;

import java.util.List;
import java.util.Optional;
import model.Equipment;

public interface IEquipmentDAO {
	List<Equipment> findAll();

	Optional<Equipment> findById(int id);

	boolean updateAvailableQuantity(int id, int availableQuantity);
}
