package sarik.dev.fileuploaddownload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sarik.dev.fileuploaddownload.entity.AttachmentContent;

import java.util.Optional;

/**
 * {@link AttachmentContent} entitilarini boshqarish uchun repository interfeysi.
 * Ushbu interfeys AttachmentContent ma'lumotlar bazasida CRUD operatsiyalarini bajarish uchun metodlarni taqdim etadi.
 */
public interface AttachmentContentRepository extends JpaRepository<AttachmentContent, Long> {

    /**
     * Berilgan attachment ID ga asoslangan AttachmentContent ni topadi.
     *
     * @param attachmentId Olingan attachment ID si, uning mazmuni qaytarilishi kerak.
     * @return Optional<AttachmentContent> - Topilgan AttachmentContent ni o'z ichiga olgan Optional,
     * agar attachment ID si bo'yicha ma'lumot topilmasa bo'sh bo'ladi.
     */
    Optional<AttachmentContent> findByAttachmentId(Long attachmentId);
}
