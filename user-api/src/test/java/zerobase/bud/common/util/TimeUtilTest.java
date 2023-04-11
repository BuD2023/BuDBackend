package zerobase.bud.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilTest {
    @Test
    @DisplayName("TimeUtil 작동 테스트")
    void timeUtilTest() {
        //given
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime beforeYears = current.minusMonths(13);
        LocalDateTime beforeMonths = current.minusMonths(2);
        LocalDateTime beforeDays = current.minusDays(15);
        LocalDateTime beforeMinutes = current.minusMinutes(14);
        LocalDateTime beforeSeconds = current.minusSeconds(4);
        //when
        String year = TimeUtil.caculateTerm(beforeYears);
        String month = TimeUtil.caculateTerm(beforeMonths);
        String day = TimeUtil.caculateTerm(beforeDays);
        String minute = TimeUtil.caculateTerm(beforeMinutes);
        String second = TimeUtil.caculateTerm(beforeSeconds);
        //then
        assertTrue(year.contains("년 전"));
        assertTrue(month.contains("달 전"));
        assertTrue(day.contains("일 전"));
        assertTrue(minute.contains("분 전"));
        assertTrue(second.contains("초 전"));
    }

}