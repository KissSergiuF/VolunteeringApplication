package com.licenta.service;
import org.springframework.transaction.annotation.Transactional;
import com.licenta.DTO.EventDTO;
import com.licenta.DTO.CertificateRequestDTO;
import com.licenta.DTO.ParticipantHoursDTO;
import com.licenta.model.Event;
import com.licenta.model.User;
import com.licenta.repository.ChatMessageRepository;
import com.licenta.repository.EventRegistrationRepository;
import com.licenta.repository.EventRepository;
import com.licenta.repository.UserRepository;
import com.licenta.repository.EventBanRepository;
import com.licenta.model.EventBan;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EventBanRepository eventBanRepository;
    private final EventWaitlistService waitlistService;
    private final EmailService emailService;

    public EventDTO createEvent(EventDTO dto, Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizatorul nu a fost gasit"));

        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setCategory(dto.getCategory());
        event.setLocationName(dto.getLocation());
        event.setLatitude(dto.getLat());
        event.setLongitude(dto.getLng());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setOrganizer(organizer);
        event.setMaxVolunteers(dto.getMaxVolunteers());
        Event saved = eventRepository.save(event);
        return mapToDTO(saved);
    }

    @Transactional
    public List<EventDTO> getAllEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> allEvents = eventRepository.findAll();
        List<Event> expired = allEvents.stream()
                .filter(e -> e.getEndDate().isBefore(now) && Boolean.TRUE.equals(e.getIsActive()))
                .toList();

        expired.forEach(e -> {
            e.setIsActive(false);
            waitlistService.clearWaitlistForEvent(e.getId());
        });

        eventRepository.saveAll(expired);

        return allEvents.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventDTO> getEventsForRegisteredUser(Long userId) {
        return eventRegistrationRepository.findEventsByUserId(userId)
                .stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(this::mapToDTO)
                .toList();
    }

    public List<EventDTO> getEventsCreatedByAssociation(Long userId) {
        return eventRepository.findByOrganizer_Id(userId)
                .stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(this::mapToDTO)
                .toList();
    }

    public void deleteEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost gasit"));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new RuntimeException("Nu ai voie sa stergi acest eveniment.");
        }

        chatMessageRepository.deleteAll(chatMessageRepository.findByEventIdOrderByTimestampAsc(eventId));
        eventRegistrationRepository.deleteByEventId(eventId);
        eventRepository.deleteById(eventId);
    }

    private EventDTO mapToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setCategory(event.getCategory());
        dto.setLocation(event.getLocationName());
        dto.setLat(event.getLatitude());
        dto.setLng(event.getLongitude());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setOrganizerId(event.getOrganizer().getId());
        dto.setIsActive(event.getIsActive());

        int registeredCount = eventRegistrationRepository.countByEventId(event.getId());

        boolean isFull = event.getMaxVolunteers() != null &&
                event.getMaxVolunteers() > 0 &&
                registeredCount >= event.getMaxVolunteers();

        dto.setIsFull(isFull);
        dto.setMaxVolunteers(event.getMaxVolunteers());
        dto.setCurrentVolunteers(registeredCount);
        dto.setCertificatesGenerated(event.getCertificatesGenerated());


        return dto;
    }


    public List<EventDTO> getArchivedRegisteredEvents(Long userId) {
        return eventRegistrationRepository.findEventsByUserId(userId)
                .stream()
                .filter(e -> Boolean.FALSE.equals(e.getIsActive()))
                .map(this::mapToDTO)
                .toList();
    }

    public List<EventDTO> getArchivedCreatedEvents(Long userId) {
        return eventRepository.findByOrganizer_Id(userId)
                .stream()
                .filter(e -> Boolean.FALSE.equals(e.getIsActive()))
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public void kickUser(Long eventId, Long userId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost gasit"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new RuntimeException("Nu ai voie sa elimini utilizatori din acest eveniment.");
        }

        eventRegistrationRepository.deleteByEventIdAndUserId(eventId, userId);

        chatMessageRepository.deleteByEventIdAndSenderId(eventId, userId);

        if (!eventBanRepository.existsByEventIdAndUserId(eventId.intValue(), userId.intValue())) {
            EventBan ban = EventBan.builder()
                    .eventId(eventId.intValue())
                    .userId(userId.intValue())
                    .build();
            eventBanRepository.save(ban);
        }
        waitlistService.checkAndNotifyWaitlist(eventId);

    }

    public boolean isUserBannedFromEvent(Long eventId, Long userId) {
        return eventBanRepository.existsByEventIdAndUserId(eventId.intValue(), userId.intValue());
    }

    public EventDTO getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost gasit"));
        return mapToDTO(event);
    }

    @Transactional
    public void generateCertificates(Long eventId, CertificateRequestDTO request, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost gasit"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new RuntimeException("Nu ai voie sa generezi adeverinte pentru acest eveniment.");
        }

        Path stampPathPng = Paths.get("temp-stamps", "stamp_event_" + event.getId() + ".png");
        Path stampPathJpg = Paths.get("temp-stamps", "stamp_event_" + event.getId() + ".jpg");
        Path stampPathJpeg = Paths.get("temp-stamps", "stamp_event_" + event.getId() + ".jpeg");

        Path stampPath = null;
        if (Files.exists(stampPathPng)) stampPath = stampPathPng;
        else if (Files.exists(stampPathJpg)) stampPath = stampPathJpg;
        else if (Files.exists(stampPathJpeg)) stampPath = stampPathJpeg;

        final Path effectiveStampPath = stampPath;

        try {
            request.getParticipants().forEach(p -> {
                User user = userRepository.findById(p.getUserId())
                        .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost gasit: ID = " + p.getUserId()));

                int hours = p.getHours();
                String organizationName = request.getOrganizationName();

                byte[] pdfContent = generateCertificatePDF(user, event, hours, organizationName, effectiveStampPath);

                emailService.sendCertificateEmail(user.getEmail(), pdfContent, "Adeverinta_voluntariat_" + event.getName() + "_" + user.getFirstName() + "_" + user.getLastName() + ".pdf");

            });
        } finally {
            if (stampPath != null && Files.exists(stampPath)) {
                try {
                    Files.delete(stampPath);
                } catch (Exception e) {
                    System.err.println("Eroare la stergerea stampilei: " + e.getMessage());
                }
            }
        }
        event.setCertificatesGenerated(true);
        eventRepository.save(event);
    }



    private byte[] generateCertificatePDF(User user, Event event, int hours, String organizationName, Path stampPath) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            String fontPath = "fonts/DejaVuSans.ttf";
            com.lowagie.text.pdf.BaseFont baseFont = com.lowagie.text.pdf.BaseFont.createFont(
                    fontPath, com.lowagie.text.pdf.BaseFont.IDENTITY_H, com.lowagie.text.pdf.BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 20, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);

            String signatureFontPath = "fonts/GreatVibes-Regular.ttf";
            com.lowagie.text.pdf.BaseFont signatureBaseFont = com.lowagie.text.pdf.BaseFont.createFont(
                    signatureFontPath, com.lowagie.text.pdf.BaseFont.IDENTITY_H, com.lowagie.text.pdf.BaseFont.EMBEDDED);
            Font signatureFont = new Font(signatureBaseFont, 24);

            Paragraph title = new Paragraph("Adeverință de voluntariat", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

            String startDateFormatted = String.format("%s începând la ora %s",
                    event.getStartDate().toLocalDate().format(dateFormatter),
                    event.getStartDate().toLocalTime().withSecond(0).withNano(0).toString().substring(0,5));

            String endDateFormatted = String.format("%s sfârșit la ora %s",
                    event.getEndDate().toLocalDate().format(dateFormatter),
                    event.getEndDate().toLocalTime().withSecond(0).withNano(0).toString().substring(0,5));

            String content = String.format(
                    "Se adeverește prin prezență că %s %s a participat în calitate de voluntar la evenimentul \"%s\", " +
                            "organizat de %s, desfășurat în perioada %s - %s, la locația %s.\n\n" +
                            "Voluntarul a desfășurat un număr total de %d ore de activitate în cadrul acestui eveniment.\n\n" +
                            "Această adeverință se eliberează pentru a servi unde este necesar.\n",
                    user.getFirstName(),
                    user.getLastName(),
                    event.getName(),
                    organizationName,
                    startDateFormatted,
                    endDateFormatted,
                    event.getLocationName(),
                    hours
            );

            Paragraph paragraph = new Paragraph(content, normalFont);
            paragraph.setSpacingAfter(30f);
            document.add(paragraph);

            Paragraph footerDate = new Paragraph(
                    String.format("Data emiterii: %s\n\nSemnătura organizator:\n", java.time.LocalDate.now()),
                    normalFont);
            footerDate.setSpacingAfter(10f);
            document.add(footerDate);

            Paragraph signatureParagraph = new Paragraph(organizationName, signatureFont);
            signatureParagraph.setSpacingAfter(10f);
            document.add(signatureParagraph);

            if (stampPath != null && Files.exists(stampPath)) {
                Image stampImage = Image.getInstance(stampPath.toAbsolutePath().toString());
                stampImage.scaleToFit(100, 100);
                stampImage.setAlignment(Image.ALIGN_RIGHT);
                document.add(stampImage);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Eroare la generarea PDF-ului pentru utilizatorul: " + user.getFirstName() + " " + user.getLastName(), e);
        }
    }




    public void saveStampTemp(Long eventId, MultipartFile file) {
        try {
            Path tempDir = Paths.get(System.getProperty("user.dir"), "temp-stamps");
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            }

            if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg")) {
                throw new RuntimeException("Format invalid pentru stampila. Acceptat: PNG, JPG, JPEG.");
            }

            Path stampPath = tempDir.resolve("stamp_event_" + eventId + "." + extension);

            file.transferTo(stampPath.toFile());
        } catch (Exception e) {
            throw new RuntimeException("Eroare la salvarea stampilei temporare", e);
        }
    }




}