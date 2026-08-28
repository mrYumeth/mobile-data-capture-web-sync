package com.fieldsync.api.customer;

public record CustomerDeleteResponse(

    String message,
    CustomerResponse deletedCustomer

) {
}