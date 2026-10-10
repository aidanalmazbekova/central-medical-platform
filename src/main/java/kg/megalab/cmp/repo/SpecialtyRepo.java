package kg.megalab.cmp.repo;

import kg.megalab.cmp.model.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepo extends JpaRepository<Specialty, Long> {

    boolean existsByNameIgnoreCase(String name);

}
