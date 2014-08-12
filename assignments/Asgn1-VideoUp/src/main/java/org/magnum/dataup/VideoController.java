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
package org.magnum.dataup;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

    /**
     * You will need to create one or more Spring controllers to fulfill the
     * requirements of the assignment. If you use this file, please rename it to
     * something other than "AnEmptyController"
     */

    private VideoFileManager videoFileManager;
    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long, Video> videos = new HashMap<Long, Video>();

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return videos.values();
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        checkAndSetId(v);
        String url = getUrlBaseForLocalServer() + "/video/" + v.getId()
                + "/data";
        v.setDataUrl(url);
        videos.put(v.getId(), v);
        return v;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
    public @ResponseBody VideoStatus setVideoData(
            @PathVariable(VideoSvcApi.ID_PARAMETER) long id,
            @RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData,
            HttpServletResponse response) throws IOException {
        Video v = videos.get(id);
        if (v == null) {
            response.sendError(404, "Video not found");
            return null;
        }
        getVideoFileManager().saveVideoData(videos.get(id),
                videoData.getInputStream());
        return new VideoStatus(VideoState.READY);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
    public void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
            HttpServletResponse response) throws IOException {
        Video v = videos.get(id);
        if (v == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Video with id " + id + " was not found");
        } else if (!getVideoFileManager().hasVideoData(v)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Video with id " + id + "has no video data");
        } else {
            OutputStream out = response.getOutputStream();
            getVideoFileManager().copyVideoData(v, out);
        }
    }

    private VideoFileManager getVideoFileManager() throws IOException {
        if (videoFileManager == null) {
            videoFileManager = VideoFileManager.get();
        }
        return videoFileManager;
    }

    private void checkAndSetId(Video video) {
        if (video.getId() == 0) {
            video.setId(currentId.incrementAndGet());
        }
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String base = "http://"
                + request.getServerName()
                + ((request.getServerPort() != 80) ? ":"
                        + request.getServerPort() : "");
        return base;
    }
}
