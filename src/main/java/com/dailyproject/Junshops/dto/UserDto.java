package com.dailyproject.Junshops.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {

    /**
     * User Data transfer object
     */
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<OrderDto> orders;
    private CartDto cart;

}
