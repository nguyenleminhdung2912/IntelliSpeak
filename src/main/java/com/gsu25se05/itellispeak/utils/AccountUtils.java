package com.gsu25se05.itellispeak.utils;

import com.gsu25se05.itellispeak.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccountUtils {

    public User getCurrentAccount(){
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (object instanceof User) {
            return (User) object;
        } else {
            // Return null if the principal is not an instance of Account
            return null;
        }
    }
}