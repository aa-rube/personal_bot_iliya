package app.service;

import app.model.Referral;
import app.repository.ReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository repo;

    public void updateRefUser(Long r, Long ref) {
        repo.save(new Referral(r, ref));
    }

    // Обновленный метод в сервисе
    public int updateRefUserWithCount(Long r, Long ref) {
        // Сохраняем нового реферала
        updateRefUser(r, ref);
        // Вычисляем timestamp для 24 часов назад
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        // Получаем количество рефералов за последние 24 часа
        return repo.countByReferrerIdAndTimestampsGreaterThan(ref, twentyFourHoursAgo);
    }

    public Map<String, String> getUsrLevel(Long chatId) {
        /* ---------- 1. личные приглашения ---------- */
        List<Referral> lvl1 =
                repo.findByReferrerId(chatId);
        long lvl1Cnt = lvl1.size();

        /* ---------- 2. баллы за 1-й уровень ---------- */
        long lvl1Pts;
        if (lvl1Cnt <= 30) {
            lvl1Pts = lvl1Cnt * 10;
        } else if (lvl1Cnt <= 100) {
            lvl1Pts = 30 * 10 + (lvl1Cnt - 30) * 7;
        } else {
            lvl1Pts = 30 * 10 + 70 * 7 + (lvl1Cnt - 100) * 5;
        }

        /* ---------- 3. 2-й уровень (3 балла за каждого) ---------- */
        List<Long> lvl1Ids = lvl1.stream()
                .map(Referral::getUserId)
                .collect(Collectors.toList());
        long lvl2Cnt = lvl1Ids.isEmpty() ? 0 : repo.findByReferrerIdIn(lvl1Ids).size();
        long lvl2Pts = lvl2Cnt * 3;

        long totalPts = lvl1Pts + lvl2Pts;

        /* ---------- 4. бейдж по количеству личных приглашений ---------- */
        String label = "Новичок \uD83D\uDC7D";
        if (lvl1Cnt <= 2) {
            label = "Любознательный пользователь \uD83D\uDD30";
        } else if (lvl1Cnt <= 9) {
            label = "Поделился с друзьями \uD83D\uDC65";
        } else if (lvl1Cnt <= 24) {
            label = "Вдохновитель окружения \uD83D\uDE80";
        } else if (lvl1Cnt <= 49) {
            label = "Просветитель \uD83C\uDF1F";
        } else if (lvl1Cnt > 50) {
            label = "Амбассадор нейросетей \uD83D\uDC51";
        }

        /* ---------- 5. ответ ---------- */
        return Map.of(
                "l", label,                // Label
                "b", String.valueOf(totalPts), // Balls
                "l1", String.valueOf(lvl1Cnt),
                "l2", String.valueOf(lvl2Cnt)
        );
    }
}