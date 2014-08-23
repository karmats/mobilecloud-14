/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoController {

    /**
     * You will need to create one or more Spring controllers to fulfill the
     * requirements of the assignment. If you use this file, please rename it to
     * something other than "AnEmptyController"
     * 
     * 
     */

    @Autowired
    private VideoRepository videoRepo;

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return Lists.newArrayList(videoRepo.findAll());
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        return videoRepo.save(v);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> findByTitle(
            @RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
        return videoRepo.findByName(title);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
    public @ResponseBody Video getVideoById(@PathVariable Long id) {
        System.out.println("Finding video with id " + id);
        return videoRepo.findOne(id);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
    public void likeVideo(@PathVariable Long id, HttpServletResponse response,
            Principal p) throws IOException {
        Video v = videoRepo.findOne(id);
        if (v == null) {
            response.sendError(404, "Video not found");
        } else if (v.getLikedBy().contains(p.getName())) {
            response.sendError(400, "User " + p.getName()
                    + " has already liked this video");
        } else {
            v.getLikedBy().add(p.getName());
            v.setLikes(v.getLikes() + 1);
            videoRepo.save(v);
        }
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
    public void unlikeVideo(@PathVariable Long id,
            HttpServletResponse response, Principal p) throws IOException {
        Video v = videoRepo.findOne(id);
        if (v == null) {
            response.sendError(404, "Video not found");
        } else if (!v.getLikedBy().remove(p.getName())) {
            response.sendError(400, "Video not liked by user " + p.getName());
        } else {
            v.setLikes(v.getLikes() - 1);
            videoRepo.save(v);
        }
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
    public @ResponseBody Collection<String> getUsersWhoLikedVideo(
            @PathVariable long id, HttpServletResponse response)
            throws IOException {
        Video v = videoRepo.findOne(id);
        if (v == null) {
            response.sendError(404, "Video not found");
            return null;
        }
        return v.getLikedBy();
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> findByDurationLessThan(
            @RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
        return videoRepo.findByDurationLessThan(duration);
    }

}
