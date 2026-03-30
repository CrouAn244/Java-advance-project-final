package model;

public class BookingDetail {
    private Integer id;
    private Integer bookingId;
    private Integer equipmentId;
    private Integer serviceId;
    private Integer quantity;

    public BookingDetail() {
    }

    public BookingDetail(Integer id, Integer bookingId, Integer equipmentId, Integer serviceId, Integer quantity) {
        this.id = id;
        this.bookingId = bookingId;
        this.equipmentId = equipmentId;
        this.serviceId = serviceId;
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "BookingDetail{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", equipmentId=" + equipmentId +
                ", serviceId=" + serviceId +
                ", quantity=" + quantity +
                '}';
    }
}
