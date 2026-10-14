package kg.megalab.cmp.repo;

import kg.megalab.cmp.model.entity.Specialty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class SpecialtyRepoIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private SpecialtyRepo specialtyRepo;

    @Test
    void saveSpecialty() {
        Specialty specialty = new Specialty();
        specialty.setName("Кардиология");
        specialty.setDescription("Заболевания сердца");

        Specialty saved = specialtyRepo.save(specialty);

        assertNotNull(saved.getId());
        assertEquals("Кардиология", saved.getName());
    }

    @Test
    void existsByNameIgnoreCase() {
        Specialty specialty = new Specialty();
        specialty.setName("Неврология");
        specialtyRepo.save(specialty);
        boolean exists = specialtyRepo.existsByNameIgnoreCase("НЕВРОЛОГИЯ");
        assertTrue(exists);
    }

    @Test
    void findSpecialtyById() {
        Specialty specialty = new Specialty();
        specialty.setName("Педиатрия");
        Specialty saved = specialtyRepo.save(specialty);
        Specialty found = specialtyRepo.findById(saved.getId()).orElseThrow();
        assertEquals("Педиатрия", found.getName());
    }

}
