package model;

public class Equipment {
	private Integer id;
	private String name;
	private Integer totalQuantity;
	private Integer availableQuantity;
	private String status;

	public Equipment() {
	}

	public Equipment(Integer id, String name, Integer totalQuantity, Integer availableQuantity, String status) {
		this.id = id;
		this.name = name;
		this.totalQuantity = totalQuantity;
		this.availableQuantity = availableQuantity;
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Integer getAvailableQuantity() {
		return availableQuantity;
	}

	public void setAvailableQuantity(Integer availableQuantity) {
		this.availableQuantity = availableQuantity;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Equipment{" +
				"id=" + id +
				", name='" + name + '\'' +
				", totalQuantity=" + totalQuantity +
				", availableQuantity=" + availableQuantity +
				", status='" + status + '\'' +
				'}';
	}
}
