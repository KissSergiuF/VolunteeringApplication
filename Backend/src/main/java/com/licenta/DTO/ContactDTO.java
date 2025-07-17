package com.licenta.DTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactDTO {
    private String name;
    private String email;
    private String phone;
    private String message;

    public ContactDTO(){}
    public ContactDTO(String name, String email, String phone, String message){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.message = message;
    }
}
