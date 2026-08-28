package com.fieldsync.api.customer;

public record CustomerRequest(

    String name,
    String phone,
    String email,
    String address

) {
}