package com.health.care.healthcare;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service("healthcareFeatureService")
public class HealthcareService {
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("40.00");
    private static final BigDecimal CONSULTATION_COMMISSION_RATE = new BigDecimal("0.15");

    private final DoctorRepository doctors;
    private final ConsultationRepository consultations;
    private final PharmacyRepository pharmacies;
    private final MedicineOrderRepository medicineOrders;
    private final HealthProgramRepository programs;
    private final PreventiveReminderRepository reminders;

    public HealthcareService(DoctorRepository doctors, ConsultationRepository consultations,
                             PharmacyRepository pharmacies, MedicineOrderRepository medicineOrders,
                             HealthProgramRepository programs, PreventiveReminderRepository reminders) {
        this.doctors = doctors;
        this.consultations = consultations;
        this.pharmacies = pharmacies;
        this.medicineOrders = medicineOrders;
        this.programs = programs;
        this.reminders = reminders;
    }

    public DoctorProfile registerDoctor(DoctorRequest request) {
        DoctorProfile doctor = new DoctorProfile(null, request.username(), request.name(), request.specialization(),
                request.licenseNumber(), true, true, money(request.consultationFee()), request.bio());
        return doctors.save(doctor);
    }

    public List<DoctorProfile> availableDoctors() {
        return doctors.findByCertifiedTrueAndAvailableTrue();
    }

    public Consultation bookConsultation(String patient, ConsultationRequest request) {
        DoctorProfile doctor = doctors.findById(request.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        if (!doctor.isCertified() || !doctor.isAvailable()) {
            throw new IllegalArgumentException("Doctor is not currently available for consultations");
        }
        BigDecimal fee = money(doctor.getConsultationFee());
        BigDecimal commission = money(fee.multiply(CONSULTATION_COMMISSION_RATE));
        Consultation consultation = new Consultation(null, patient, doctor.getId(), doctor.getName(), request.type(),
                ConsultationStatus.REQUESTED, request.scheduledAt(), null, null, fee, commission,
                money(fee.subtract(commission)), "https://call.healthcare.local/room/" + UUID.randomUUID(), Instant.now());
        return consultations.save(consultation);
    }

    public List<Consultation> patientConsultations(String patient) {
        return consultations.findByPatientUsernameOrderByScheduledAtDesc(patient);
    }

    public List<Consultation> consultationsFor(String username, boolean doctor) {
        if (!doctor) {
            return patientConsultations(username);
        }
        DoctorProfile profile = doctors.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found"));
        return consultations.findByDoctorIdOrderByScheduledAtDesc(profile.getId());
    }

    public Consultation updateConsultation(String id, String caller, ConsultationStatus status) {
        Consultation consultation = consultations.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found"));
        if (!consultation.getPatientUsername().equals(caller)) {
            doctors.findById(consultation.getDoctorId())
                    .filter(d -> d.getUsername().equals(caller))
                    .orElseThrow(() -> new IllegalArgumentException("You are not a participant in this consultation"));
        }
        if (consultation.getStatus() == ConsultationStatus.COMPLETED || consultation.getStatus() == ConsultationStatus.CANCELLED) {
            throw new IllegalArgumentException("A closed consultation cannot be updated");
        }
        consultation.setStatus(status);
        if (status == ConsultationStatus.IN_PROGRESS) consultation.setStartedAt(Instant.now());
        if (status == ConsultationStatus.COMPLETED) consultation.setCompletedAt(Instant.now());
        return consultations.save(consultation);
    }

    public Pharmacy addPharmacy(PharmacyRequest request) {
        Pharmacy pharmacy = new Pharmacy(null, request.name(), request.address(), request.phone(), true, true,
                money(request.commissionRate()));
        return pharmacies.save(pharmacy);
    }

    public List<Pharmacy> availablePharmacies() {
        return pharmacies.findByActiveTrueAndVerifiedTrue();
    }

    public MedicineOrder placeMedicineOrder(String patient, MedicineOrderRequest request) {
        Pharmacy pharmacy = pharmacies.findById(request.pharmacyId())
                .filter(p -> p.isActive() && p.isVerified())
                .orElseThrow(() -> new IllegalArgumentException("Verified pharmacy not found"));
        List<MedicineOrderItem> items = request.items().stream()
                .map(i -> new MedicineOrderItem(i.medicineName(), i.quantity(), money(i.unitPrice())))
                .toList();
        BigDecimal subtotal = money(items.stream().map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal total = money(subtotal.add(DELIVERY_FEE));
        BigDecimal commission = money(subtotal.multiply(pharmacy.getCommissionRate().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)));
        Instant now = Instant.now();
        return medicineOrders.save(new MedicineOrder(null, patient, pharmacy.getId(), pharmacy.getName(), items,
                subtotal, DELIVERY_FEE, total, commission, MedicineOrderStatus.PLACED, request.deliveryAddress(), now, now));
    }

    public List<MedicineOrder> patientOrders(String patient) {
        return medicineOrders.findByPatientUsernameOrderByCreatedAtDesc(patient);
    }

    public HealthProgram publishProgram(HealthProgramRequest request) {
        return programs.save(new HealthProgram(null, request.title(), request.category(), request.content(),
                request.sponsorName(), request.sponsored(), money(request.sponsorshipFee()), true, Instant.now()));
    }

    public List<HealthProgram> publishedPrograms() {
        return programs.findByPublishedTrueOrderByPublishedAtDesc();
    }

    public PreventiveReminder createReminder(String username, ReminderRequest request) {
        return reminders.save(new PreventiveReminder(null, username, request.type(), request.title(), request.details(),
                request.dueDate(), false, false, Instant.now()));
    }

    public List<PreventiveReminder> reminders(String username) {
        return reminders.findByUsernameOrderByDueDateAsc(username);
    }

    public PreventiveReminder completeReminder(String id, String username) {
        PreventiveReminder reminder = reminders.findById(id)
                .filter(r -> r.getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        reminder.setCompleted(true);
        return reminders.save(reminder);
    }

    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    public void markDueRemindersAsNotified() {
        reminders.findByDueDateLessThanEqualAndCompletedFalseAndNotifiedFalse(LocalDate.now())
                .forEach(reminder -> { reminder.setNotified(true); reminders.save(reminder); });
    }

    public RevenueSummary revenueSummary() {
        BigDecimal consultationGross = consultations.findAll().stream().map(Consultation::getFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal consultationCommission = consultations.findAll().stream().map(Consultation::getPlatformCommission).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pharmacyGross = medicineOrders.findAll().stream().map(MedicineOrder::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pharmacyCommission = medicineOrders.findAll().stream().map(MedicineOrder::getPharmacyCommission).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sponsored = programs.findAll().stream().filter(HealthProgram::isSponsored)
                .map(HealthProgram::getSponsorshipFee).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RevenueSummary(money(consultationGross), money(consultationCommission), money(pharmacyGross),
                money(pharmacyCommission), sponsored, money(consultationCommission.add(pharmacyCommission).add(sponsored)));
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
