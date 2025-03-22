package mk.ukim.finki.wp.kol2024g1.repository;

import mk.ukim.finki.wp.kol2024g1.model.Hotel;
import mk.ukim.finki.wp.kol2024g1.model.Reservation;

import java.util.List;

public interface ReservationRepository extends JpaSpecificationRepository<Reservation, Long> {

    List<Reservation> findAllByHotel(Hotel hotel);

}
