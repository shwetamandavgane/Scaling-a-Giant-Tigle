package com.northeastern.bsds.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    private Integer storeId;
    private Integer custId;
    private Integer itemId;
    private Integer itemCount;
    private Integer date;
}
