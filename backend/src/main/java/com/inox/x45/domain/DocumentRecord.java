package com.inox.x45.domain;

import com.inox.x45.domain.enums.DocumentType;
import com.inox.x45.domain.enums.LinkedEntityType;
import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Metadata for a file stored in Azure Blob Storage (Section 9). The file itself never touches this table. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "document_record")
public class DocumentRecord extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "blob_ref", nullable = false, length = 500)
    private String blobRef;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "linked_entity_type", length = 30)
    private LinkedEntityType linkedEntityType;

    @Column(name = "linked_entity_id")
    private Long linkedEntityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private AppUser uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}
