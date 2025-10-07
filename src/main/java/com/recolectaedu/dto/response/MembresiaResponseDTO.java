package com.recolectaedu.dto.response;

import com.recolectaedu.model.enums.MembresiaStatus;
import com.recolectaedu.model.enums.Plan;

import java.time.Instant;

public record MembresiaResponseDTO(
        Integer id,
        Plan plan,
        MembresiaStatus status,
        boolean autoRenew,
        Instant startsAt,
        Instant endsAt
) {}
