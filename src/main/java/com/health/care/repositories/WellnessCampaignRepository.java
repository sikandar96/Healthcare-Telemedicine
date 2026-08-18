package com.health.care.repositories;

import com.health.care.entities.*;
import com.health.care.enums.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WellnessCampaignRepository extends MongoRepository<WellnessCampaign, String> {
    List<WellnessCampaign> findByStatusOrderByStartDateAsc(CampaignStatus status);
}
