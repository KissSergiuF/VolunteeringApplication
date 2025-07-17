package com.licenta.DTO;
import com.licenta.model.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private String role;
    private String profilePicture;
    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.telephone = user.getTelephone();
        this.role = user.getRole();
        this.profilePicture = user.getProfilePicture();
    }
}

