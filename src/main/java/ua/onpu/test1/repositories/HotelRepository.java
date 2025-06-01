package ua.onpu.test1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.onpu.test1.models.Hotel;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
