package com.example.mentoringapis.unit;

import com.example.mentoringapis.utilities.DateTimeUtils;
import org.junit.jupiter.api.Test;

public class TimeUtilsTest {
    @Test
    void test(){
        var returnFromDb = DateTimeUtils.nowInVietnam();
        returnFromDb.getHour();
    }
}
