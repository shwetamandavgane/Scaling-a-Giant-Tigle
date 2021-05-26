package com.northeastern.bsds.store.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ItemResponse {
  List<Items> stores = new ArrayList<>();
}
