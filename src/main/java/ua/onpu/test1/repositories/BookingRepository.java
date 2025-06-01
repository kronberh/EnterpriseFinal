package ua.onpu.test1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.onpu.test1.models.Booking;
import ua.onpu.test1.models.Room;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuestName(String guestName);
    List<Booking> findByRoom(Room room);
    List<Booking> findAllByRoomHotelId(Long hotelId);
}
