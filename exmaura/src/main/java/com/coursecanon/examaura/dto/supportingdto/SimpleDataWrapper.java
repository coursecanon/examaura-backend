package com.coursecanon.examaura.dto.supportingdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDataWrapper<T>{
    private List<T> data;
}
