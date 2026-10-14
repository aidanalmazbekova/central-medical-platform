package kg.megalab.laboratory.repo;

import kg.megalab.laboratory.model.entity.LabRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LabRequestRepo extends JpaRepository<LabRequest, Long> {

    boolean existsByRequestId(UUID requestId);

}