package com.northeastern.bsds.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreItem {
    private Integer storeItemId;
    private Integer storeId;
    private Integer itemId;
    private Integer itemCount;
}
