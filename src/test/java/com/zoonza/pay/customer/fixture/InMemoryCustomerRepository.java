package com.zoonza.pay.customer.fixture;

import com.zoonza.pay.customer.domain.Customer;
import com.zoonza.pay.customer.domain.CustomerRepository;
import com.zoonza.pay.customer.domain.PhoneNumber;

import java.util.ArrayList;
import java.util.List;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final List<Customer> customers = new ArrayList<>();

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return customers.stream().anyMatch(customer -> customer.getPhoneNumber().equals(phoneNumber));
    }

    @Override
    public Customer save(Customer customer) {
        customers.add(customer);
        return customer;
    }

    public List<Customer> savedCustomers() {
        return List.copyOf(customers);
    }
}
