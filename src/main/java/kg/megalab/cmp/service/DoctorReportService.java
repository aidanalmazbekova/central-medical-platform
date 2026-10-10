package kg.megalab.cmp.service;

import kg.megalab.cmp.model.entity.Doctor;
import kg.megalab.cmp.model.enums.AppointmentStatus;
import kg.megalab.cmp.repo.AppointmentRepo;
import kg.megalab.cmp.repo.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorReportService {

    @Value("${spring.kafka.report-topic}")
    private String reportTopic;
    private final DoctorRepo doctorRepo;
    private final AppointmentRepo appointmentRepo;
    private final KafkaTemplate<Long, String> kafkaTemplate;

    public String generateReport(int year) {

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Doctor> doctors = doctorRepo.findAll();

        StringBuilder report = new StringBuilder();

        report.append("Годовой отчет за ").append(year).append("\n\n");

        for (Doctor doctor : doctors) {
            long completed = appointmentRepo.countAppointmentsForReport(doctor.getId(), AppointmentStatus.COMPLETED, startDate, endDate);
            report.append("Врач: ").append(doctor.getLastName()).append(" ").append(doctor.getFirstName()).append("\n");
            report.append("Специальность: ").append(doctor.getSpecialty().getName()).append("\n");
            report.append("Завершенных приемов: ").append(completed).append("\n\n");
        }
        return report.toString();
    }

    @Scheduled(cron = "0 0 9 31 1 *", zone = "Asia/Bishkek")
    public void sendAnnualReport() {
        int year = LocalDate.now().getYear() - 1;
        String report = generateReport(year);

        kafkaTemplate.send(reportTopic, report);
        System.out.println("Годовой отчет отправлен в Kafka");
    }

}