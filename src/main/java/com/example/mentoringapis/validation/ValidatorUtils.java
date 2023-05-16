package com.example.mentoringapis.validation;

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

                    if (!pattern.matcher(mailString).matches())
                        return false;

                    pattern =  Pattern.compile("\\d{4,}(@fpt.edu.vn)");
                    var nameWithMajorCodeString = pattern.split(mailString)[0];
                    var majorCode = nameWithMajorCodeString.substring(nameWithMajorCodeString.length()-2);
                    return List.of("se","ss","sa").contains(majorCode.toLowerCase(Locale.ROOT));
                })
                .orElse(false);
    }

}
