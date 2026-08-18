package com.health.care.services;

import com.health.care.dtos.MedicineOrderRequest;
import com.health.care.dtos.PharmacyRequest;
import com.health.care.entities.MedicineOrder;
import com.health.care.entities.MedicineOrderItem;
import com.health.care.entities.Pharmacy;
import com.health.care.enums.MedicineOrderStatus;
import com.health.care.repositories.MedicineOrderRepository;
import com.health.care.repositories.PharmacyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class PharmacyService {

    private static final Logger logger = LoggerFactory.getLogger(PharmacyService.class);
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("40.00");

    private final PharmacyRepository pharmacies;
    private final MedicineOrderRepository medicineOrders;

    public PharmacyService(PharmacyRepository pharmacies, MedicineOrderRepository medicineOrders) {
        this.pharmacies = pharmacies;
        this.medicineOrders = medicineOrders;
    }

    public Pharmacy addPharmacy(PharmacyRequest request) {
        logger.info("Registering pharmacy partner '{}'", request.name());
        Pharmacy pharmacy = new Pharmacy(null, request.name(), request.address(), request.phone(), true, true,
                money(request.commissionRate()));
        Pharmacy saved = pharmacies.save(pharmacy);
        logger.info("Pharmacy '{}' registered", saved.getId());
        return saved;
    }

    public List<Pharmacy> availablePharmacies() {
        List<Pharmacy> available = pharmacies.findByActiveTrueAndVerifiedTrue();
        logger.debug("Found {} available verified pharmacies", available.size());
        return available;
    }

    public MedicineOrder placeMedicineOrder(String patient, MedicineOrderRequest request) {
        logger.info("Placing medicine order for patient '{}' with pharmacy '{}'", patient, request.pharmacyId());
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
        MedicineOrder saved = medicineOrders.save(new MedicineOrder(null, patient, pharmacy.getId(), pharmacy.getName(), items,
                subtotal, DELIVERY_FEE, total, commission, MedicineOrderStatus.PLACED, request.deliveryAddress(), now, now));
        logger.info("Medicine order '{}' created for patient '{}'", saved.getId(), patient);
        return saved;
    }

    public List<MedicineOrder> patientOrders(String patient) {
        return medicineOrders.findByPatientUsernameOrderByCreatedAtDesc(patient);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
