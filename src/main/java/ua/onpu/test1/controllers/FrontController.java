package ua.onpu.test1.controllers;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.onpu.test1.models.Booking;
import ua.onpu.test1.models.Hotel;
import ua.onpu.test1.models.Room;
import ua.onpu.test1.models.User;
import ua.onpu.test1.repositories.BookingRepository;
import ua.onpu.test1.repositories.HotelRepository;
import ua.onpu.test1.repositories.RoomRepository;
import ua.onpu.test1.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FrontController {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public FrontController(UserRepository userRepository, HotelRepository hotelRepository, RoomRepository roomRepository, BookingRepository bookingRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") @Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("usernameError", "Username already taken");
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/hotels")
    public String showHotels(Model model) {
        List<Hotel> hotels = hotelRepository.findAll();
        model.addAttribute("hotels", hotels);
        return "hotels";
    }

    @GetMapping("/rooms")
    public String showRooms(@RequestParam Long hotelId, Model model, Authentication authentication) {
        String currentUsername = authentication.getName();
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        Map<Long, String> roomBookingMap = new HashMap<>();
        List<Booking> allBookings = bookingRepository.findAllByRoomHotelId(hotelId);

        for (Booking booking : allBookings) {
            roomBookingMap.put(booking.getRoom().getId(), booking.getGuestName());
        }

        Hotel hotel = hotelRepository.findById(hotelId).get();

        model.addAttribute("rooms", rooms);
        model.addAttribute("hotel", hotel);
        model.addAttribute("roomBookingMap", roomBookingMap);
        model.addAttribute("currentUsername", currentUsername);
        return "rooms";
    }

    @GetMapping("/myrooms")
    public String showAccountRooms(Model model, Authentication authentication) {
        String currentUsername = authentication.getName();
        List<Booking> bookings = bookingRepository.findByGuestName(currentUsername);
        List<Room> rooms = bookings.stream().map(Booking::getRoom).collect(Collectors.toList());

        model.addAttribute("rooms", rooms);
        return "myrooms";
    }


    @GetMapping("/book-room/{roomId}")
    public String bookRoom(@PathVariable Long roomId, Authentication auth, RedirectAttributes redirectAttrs) {
        String username = auth.getName();

        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Room not found.");
            return "redirect:/rooms";
        }

        boolean isBooked = !bookingRepository.findByRoom(roomOpt.get()).isEmpty();
        if (isBooked) {
            redirectAttrs.addFlashAttribute("error", "Room is already booked.");
            return "redirect:/rooms";
        }

        Booking booking = new Booking();
        booking.setRoom(roomOpt.get());
        booking.setGuestName(username);
        bookingRepository.save(booking);

        Room room = roomOpt.get();
        return "redirect:/rooms?hotelId=" + room.getHotel().getId();
    }

    @GetMapping("/unbook-room/{roomId}")
    public String unbookRoom(@PathVariable Long roomId, Authentication auth, RedirectAttributes redirectAttrs) {
        String username = auth.getName();

        List<Booking> bookings = bookingRepository.findByGuestName(username);
        Optional<Booking> userBooking = bookings.stream()
                .filter(b -> b.getRoom().getId().equals(roomId))
                .findFirst();

        if (userBooking.isPresent()) {
            bookingRepository.delete(userBooking.get());
        } else {
            redirectAttrs.addFlashAttribute("error", "You have no booking for this room.");
        }

        Long hotelId = roomRepository.findById(roomId).map(room -> room.getHotel().getId()).orElse(null);
        return "redirect:/rooms?hotelId=" + hotelId;
    }
}
