package dao;

import java.util.List;
import java.util.Optional;
import model.Equipment;

public interface IEquipmentDAO {
	List<Equipment> findAll();

	Optional<Equipment> findById(int id);

	Optional<Equipment> findByName(String name);

	Equipment save(Equipment equipment);

	boolean update(Equipment equipment);

	boolean deleteById(int id);

	boolean updateAvailableQuantity(int id, int availableQuantity);
}
