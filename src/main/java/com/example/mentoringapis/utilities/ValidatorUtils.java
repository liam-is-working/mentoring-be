package com.example.mentoringapis.utilities;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class ValidatorUtils {
    private ValidatorUtils() {
    }
    public static boolean isFptStudentEMail(String email){
        return Optional.ofNullable(email)
                .map(mailString -> {
                    var pattern = Pattern.compile("^\\w*\\d{4,}(@fpt.edu.vn)$");

                    return pattern.matcher(mailString).matches();
                })
                .orElse(false);
    }

}
