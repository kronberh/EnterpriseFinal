package ua.onpu.test1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.onpu.test1.models.Hotel;
import ua.onpu.test1.models.Room;
import ua.onpu.test1.repositories.HotelRepository;
import ua.onpu.test1.repositories.RoomRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Autowired
    public RoomController(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    @GetMapping("/by-hotel/{hotelId}")
    public List<Room> getRoomsByHotelId(@PathVariable Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @PostMapping
    public ResponseEntity<Room> addRoom(@RequestBody Room room) {
        Optional<Hotel> hotel = hotelRepository.findById(room.getHotel().getId());
        if (hotel.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        room.setHotel(hotel.get());
        Room savedRoom = roomRepository.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }
}
