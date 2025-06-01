package ua.onpu.test1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.onpu.test1.models.Hotel;
import ua.onpu.test1.repositories.HotelRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelRepository hotelRepository;

    @Autowired
    public HotelController(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @GetMapping
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable Long id) {
        Optional<Hotel> hotel = hotelRepository.findById(id);
        return hotel.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Hotel> addHotel(@RequestBody Hotel hotel) {
        Hotel savedHotel = hotelRepository.save(hotel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHotel);
    }
}
