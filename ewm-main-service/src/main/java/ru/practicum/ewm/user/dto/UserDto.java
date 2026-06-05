package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email не может быть пустым")
    @Size(min = 6, max = 254)
    private String email;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250)
    private String name;
}
