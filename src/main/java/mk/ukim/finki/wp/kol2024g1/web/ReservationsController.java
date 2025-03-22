package mk.ukim.finki.wp.kol2024g1.web;

import mk.ukim.finki.wp.kol2024g1.model.Hotel;
import mk.ukim.finki.wp.kol2024g1.model.Reservation;
import mk.ukim.finki.wp.kol2024g1.model.RoomType;
import mk.ukim.finki.wp.kol2024g1.service.HotelService;
import mk.ukim.finki.wp.kol2024g1.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping({"/reservations", "/"})
public class ReservationsController {

    private final ReservationService reservationService;
    private final HotelService hotelService;

    public ReservationsController(ReservationService reservationService, HotelService hotelService) {
        this.reservationService = reservationService;
        this.hotelService = hotelService;
    }

    /**
     * This method should use the "list.html" template to display all reservations.
     * The method should be mapped on paths '/' and '/reservations'.
     * The arguments that this method takes are optional and can be 'null'.
     * In the case when the arguments are not passed (both are 'null') all reservations should be displayed.
     * If some, or all of the arguments are not 'null', the filtered reservations that are the result of the call
     * listAll method from the ReservationService should be displayed.
     * If you want to return a paginated result, you should also pass the page number and the page size as arguments.
     *
     * @param guestName
     * @param roomType
     * @param hotel
     * @param pageNum
     * @param pageSize
     * @return The view "list.html".
     */
    @GetMapping
    public String listAll(
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) Long hotel,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Model model
    ) {
        List<Hotel> hotels = hotelService.listAll();
        List<RoomType> roomTypes = Arrays.stream(RoomType.values()).toList();

        model.addAttribute("hotels", hotels);
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("page", reservationService.findPage(guestName, roomType, hotel, pageNum - 1, pageSize));
        model.addAttribute("guestName", guestName);
        model.addAttribute("roomType", roomType);
        model.addAttribute("hotel", hotel);


        return "list";
    }

    /**
     * This method should display the "form.html" template.
     * The method should be mapped on path '/reservations/add'.
     *
     * @return The view "form.html".
     */
    @GetMapping("/add")
    public String showAdd(Model model) {
        List<Hotel> hotels = hotelService.listAll();
        List<RoomType> roomTypes = Arrays.stream(RoomType.values()).toList();

        model.addAttribute("hotels", hotels);
        model.addAttribute("roomTypes", roomTypes);

        return "form";
    }

    /**
     * This method should display the "form.html" template.
     * However, in this case, all 'input' elements should be filled with the appropriate value for the reservations that is updated.
     * The method should be mapped on path '/reservations/edit/[id]'.
     *
     * @return The view "form.html".
     */
    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        List<Hotel> hotels = hotelService.listAll();
        List<RoomType> roomTypes = Arrays.stream(RoomType.values()).toList();
        Reservation reservation = reservationService.findById(id);

        model.addAttribute("hotels", hotels);
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("reservation", reservation);

        return "form";
    }

    /**
     * This method should create a reservation given the arguments it takes.
     * The method should be mapped on path '/reservations'.
     * After the reservation is created, all reservations should be displayed.
     *
     * @return The view "list.html".
     */
    @PostMapping
    public String create(@RequestParam String guestName, @RequestParam LocalDate dateCreated, @RequestParam Integer daysOfStay, @RequestParam RoomType roomType, @RequestParam Long hotelId) {
        this.reservationService.create(guestName, dateCreated, daysOfStay, roomType, hotelId);
        return "redirect:/reservations";
    }

    /**
     * This method should update a reservation given the arguments it takes.
     * The method should be mapped on path '/reservations/[id]'.
     * After the reservation is updated, all reservations should be displayed.
     *
     * @return The view "list.html".
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String guestName, @RequestParam LocalDate dateCreated, @RequestParam Integer daysOfStay, @RequestParam RoomType roomType, @RequestParam Long hotelId) {
        this.reservationService.update(id, guestName, dateCreated, daysOfStay, roomType, hotelId);
        return "redirect:/reservations";
    }

    /**
     * This method should delete the reservation that has the appropriate identifier.
     * The method should be mapped on path '/reservations/delete/[id]'.
     * After the reservation is deleted, all reservations should be displayed.
     *
     * @return The view "list.html".
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        this.reservationService.delete(id);
        return "redirect:/reservations";
    }

    /**
     * This method should implement the logic for extending the duration,
     * by adding one day to the daysOfStay.
     * The method should be mapped on path '/reservations/extend/[id]'.
     * After the operation, all reservations should be displayed.
     *
     * @return The view "list.html".
     */
    @PostMapping("/extend/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')") // ✅ Explicitly enforce access control
    public String extend(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName(); // ✅ Get logged-in user's username

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        System.out.println("Authenticated User: " + username);
        System.out.println("Assigned Authorities: " + authorities);

        Reservation reservation = reservationService.findById(id);

        if (reservation == null || !reservation.getGuestName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only extend your own reservations.");
        }

        this.reservationService.extendStay(id);
        return "redirect:/reservations";
    }
}
