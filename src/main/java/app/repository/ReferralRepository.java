package app.repository;

import app.model.Referral;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Map;

public interface ReferralRepository extends MongoRepository<Referral, Long> {
    List<Referral> findByReferrerId(Long referrerId);    // 1-й уровень
    List<Referral> findByReferrerIdIn(List<Long> ids);   // 2-й уровень
    int countByReferrerIdAndTimestampsGreaterThan(Long referrerId, Long timestamp);
}