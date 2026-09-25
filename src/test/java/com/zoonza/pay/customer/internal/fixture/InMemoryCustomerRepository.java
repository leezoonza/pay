package com.zoonza.pay.customer.internal.fixture;

import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.customer.internal.domain.CustomerRepository;
import com.zoonza.pay.shared.domain.PhoneNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Customer> findByPhoneNumber(PhoneNumber phoneNumber) {
        return customers.stream()
                .filter(customer -> customer.getPhoneNumber().equals(phoneNumber))
                .findFirst();
    }

    public List<Customer> savedCustomers() {
        return List.copyOf(customers);
    }
}
