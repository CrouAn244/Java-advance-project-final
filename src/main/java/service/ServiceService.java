package service;

import dao.IServiceDAO;
import dao.impl.ServiceDAOImpl;
import java.math.BigDecimal;
import java.util.List;
import model.Service;
import util.Validator;

public class ServiceService {
	private final IServiceDAO serviceDAO;

	public ServiceService() {
		this.serviceDAO = new ServiceDAOImpl();
	}

	public List<Service> getAllServices() {
		return serviceDAO.findAll();
	}

	public Service createService(String name, BigDecimal price) {
		String validatedName = Validator.requireNotBlank(name, "Ten dich vu");
		BigDecimal validatedPrice = validatePrice(price);
		if (serviceDAO.findByName(validatedName).isPresent()) {
			throw new IllegalArgumentException("Ten dich vu da ton tai.");
		}

		Service service = new Service();
		service.setName(validatedName);
		service.setPrice(validatedPrice);
		return serviceDAO.save(service);
	}

	public Service updateService(int id, String name, BigDecimal price) {
		Service existing = serviceDAO.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Khong tim thay dich vu voi ID " + id + "."));

		String validatedName = Validator.requireNotBlank(name, "Ten dich vu");
		BigDecimal validatedPrice = validatePrice(price);
		serviceDAO.findByName(validatedName).ifPresent(found -> {
			if (!found.getId().equals(existing.getId())) {
				throw new IllegalArgumentException("Ten dich vu da ton tai.");
			}
		});

		existing.setName(validatedName);
		existing.setPrice(validatedPrice);
		boolean updated = serviceDAO.update(existing);
		if (!updated) {
			throw new IllegalArgumentException("Cap nhat dich vu that bai.");
		}
		return existing;
	}

	public void deleteService(int id) {
		boolean deleted = serviceDAO.deleteById(id);
		if (!deleted) {
			throw new IllegalArgumentException("Khong tim thay dich vu voi ID " + id + " de xoa.");
		}
	}

	private BigDecimal validatePrice(BigDecimal price) {
		if (price == null) {
			throw new IllegalArgumentException("Gia dich vu khong duoc de trong.");
		}
		if (price.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Gia dich vu khong duoc am.");
		}
		return price;
	}
}
