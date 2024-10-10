package sarik.dev.fileuploaddownload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AttachmentContent - Faylning haqiqiy ma'lumotlarini saqlash uchun
 * mo'ljallangan klass. Ushbu klass faylning mazmunini
 * (byte[] ko'rinishida) va unga tegishli attachment ma'lumotlarini o'z ichiga oladi.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AttachmentContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unikal identifikator

    private byte[] file; // Faylning mazmuni (byte[] ko'rinishida)

    @OneToOne
    @JoinColumn(name = "attachment_id") // Attachment bilan bog'lanish
    private Attachment attachment; // Ulanadigan attachment ma'lumoti
}
