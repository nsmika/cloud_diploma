package ru.netology.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files", schema = "cloud")
@Data
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String filepath;
    private Long filesize;

    public FileEntity(String filename, String filepath, Long filesize) {
        this.filename = filename;
        this.filepath = filepath;
        this.filesize = filesize;
    }
}
