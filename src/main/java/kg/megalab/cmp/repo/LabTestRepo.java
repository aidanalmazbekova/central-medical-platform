package kg.megalab.cmp.repo;

import kg.megalab.cmp.model.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTestRepo extends JpaRepository<LabTest, Long> {

    boolean existsByNameIgnoreCase(String name);

}