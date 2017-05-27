package org.openjava.upay.proxy.util;

import org.openjava.upay.util.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class AjaxHttpUtils
{
    private static Logger LOG = LoggerFactory.getLogger(AjaxHttpUtils.class);

    private static final String UTF8 = "utf-8";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    public static final void sendResponse(HttpServletResponse response, Object dataPacket)
    {
        try {
            String content = JsonUtils.toJsonString(dataPacket);
            response.setContentType(JSON_CONTENT_TYPE);
            byte[] responseBytes = content.getBytes(UTF8);
            response.setContentLength(responseBytes.length);
            response.getOutputStream().write(responseBytes);
            response.flushBuffer();
        } catch (IOException iex) {
            LOG.error("Failed to write data packet back", iex);
        }
    }

    public static final String extractHttpBody(HttpServletRequest request)
    {
        String content = "";
        try {
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                content = content.concat(line);
            }
        } catch (IOException iex) {
            LOG.error("Failed to extract http body", iex);
        }
        return content;
    }
}
