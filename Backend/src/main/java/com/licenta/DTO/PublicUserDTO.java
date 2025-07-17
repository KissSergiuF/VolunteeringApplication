package com.licenta.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private String profilePicture;
}
