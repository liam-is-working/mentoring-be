package com.example.mentoringapis.unit;

import com.example.mentoringapis.utilities.ValidatorUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;


public class ValidatorUtilsTest {


    @ParameterizedTest
    @ValueSource(strings = {"sesese151336@fpt.edu.vn", "sasasa151336@fpt.edu.vn", "ssss151336@fpt.edu.vn"}) // six numbers
    public void isFptStudentEMail_givenValidEmail_shouldReturnTrue(String email){
        assertThat(ValidatorUtils.isFptStudentEMail(email)).isTrue();
    };

    @ParameterizedTest
    @ValueSource(strings = {"sesesz151336@fpt.edu.vn", "sasasaaaaa@fpt.edu.vn", "ssss151336@fpt.edu"}) // six numbers
    public void isFptStudentEMail_givenInvalidEmail_shouldReturnFalse(String email){
        assertThat(ValidatorUtils.isFptStudentEMail(email)).isFalse();
    };
}
