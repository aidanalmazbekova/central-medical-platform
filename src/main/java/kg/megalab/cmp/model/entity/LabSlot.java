package kg.megalab.cmp.model.entity;

import jakarta.persistence.*;
import kg.megalab.cmp.model.enums.LabSlotStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "lab_slots")
public class LabSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lab_test_id")
    private LabTest labTest;

    private LocalDate workDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private LabSlotStatus status;

}