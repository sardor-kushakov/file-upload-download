package sarik.dev.fileuploaddownload.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Attachment - Fayl ma'lumotlarini saqlash uchun mo'ljallangan klass.
 * Ushbu klass faylning asosiy ma'lumotlarini, jumladan, asl nomi,
 * hajmi, kontent turi va saqlangan fayl nomini o'z ichiga oladi.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unikal identifikator

    private String fileOriginalName; // Faylning asl nomi

    private long size; // Fayl hajmi (baytlarda)

    private String contentType; // Faylning kontent turi (masalan, image/jpeg)

    private String fileName; // Faylni saqlashda foydalaniladigan nom
}
