package mk.ukim.finki.wp.kol2024g1.config;

import jakarta.annotation.PostConstruct;
import mk.ukim.finki.wp.kol2024g1.model.Hotel;
import mk.ukim.finki.wp.kol2024g1.model.RoomType;
import mk.ukim.finki.wp.kol2024g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2024g1.service.EmployeeService;
import mk.ukim.finki.wp.kol2024g1.service.HotelService;
import mk.ukim.finki.wp.kol2024g1.service.ReservationService;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer {

    private final HotelService hotelService;
    private final ReservationService reservationService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public DataInitializer(HotelService hotelService, ReservationService reservationService, EmployeeRepository employeeRepository, EmployeeService employeeService) {
        this.hotelService = hotelService;
        this.reservationService = reservationService;
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
    }


    private RoomType randomize(int i) {
        if (i % 2 == 0) return RoomType.SINGLE;
        return RoomType.DOUBLE;
    }


    @PostConstruct
    public void initData() {

        Hotel lastHotel = null;
        for (int i = 1; i < 6; i++) {
            lastHotel = this.hotelService.create("Hotel: " + i);
        }
        String password = "pwd";

        if (!employeeRepository.existsByNameAndHotel("manager", lastHotel)) {
            employeeService.create("manager", password, lastHotel, true);

            if (!employeeRepository.existsByNameAndHotel("user", lastHotel)) {
                employeeService.create("user", password, lastHotel, false);

                for (int i = 1; i < 11; i++) {
                    this.reservationService.create("user", LocalDate.now().minusYears(25 + i), 0, this.randomize(i), this.hotelService.listAll().get((i - 1) % 5).getId());
                }
            }
        }
    }
//    @PostConstruct
//    public void initData() {
//        employeeRepository.deleteAll();
//        hotelRepository.deleteAll();
//
//        Hotel lastHotel = null;
//        for (int i = 1; i < 6; i++) {
//            lastHotel = this.hotelService.create("Hotel: " + i);
//        }
//        String password = passwordEncoder != null ? passwordEncoder.encode("user123") : "user123";
//        Employee manager = new Employee(null, "manager", password, lastHotel, true);
//        Employee receptionist = new Employee(null, "user", password, lastHotel, false);
//
//        employeeRepository.save(manager);
//        employeeRepository.save(receptionist);
//
//        for (int i = 1; i < 11; i++) {
//            this.reservationService.create(
//                    "Reservation: " + i,
//                    LocalDate.now().minusYears(25 + i),
//                    0,
//                    this.randomize(i),
//                    this.hotelService.listAll().get((int) ((i - 1) % hotelRepository.count())).getId());
//        }
//
//

}