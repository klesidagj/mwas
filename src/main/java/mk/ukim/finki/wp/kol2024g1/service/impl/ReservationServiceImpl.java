package mk.ukim.finki.wp.kol2024g1.service.impl;

import mk.ukim.finki.wp.kol2024g1.model.Employee;
import mk.ukim.finki.wp.kol2024g1.model.Hotel;
import mk.ukim.finki.wp.kol2024g1.model.Reservation;
import mk.ukim.finki.wp.kol2024g1.model.RoomType;
import mk.ukim.finki.wp.kol2024g1.model.exceptions.InvalidReservationIdException;
import mk.ukim.finki.wp.kol2024g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2024g1.repository.HotelRepository;
import mk.ukim.finki.wp.kol2024g1.repository.ReservationRepository;
import mk.ukim.finki.wp.kol2024g1.service.HotelService;
import mk.ukim.finki.wp.kol2024g1.service.ReservationService;
import mk.ukim.finki.wp.kol2024g1.service.specifications.FieldFilterSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final HotelService hotelService;
    private final EmployeeRepository employeeRepository;


    public ReservationServiceImpl(ReservationRepository reservationRepository, HotelService hotelService, EmployeeRepository employeeRepository, HotelRepository hotelRepository) {
        this.reservationRepository = reservationRepository;
        this.hotelService = hotelService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Reservation> listAll() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Employee employee = employeeRepository.findByNameIgnoreCase(username).orElse(null);

        if (employee == null || employee.getHotel() == null) {
            return List.of();
        }
        List<Reservation> reservations = reservationRepository.findAllByHotel(employee.getHotel());
        return reservations;
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id).orElseThrow(InvalidReservationIdException::new);
    }

    @Override
    public Reservation create(String guestName, LocalDate dateCreated, Integer daysOfStay, RoomType roomType, Long hotelId) {
        Reservation reservation = new Reservation(guestName, dateCreated, daysOfStay, roomType, hotelService.findById(hotelId));

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation update(Long id, String guestName, LocalDate dateCreated, Integer daysOfStay, RoomType roomType, Long hotelId) {
        Reservation reservation = this.findById(id);
        Hotel hotel = hotelService.findById(hotelId);

        reservation.setGuestName(guestName);
        reservation.setDateCreated(dateCreated);
        reservation.setDaysOfStay(daysOfStay);
        reservation.setRoomType(roomType);
        reservation.setHotel(hotel);

        return this.reservationRepository.save(reservation);
    }

    @Override
    public Reservation delete(Long id) {
        Reservation reservation = this.findById(id);
        this.reservationRepository.deleteById(id);
        return reservation;
    }

    @Override
    public Reservation extendStay(Long id) {
        Reservation reservation = this.findById(id);
        reservation.setDaysOfStay(reservation.getDaysOfStay() + 1);
        return this.reservationRepository.save(reservation);
    }

    @Override
    public Page<Reservation> findPage(String guestName, RoomType roomType, Long hotel, int pageNum, int pageSize) {
        Specification<Reservation> specification = Specification.where(FieldFilterSpecification.filterContainsText(Reservation.class, "guestName", guestName))
                .and(FieldFilterSpecification.filterEqualsV(Reservation.class, "roomType", roomType))
                .and(FieldFilterSpecification.filterEquals(Reservation.class, "hotel.id", hotel));

        return this.reservationRepository.findAll(specification, PageRequest.of(pageNum, pageSize));
    }
}
