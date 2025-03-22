package mk.ukim.finki.wp.kol2024g1.web;


import lombok.AllArgsConstructor;
import mk.ukim.finki.wp.kol2024g1.model.Hotel;
import mk.ukim.finki.wp.kol2024g1.model.Reservation;
import mk.ukim.finki.wp.kol2024g1.model.RoomType;
import mk.ukim.finki.wp.kol2024g1.service.HotelService;
import mk.ukim.finki.wp.kol2024g1.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping({"/api/reservations"})
public class ReservationApi {

    private final ReservationService reservationService;
    private final HotelService hotelService;


    @GetMapping
    public List<Hotel> listAll() {
        return hotelService.listAll();
    }

    @PostMapping
    public Reservation create(@RequestParam String guestName,
                              @RequestParam LocalDate dateCreated,
                              @RequestParam Integer daysOfStay,
                              @RequestParam RoomType roomType,
                              @RequestParam Long hotelId) {
        return this.reservationService.create(guestName, dateCreated, daysOfStay, roomType, hotelId);
    }


    @PutMapping("/{id}")
    public Reservation update(@PathVariable Long id,
                              @RequestParam String guestName,
                              @RequestParam LocalDate dateCreated,
                              @RequestParam Integer daysOfStay,
                              @RequestParam RoomType roomType,
                              @RequestParam Long hotelId) {
        return this.reservationService.update(id, guestName, dateCreated, daysOfStay, roomType, hotelId);

    }


    @DeleteMapping("/delete/{id}")
    public Reservation delete(@PathVariable Long id) {
        return this.reservationService.delete(id);
    }

    @PostMapping("/extend/{id}")
    public Reservation extend(@PathVariable Long id) {
        return this.reservationService.extendStay(id);
    }
}
