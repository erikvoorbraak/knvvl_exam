package org.knvvl.exam.repos;

import java.util.List;

import org.knvvl.exam.entities.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Nullable;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Integer>
{
    List<Picture.PictureView> getPictureByOrderById();

    @Nullable
    Picture findTopByOrderByIdDesc();

    @Nullable
    Picture findByFilename(String fileName);
}
