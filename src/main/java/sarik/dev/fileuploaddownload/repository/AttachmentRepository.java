package sarik.dev.fileuploaddownload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sarik.dev.fileuploaddownload.entity.Attachment;

/**
 * {@link Attachment} entitilarini boshqarish uchun repository interfeysi.
 * Ushbu interfeys AttachmentContent ma'lumotlar bazasida CRUD operatsiyalarini bajarish uchun metodlarni taqdim etadi.
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    // Bu interfeysda qo'shimcha metodlar taqdim etilishi mumkin, agar kerak bo'lsa.
}
