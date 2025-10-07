package com.recolectaedu.dto.request;

import com.recolectaedu.model.enums.Plan;
import jakarta.validation.constraints.NotNull;

public record MembresiaRequestDTO(
        @NotNull Plan plan,   // MONTHLY o ANNUAL
        boolean autoRenew     // true/false
) {}
