package org.knvvl.exam.repos;

import java.util.List;

import org.knvvl.exam.entities.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Integer>
{
    List<Picture.PictureView> getPictureByOrderById();

    Picture findTopByOrderByIdDesc();

    Picture findByFilename(String fileName);
}
