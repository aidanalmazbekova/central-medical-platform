package kg.megalab.cmp.model.entity;

import jakarta.persistence.*;
import kg.megalab.cmp.model.enums.LabAppointmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "lab_appointments")
public class LabAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "lab_slot_id")
    private LabSlot labSlot;

    @Enumerated(EnumType.STRING)
    private LabAppointmentStatus status;

    @CreationTimestamp
    private OffsetDateTime createdAt;

}