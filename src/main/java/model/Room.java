package model;

public class Room {
	private Integer id;
	private String name;
	private Integer capacity;
	private String location;
	private String description;

	public Room() {
	}

	public Room(Integer id, String name, Integer capacity, String location, String description) {
		this.id = id;
		this.name = name;
		this.capacity = capacity;
		this.location = location;
		this.description = description;
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

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Room{" +
				"id=" + id +
				", name='" + name + '\'' +
				", capacity=" + capacity +
				", location='" + location + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}
