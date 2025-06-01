package ua.onpu.test1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.onpu.test1.models.Booking;
import ua.onpu.test1.models.Room;
import ua.onpu.test1.repositories.BookingRepository;
import ua.onpu.test1.repositories.RoomRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public BookingController(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Optional<Room> room = roomRepository.findById(booking.getRoom().getId());
        if (room.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        booking.setRoom(room.get());
        Booking savedBooking = bookingRepository.save(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
    }

    @GetMapping("/by-guest")
    public List<Booking> getBookingsByGuestName(@RequestParam String guestName) {
        return bookingRepository.findByGuestName(guestName);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id, Authentication auth) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Booking booking = bookingOpt.get();
        if (!booking.getGuestName().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
