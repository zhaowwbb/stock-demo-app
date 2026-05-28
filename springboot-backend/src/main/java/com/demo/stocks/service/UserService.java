package com.demo.stocks.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String getUser(String name) {
        System.out.println("Inside getUser()");
        return "User: " + name;
    }
}