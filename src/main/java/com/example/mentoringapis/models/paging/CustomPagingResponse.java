package com.example.mentoringapis.models.paging;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CustomPagingResponse<T> {
    List<T> content;
    String nextPage;
}
