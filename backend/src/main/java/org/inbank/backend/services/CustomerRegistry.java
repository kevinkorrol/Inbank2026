package org.inbank.backend.services;

import org.inbank.backend.objects.UserSegment;
import org.springframework.stereotype.Component;

@Component
public class CustomerRegistry {

    public UserSegment getUserSegment(String personalCode) {
        if (personalCode == null || personalCode.isBlank()) {
            return null;
        }

        return switch (personalCode.trim()) {
            case "49002010965" -> new UserSegment(0, true);
            case "49002010976" -> new UserSegment(100, false);
            case "49002010987" -> new UserSegment(300, false);
            case "49002010998" -> new UserSegment(1000, false);
            default -> null;
        };
    }
}
