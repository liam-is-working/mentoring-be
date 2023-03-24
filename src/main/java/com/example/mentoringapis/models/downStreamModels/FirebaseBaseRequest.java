package com.example.mentoringapis.models.downStreamModels;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
public class FirebaseBaseRequest implements Serializable {
    private boolean returnSecureToken = true;
}
