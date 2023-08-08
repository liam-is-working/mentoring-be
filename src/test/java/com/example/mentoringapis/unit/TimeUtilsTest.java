package com.example.mentoringapis.unit;

import com.example.mentoringapis.service.ScheduleService;
import com.example.mentoringapis.utilities.DateTimeUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeUtilsTest {
    @Test
    void test(){
        var returnFromDb = DateTimeUtils.nowInVietnam();
        returnFromDb.getHour();
    }


    @Test
    void doesCollapseDateRangeTest(){
        assertThat(ScheduleService.doesCollapseTimeRange(
                LocalTime.of(1,0,0),
                LocalTime.of(2,0,0),
                LocalTime.of(1,30,0),
                LocalTime.of(2,30,0))).isTrue();
    }
}
