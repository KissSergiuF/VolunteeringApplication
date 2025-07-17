package com.licenta.DTO;

import lombok.Data;
import java.util.List;
@Data
public class CertificateRequestDTO {
    private String organizationName;
    private List<ParticipantHoursDTO> participants;
}
