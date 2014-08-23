package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * An interface for a repository that can store Video objects and allow them to
 * be searched by title.
 * 
 * @author jules
 *
 */
@Repository
public interface VideoRepository extends CrudRepository<Video, Long> {

    // Find all videos with a matching title (e.g., Video.name)
    public Collection<Video> findByName(String name);

    // Find all videos that are shorter than a specified duration
    public Collection<Video> findByDurationLessThan(
    // The @Param annotation tells tells Spring Data Rest which HTTP request
    // parameter it should use to fill in the "duration" variable used to
    // search for Videos
            @Param(VideoSvcApi.DURATION_PARAMETER) long maxduration);

}