package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.RoleType;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {

    @NotEmpty(message = "At least one role must be specified")
    private Set<RoleType> roles;
}
