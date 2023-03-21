package org.knvvl.exam.rest;

import static org.knvvl.exam.rest.QuestionRestService.GSON;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;

import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.services.ExamRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("api")
public class PictureRestService
{
    @Autowired
    private ExamRepositories examRepositories;

    @GetMapping(value = "/pictures", produces = APPLICATION_JSON_VALUE)
    String getPictures()
    {
        JsonArray all = new JsonArray();
        for (Picture.PictureView picture : examRepositories.getPictureRepository().getPictureByOrderById())
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", picture.getId());
            json.addProperty("filename", picture.getFilename());
            json.addProperty("filesize", picture.getFileSize());
            json.addProperty("url", "/api/pictures/" + picture.getId());
            all.add(json);
        }
        return GSON.toJson(all);
    }

    @GetMapping(value = "/pictures/{pictureId}", produces = IMAGE_JPEG_VALUE)
    byte[] getPictureBytes(@PathVariable("pictureId") int pictureId)
    {
        try
        {
            Picture picture = examRepositories.getPictureRepository().getReferenceById(pictureId);
            return picture.getFileData();
        }
        catch (EntityNotFoundException e)
        {
            throw new ResponseStatusException(NOT_FOUND, "Picture not found in database: " + pictureId);
        }
    }

    @PostMapping(value = "/pictures", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadNewPicture(@RequestParam("file") MultipartFile file) throws IOException
    {
        byte[] bytes = file.getBytes();
        if (bytes.length == 0)
        {
            return ResponseEntity.status(BAD_REQUEST).body("No file to upload was found");
        }
        Picture picture = new Picture();
        picture.setFilename(retrieveFilename(file));
        picture.setFileData(bytes);
        examRepositories.addPicture(picture);
        return ResponseEntity.status(OK).body(null);
    }

    private static String retrieveFilename(MultipartFile file)
    {
        String filename = file.getOriginalFilename();
        if (filename == null)
            filename = file.getName();
        if (filename.toLowerCase().endsWith(".jpg"))
            filename = filename.substring(0, filename.length() - 4);
        return filename;
    }

    @PostMapping(value = "/pictures/{pictureId}", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updatePicture(@PathVariable("pictureId") int pictureId, @RequestParam("file") MultipartFile file) throws IOException
    {
        Picture picture = examRepositories.getPictureRepository().getReferenceById(pictureId);
        picture.setFileData(file.getBytes());
        examRepositories.savePicture(picture);
        return ResponseEntity.status(OK).body(null);
    }

    @DeleteMapping(path = "pictures/{pictureId}", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deletePicture(@PathVariable("pictureId") int pictureId)
    {
        try
        {
            examRepositories.deletePicture(pictureId);
        }
        catch (DataIntegrityViolationException e)
        {
            return ResponseEntity.status(BAD_REQUEST).body("Cannot delete: " + e.getMessage());
        }
        return ResponseEntity.status(OK).body(null);
    }
}
