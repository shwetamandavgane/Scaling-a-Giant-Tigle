package com.northeastern.bsds.store.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private Integer storeId;
    private Integer custId;
    private Integer itemId;
    private Integer itemCount;
    private Integer date;

    @Override
    public String toString() {
        return "Message{" +
            "custId=" + custId +
            ", storeId=" + storeId +
            ", itemId=" + itemId +
            ", itemCount=" + itemCount +
            ", date=" + date +
            '}';
    }
}
