package sarik.dev.fileuploaddownload.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import sarik.dev.fileuploaddownload.entity.Attachment;
import sarik.dev.fileuploaddownload.entity.AttachmentContent;
import sarik.dev.fileuploaddownload.repository.AttachmentContentRepository;
import sarik.dev.fileuploaddownload.repository.AttachmentRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentContentRepository attachmentContentRepository;

    private final String uploadDirectory = "uploads"; // Upload qilinadigan fayllar saqlanadigan papka

    public AttachmentController(AttachmentRepository attachmentRepository, AttachmentContentRepository attachmentContentRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentContentRepository = attachmentContentRepository;
    }

    /**
     * Faylni yuklab, ma'lumotlar bazasiga saqlaydi.
     *
     * @param request Multipart HTTP so'rov fayli.
     * @return Yuklash operatsiyasining natijasi.
     * @throws IOException Agar faylni yuklashda I/O xatosi yuz bersa.
     */
    @PostMapping("/upload")
    public String uploadFile(MultipartHttpServletRequest request) throws IOException {
        MultipartFile file = extractFirstFile(request); // So'rovdan birinchi faylni chiqaradi
        if (file != null) {
            return saveAttachment(file); // Faylni saqlaydi va natijani qaytaradi
        }
        return "File upload failed"; // Fayl null bo'lsa, xato xabari qaytaradi
    }

    /**
     * Faylni yuklab, fayl tizimiga saqlaydi.
     *
     * @param request Multipart HTTP so'rov fayli.
     * @return Yuklash operatsiyasining natijasi.
     * @throws IOException Agar faylni yuklashda I/O xatosi yuz bersa.
     */
    @PostMapping("/uploadSystem")
    public String uploadToFileSystem(MultipartHttpServletRequest request) throws IOException {
        MultipartFile file = extractFirstFile(request); // So'rovdan birinchi faylni chiqaradi
        if (file != null) {
            return saveAttachmentToFileSystem(file); // Faylni fayl tizimiga saqlaydi va natijani qaytaradi
        }
        return "File upload failed"; // Fayl null bo'lsa, xato xabari qaytaradi
    }

    /**
     * Ma'lumotlar bazasidagi faylni ID bo'yicha yuklaydi.
     *
     * @param id       Yuklanishi kerak bo'lgan faylning IDsi.
     * @param response HTTP javobi uchun ob'ekt.
     * @throws IOException Agar faylni yuklashda I/O xatosi yuz bersa.
     */
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id); // ID bo'yicha faylni olish
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            Optional<AttachmentContent> optionalContent = attachmentContentRepository.findByAttachmentId(id); // Fayl kontentini olish
            if (optionalContent.isPresent()) {
                sendFile(response, attachment.getFileOriginalName(), attachment.getContentType(), optionalContent.get().getFile()); // Faylni javobga jo'natadi
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found"); // Agar kontent topilmasa, xato qaytaradi
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found"); // Agar fayl topilmasa, xato qaytaradi
        }
    }

    /**
     * Fayl tizimidan faylni yuklaydi.
     *
     * @param id       Yuklanishi kerak bo'lgan faylning IDsi.
     * @param response HTTP javobi uchun ob'ekt.
     * @throws IOException Agar faylni yuklashda I/O xatosi yuz bersa.
     */
    @GetMapping("downloadFromSystem/{id}")
    public void downloadFromFileSystem(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id); // ID bo'yicha faylni olish
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            Path filePath = Paths.get(uploadDirectory, attachment.getFileName()); // Fayl yo'lini yaratish
            if (Files.exists(filePath)) {
                sendFile(response, attachment.getFileOriginalName(), attachment.getContentType(), new FileInputStream(filePath.toFile())); // Faylni javobga jo'natadi
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found"); // Agar fayl topilmasa, xato qaytaradi
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found"); // Agar fayl topilmasa, xato qaytaradi
        }
    }

    /**
     * So'rovdan birinchi yuklangan faylni chiqaradi.
     *
     * @param request Multipart HTTP so'rov fayli.
     * @return Yuklangan fayl yoki null.
     */
    private MultipartFile extractFirstFile(MultipartHttpServletRequest request) {
        Iterator<String> fileNames = request.getFileNames(); // So'rovdan fayl nomlarini olish
        return fileNames.hasNext() ? request.getFile(fileNames.next()) : null; // Birinchi faylni qaytaradi yoki null
    }

    /**
     * Yuklangan faylni ma'lumotlar bazasiga saqlaydi.
     *
     * @param file Saqlanadigan fayl.
     * @return Yuklash operatsiyasining natijasi.
     * @throws IOException Agar faylni saqlashda I/O xatosi yuz bersa.
     */
    private String saveAttachment(MultipartFile file) throws IOException {
        Attachment attachment = createAttachment(file); // Faylni qo'shish ob'ektini yaratadi
        Attachment savedAttachment = attachmentRepository.save(attachment); // Faylni repositoryga saqlaydi

        AttachmentContent attachmentContent = new AttachmentContent(); // Fayl kontentini yaratadi
        attachmentContent.setFile(file.getBytes()); // Fayl ma'lumotlarini o'rnatadi
        attachmentContent.setAttachment(savedAttachment); // Kontentni faylga bog'laydi
        attachmentContentRepository.save(attachmentContent); // Kontentni repositoryga saqlaydi

        return "File successfully uploaded, ID: " + savedAttachment.getId(); // Muvaffaqiyatli xabarnoma qaytaradi
    }

    /**
     * Yuklangan faylni fayl tizimiga saqlaydi.
     *
     * @param file Saqlanadigan fayl.
     * @return Yuklash operatsiyasining natijasi.
     * @throws IOException Agar faylni saqlashda I/O xatosi yuz bersa.
     */
    private String saveAttachmentToFileSystem(MultipartFile file) throws IOException {
        Attachment attachment = createAttachment(file); // Faylni qo'shish ob'ektini yaratadi
        String randomName = generateRandomFileName(Objects.requireNonNull(file.getOriginalFilename())); // Tasodifiy fayl nomini yaratadi
        attachment.setFileName(randomName); // Tasodifiy nomni faylga o'rnatadi

        attachmentRepository.save(attachment); // Faylni repositoryga saqlaydi
        Path path = Paths.get(uploadDirectory, randomName); // Fayl uchun yo'l yaratadi
        Files.copy(file.getInputStream(), path); // Faylni belgilangan yo'lga ko'chiradi

        return "File successfully uploaded, ID: " + attachment.getId(); // Muvaffaqiyatli xabarnoma qaytaradi
    }

    /**
     * Yuklangan fayldan fayl qo'shish ob'ektini yaratadi.
     *
     * @param file Yuklangan fayl.
     * @return Yaratilgan Attachment ob'ekti.
     */
    private Attachment createAttachment(MultipartFile file) {
        Attachment attachment = new Attachment(); // Yangi fayl ob'ektini yaratadi
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null) {
            throw new IllegalArgumentException("Original filename cannot be null"); // Agar original fayl nomi null bo'lsa, xato tashlaydi
        }

        attachment.setFileOriginalName(originalFilename); // Original fayl nomini o'rnatadi
        attachment.setSize(file.getSize()); // Fayl o'lchamini o'rnatadi
        attachment.setContentType(file.getContentType()); // Fayl turini o'rnatadi
        return attachment; // Yaratilgan ob'ekti qaytaradi
    }

    /**
     * Tasodifiy fayl nomini yaratadi.
     *
     * @param originalFilename Original fayl nomi.
     * @return Tasodifiy fayl nomi.
     */
    private String generateRandomFileName(String originalFilename) {
        String[] split = originalFilename.split("\\."); // Fayl kengaytmasini olish
        return UUID.randomUUID() + "." + split[split.length - 1]; // Tasodifiy fayl nomini qaytaradi
    }

    /**
     * Faylni HTTP javobiga jo'natadi.
     *
     * @param response        HTTP javobi uchun ob'ekt.
     * @param fileName        Fayl nomi.
     * @param contentType     Fayl turi.
     * @param fileInputStream Fayl oqimi.
     * @throws IOException Agar faylni jo'natishda I/O xatosi yuz bersa.
     */
    private void sendFile(HttpServletResponse response, String fileName, String contentType, FileInputStream fileInputStream) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\""); // Fayl nomini belgilaydi
        response.setContentType(contentType); // Fayl turini belgilaydi
        FileCopyUtils.copy(fileInputStream, response.getOutputStream()); // Fayl oqimini javobga yuboradi
    }

    /**
     * Faylni HTTP javobiga jo'natadi (byte[] versiyasi).
     *
     * @param response    HTTP javobi uchun ob'ekt.
     * @param fileName    Fayl nomi.
     * @param contentType Fayl turi.
     * @param fileData    Fayl ma'lumotlari.
     * @throws IOException Agar faylni jo'natishda I/O xatosi yuz bersa.
     */
    private void sendFile(HttpServletResponse response, String fileName, String contentType, byte[] fileData) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\""); // Fayl nomini belgilaydi
        response.setContentType(contentType); // Fayl turini belgilaydi
        FileCopyUtils.copy(fileData, response.getOutputStream()); // Fayl ma'lumotlarini javobga yuboradi
    }
}
