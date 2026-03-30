package dao;

import java.util.List;
import java.util.Optional;
import model.Service;

public interface IServiceDAO {
	List<Service> findAll();

	Optional<Service> findById(int id);

	Optional<Service> findByName(String name);

	Service save(Service service);

	boolean update(Service service);

	boolean deleteById(int id);
}
