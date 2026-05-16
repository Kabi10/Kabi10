#include "esp_camera.h"
#include <WiFi.h>
#include "esp_timer.h"
#include "img_converters.h"
#include "Arduino.h"
#include "fb_gfx.h"
#include "soc/soc.h"
#include "soc/rtc_cntl_reg.h"
#include "esp_http_server.h"

// ===========================
// WiFi Credentials
// ===========================
const char* ssid = "TELUSWiFi4176";
const char* password = "mynetwork";

// ===========================
// Camera Pin Configuration
// ===========================
#define PWDN_GPIO_NUM     32
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM      0
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27
#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       21
#define Y4_GPIO_NUM       19
#define Y3_GPIO_NUM       18
#define Y2_GPIO_NUM        5
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22

// ===========================
// Stream Configuration
// ===========================
#define PART_BOUNDARY "123456789000000000000987654321"
static const char* _STREAM_CONTENT_TYPE = "multipart/x-mixed-replace;boundary=" PART_BOUNDARY;
static const char* _STREAM_BOUNDARY = "\r\n--" PART_BOUNDARY "\r\n";
static const char* _STREAM_PART = "Content-Type: image/jpeg\r\nContent-Length: %u\r\n\r\n";

httpd_handle_t stream_httpd = NULL;
httpd_handle_t camera_httpd = NULL;

// System status variables
unsigned long lastWiFiCheck = 0;
unsigned long streamDuration = 0;
bool streamActive = false;
int frameCount = 0;
int errorCount = 0;
unsigned long bootTime = 0;

// ===========================
// HTML Interface
// ===========================
static const char PROGMEM INDEX_HTML[] = R"rawliteral(
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ESP32-CAM Stream</title>
    <style>
        body {
            font-family: Arial;
            text-align: center;
            margin: 0;
            padding: 20px;
            background: #1a1a1a;
            color: #fff;
        }
        h1 { color: #4CAF50; margin-bottom: 20px; }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: #2a2a2a;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0,0,0,0.5);
        }
        #stream {
            width: 100%;
            max-width: 640px;
            height: auto;
            border: 2px solid #4CAF50;
            border-radius: 5px;
            background: #000;
        }
        .controls {
            margin: 20px 0;
            display: flex;
            justify-content: center;
            gap: 10px;
            flex-wrap: wrap;
        }
        button {
            background: #4CAF50;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            transition: background 0.3s;
        }
        button:hover { background: #45a049; }
        button:active { transform: scale(0.95); }
        .status {
            padding: 10px;
            background: #333;
            border-radius: 5px;
            margin: 10px 0;
        }
        #status { 
            font-weight: bold;
            color: #ff9800;
        }
        .connected { color: #4CAF50 !important; }
        .error { color: #f44336 !important; }
        
        .settings {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 10px;
            margin: 10px 0;
        }
        
        .settings select {
            padding: 8px;
            border-radius: 5px;
            background: #333;
            color: white;
            border: 1px solid #555;
        }
        
        .system-info {
            text-align: left;
            background: #333;
            padding: 10px;
            border-radius: 5px;
            margin-top: 20px;
            font-size: 14px;
        }
        
        .system-info p {
            margin: 5px 0;
        }
        
        @media (max-width: 600px) {
            .controls {
                flex-direction: column;
            }
            button {
                width: 100%;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>ESP32-CAM Live Stream</h1>
        <div class="status">
            Status: <span id="status">Initializing...</span>
        </div>
        <img id="stream" src="" onerror="this.style.display='none'">
        <div class="controls">
            <button onclick="startStream()">Start Stream</button>
            <button onclick="stopStream()">Stop Stream</button>
            <button onclick="captureImage()">Capture Photo</button>
            <button onclick="resetESP()">Reset ESP32</button>
            <button onclick="location.reload()">Refresh Page</button>
        </div>
        
        <div class="settings">
            <select id="resolution" onchange="changeResolution(this.value)">
                <option value="13">UXGA (1600x1200)</option>
                <option value="12">SXGA (1280x1024)</option>
                <option value="11">HD (1280x720)</option>
                <option value="10">XGA (1024x768)</option>
                <option value="9">SVGA (800x600)</option>
                <option value="8">VGA (640x480)</option>
                <option value="7">HVGA (480x320)</option>
                <option value="6" selected>CIF (400x296)</option>
                <option value="5">QVGA (320x240)</option>
                <option value="4">HQVGA (240x176)</option>
                <option value="0">QQVGA (160x120)</option>
            </select>
            
            <select id="quality" onchange="changeQuality(this.value)">
                <option value="10">High Quality (10)</option>
                <option value="15" selected>Medium Quality (15)</option>
                <option value="20">Low Quality (20)</option>
            </select>
        </div>
        
        <div class="status">
            <div>Stream: <span id="streamUrl"></span></div>
            <div>FPS: <span id="fps">0</span></div>
        </div>
        
        <div class="system-info">
            <p>Uptime: <span id="uptime">...</span></p>
            <p>Memory (Heap): <span id="heap">...</span></p>
            <p>PSRAM: <span id="psram">...</span></p>
            <p>WiFi Signal: <span id="rssi">...</span></p>
        </div>
    </div>
    <script>
        const baseUrl = window.location.protocol + '//' + window.location.hostname;
        const streamUrl = baseUrl + ':81/stream';
        let frameCount = 0;
        let lastTime = Date.now();
        let reconnectAttempts = 0;
        let streamTimer = null;
        let infoTimer = null;
        
        document.getElementById('streamUrl').textContent = streamUrl;
        
        function updateStatus(text, className = '') {
            const status = document.getElementById('status');
            status.textContent = text;
            status.className = className;
        }
        
        function startStream() {
            const img = document.getElementById('stream');
            img.style.display = 'block';
            img.src = streamUrl + '?cb=' + Date.now();
            updateStatus('Connecting...', '');
            reconnectAttempts = 0;
            
            // Set a timer to restart stream if stuck
            if (streamTimer) clearTimeout(streamTimer);
            streamTimer = setTimeout(() => {
                if (document.getElementById('status').textContent === 'Connecting...') {
                    updateStatus('Connection Timeout, Retrying...', 'error');
                    stopStream();
                    setTimeout(startStream, 1000);
                }
            }, 10000);
            
            img.onload = function() {
                updateStatus('Connected', 'connected');
                frameCount++;
                reconnectAttempts = 0;
                const now = Date.now();
                if (now - lastTime > 1000) {
                    document.getElementById('fps').textContent = frameCount;
                    frameCount = 0;
                    lastTime = now;
                }
            };
            
            img.onerror = function() {
                reconnectAttempts++;
                if (reconnectAttempts < 5) {
                    updateStatus('Connection Failed, Retrying (' + reconnectAttempts + '/5)...', 'error');
                    setTimeout(startStream, 2000);
                } else {
                    updateStatus('Connection Failed. Please refresh page.', 'error');
                }
            };
        }
        
        function stopStream() {
            const img = document.getElementById('stream');
            img.src = '';
            img.style.display = 'none';
            updateStatus('Stopped', '');
            document.getElementById('fps').textContent = '0';
            if (streamTimer) {
                clearTimeout(streamTimer);
                streamTimer = null;
            }
        }
        
        function captureImage() {
            const timestamp = new Date().getTime();
            const link = document.createElement('a');
            link.href = baseUrl + '/capture?t=' + timestamp;
            link.download = 'capture_' + timestamp + '.jpg';
            link.click();
        }
        
        function resetESP() {
            if(confirm('Are you sure you want to reset the ESP32?')) {
                fetch(baseUrl + '/reset')
                .then(response => {
                    updateStatus('ESP32 restarting...', 'error');
                    setTimeout(() => {
                        window.location.reload();
                    }, 5000);
                })
                .catch(error => {
                    updateStatus('Reset command sent', '');
                });
            }
        }
        
        function changeResolution(value) {
            fetch(baseUrl + '/control?var=framesize&val=' + value)
            .then(response => response.text())
            .then(data => {
                console.log("Resolution changed to: " + value);
                // Restart stream if active
                if (document.getElementById('stream').style.display !== 'none') {
                    stopStream();
                    setTimeout(startStream, 500);
                }
            });
        }
        
        function changeQuality(value) {
            fetch(baseUrl + '/control?var=quality&val=' + value)
            .then(response => response.text())
            .then(data => {
                console.log("Quality changed to: " + value);
            });
        }
        
        function updateSystemInfo() {
            fetch(baseUrl + '/info')
            .then(response => response.json())
            .then(data => {
                document.getElementById('uptime').textContent = formatTime(data.uptime);
                document.getElementById('heap').textContent = formatBytes(data.heap_free);
                document.getElementById('psram').textContent = formatBytes(data.psram_free);
                document.getElementById('rssi').textContent = data.wifi_rssi + ' dBm';
            })
            .catch(error => {
                console.log('Error fetching system info:', error);
            });
        }
        
        function formatBytes(bytes) {
            if (bytes === 0) return '0 Bytes';
            const k = 1024;
            const sizes = ['Bytes', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
        }
        
        function formatTime(seconds) {
            const hours = Math.floor(seconds / 3600);
            const minutes = Math.floor((seconds % 3600) / 60);
            const secs = seconds % 60;
            return hours + 'h ' + minutes + 'm ' + secs + 's';
        }
        
        // Auto-start stream on load with a small delay
        window.onload = function() {
            setTimeout(startStream, 1000);
            // Update system info every 5 seconds
            updateSystemInfo();
            infoTimer = setInterval(updateSystemInfo, 5000);
        };
        
        // Handle page visibility changes
        document.addEventListener('visibilitychange', function() {
            if (document.visibilityState === 'hidden') {
                // Page is hidden, free resources
                stopStream();
                if (infoTimer) {
                    clearInterval(infoTimer);
                    infoTimer = null;
                }
            } else if (document.visibilityState === 'visible') {
                // Page is visible again, restart stream
                setTimeout(startStream, 1000);
                if (!infoTimer) {
                    updateSystemInfo();
                    infoTimer = setInterval(updateSystemInfo, 5000);
                }
            }
        });
    </script>
</body>
</html>
)rawliteral";

// ===========================
// Index Handler
// ===========================
static esp_err_t index_handler(httpd_req_t *req) {
    httpd_resp_set_type(req, "text/html");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_set_hdr(req, "Cache-Control", "no-cache, no-store, must-revalidate");
    return httpd_resp_send(req, INDEX_HTML, strlen(INDEX_HTML));
}

// ===========================
// Stream Handler
// ===========================
static esp_err_t stream_handler(httpd_req_t *req) {
    camera_fb_t * fb = NULL;
    esp_err_t res = ESP_OK;
    size_t _jpg_buf_len = 0;
    uint8_t * _jpg_buf = NULL;
    char * part_buf[128];
    
    static int64_t last_frame = 0;
    if(!last_frame) {
        last_frame = esp_timer_get_time();
    }
    
    // Stream timeout protection
    int64_t stream_start = esp_timer_get_time();
    frameCount = 0;
    errorCount = 0;
    streamActive = true;

    res = httpd_resp_set_type(req, _STREAM_CONTENT_TYPE);
    if(res != ESP_OK) {
        Serial.printf("Failed to set response type: %d\n", res);
        streamActive = false;
        return res;
    }

    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_set_hdr(req, "X-Framerate", "30");
    httpd_resp_set_hdr(req, "Connection", "keep-alive");
    httpd_resp_set_hdr(req, "Pragma", "no-cache");
    httpd_resp_set_hdr(req, "Cache-Control", "no-cache, no-store, must-revalidate");

    Serial.println("Stream started");
    
    while(true) {
        // Check for timeout - 5 minute maximum stream time
        int64_t current_time = esp_timer_get_time();
        streamDuration = (current_time - stream_start) / 1000000; // seconds
        
        if(streamDuration > 300) {
            Serial.println("Stream timeout protection triggered (5 minutes)");
            break;
        }
        
        // Get camera frame
        fb = esp_camera_fb_get();
        if (!fb) {
            Serial.println("Camera capture failed");
            errorCount++;
            if(errorCount > 5) {
                Serial.println("Too many capture errors, stopping stream");
                res = ESP_FAIL;
                break;
            }
            delay(100); // Small delay before retry
            continue;
        }
        
        // Calculate FPS
        int64_t fr_end = esp_timer_get_time();
        int64_t frame_time = fr_end - last_frame;
        last_frame = fr_end;
        frame_time /= 1000;
        frameCount++;
        
        // Handle JPEG conversion if needed
        if(fb->format != PIXFORMAT_JPEG) {
            bool jpeg_converted = frame2jpg(fb, 80, &_jpg_buf, &_jpg_buf_len);
            esp_camera_fb_return(fb);
            fb = NULL;
            if(!jpeg_converted) {
                Serial.println("JPEG compression failed");
                errorCount++;
                if(errorCount > 5) {
                    res = ESP_FAIL;
                    break;
                }
                continue;
            }
        } else {
            _jpg_buf_len = fb->len;
            _jpg_buf = fb->buf;
        }

        // Send headers
        size_t hlen = snprintf((char *)part_buf, 128, _STREAM_PART, _jpg_buf_len);
        res = httpd_resp_send_chunk(req, (const char *)part_buf, hlen);
        
        // Send image in chunks
        if(res == ESP_OK) {
            size_t chunksize = 0;
            size_t sent = 0;
            while(sent < _jpg_buf_len) {
                // Reduced chunk size to 2048 for better streaming
                chunksize = (_jpg_buf_len - sent) > 2048 ? 2048 : (_jpg_buf_len - sent);
                res = httpd_resp_send_chunk(req, (const char *)(_jpg_buf + sent), chunksize);
                if(res != ESP_OK) {
                    Serial.printf("Stream chunk error: %d\n", res);
                    break;
                }
                sent += chunksize;
                
                // Very short yield to allow WiFi tasks to run
                delay(1);
            }
        }
        
        // Send boundary
        if(res == ESP_OK) {
            res = httpd_resp_send_chunk(req, _STREAM_BOUNDARY, strlen(_STREAM_BOUNDARY));
            if(res != ESP_OK) {
                Serial.printf("Stream boundary error: %d\n", res);
            }
        }
        
        // Cleanup
        if(fb) {
            esp_camera_fb_return(fb);
            fb = NULL;
            _jpg_buf = NULL;
        } else if(_jpg_buf) {
            free(_jpg_buf);
            _jpg_buf = NULL;
        }
        
        // Check for errors
        if(res != ESP_OK) {
            Serial.printf("Stream error: %d\n", res);
            break;
        }
        
        // Control frame rate to reduce WiFi congestion
        int64_t fr_delta = esp_timer_get_time() - fr_end;
        fr_delta /= 1000;
        if(fr_delta < 33) { // Target ~30 FPS
            delay(33 - fr_delta);
        }
    }
    
    // Final cleanup in case of loop exit
    if(fb) {
        esp_camera_fb_return(fb);
    }
    
    // Stream ended
    streamActive = false;
    Serial.printf("Stream ended after %lu seconds, processed %d frames\n", streamDuration, frameCount);
    return res;
}

// ===========================
// Simple Stream Handler - For Testing Basic Streaming
// ===========================
static esp_err_t simple_stream_handler(httpd_req_t *req) {
    camera_fb_t * fb = NULL;
    esp_err_t res = ESP_OK;
    
    // Get single image
    fb = esp_camera_fb_get();
    if (!fb) {
        Serial.println("Camera capture failed");
        httpd_resp_send_500(req);
        return ESP_FAIL;
    }
    
    // Set content type
    httpd_resp_set_type(req, "image/jpeg");
    httpd_resp_set_hdr(req, "Cache-Control", "no-cache, no-store, must-revalidate");
    
    // Send image
    res = httpd_resp_send(req, (const char *)fb->buf, fb->len);
    
    // Return the frame buffer back to the driver for reuse
    esp_camera_fb_return(fb);
    
    return res;
}

// ===========================
// Capture Handler
// ===========================
static esp_err_t capture_handler(httpd_req_t *req) {
    camera_fb_t * fb = NULL;
    esp_err_t res = ESP_OK;

    fb = esp_camera_fb_get();
    if (!fb) {
        Serial.println("Camera capture failed");
        httpd_resp_send_500(req);
        return ESP_FAIL;
    }

    httpd_resp_set_type(req, "image/jpeg");
    httpd_resp_set_hdr(req, "Content-Disposition", "attachment; filename=capture.jpg");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_set_hdr(req, "Cache-Control", "no-cache, no-store, must-revalidate");
    
    size_t out_len, out_width, out_height;
    uint8_t * out_buf;
    
    if(fb->format != PIXFORMAT_JPEG) {
        if(!frame2jpg(fb, 90, &out_buf, &out_len)) {
            esp_camera_fb_return(fb);
            Serial.println("JPEG compression failed");
            httpd_resp_send_500(req);
            return ESP_FAIL;
        }
        res = httpd_resp_send(req, (const char *)out_buf, out_len);
        free(out_buf);
    } else {
        res = httpd_resp_send(req, (const char *)fb->buf, fb->len);
    }
    
    esp_camera_fb_return(fb);
    return res;
}

// ===========================
// System Info Handler
// ===========================
static esp_err_t system_info_handler(httpd_req_t *req) {
    char info[512];
    sprintf(info, 
        "{"
        "\"uptime\":%lu,"
        "\"heap_free\":%u,"
        "\"psram_free\":%u,"
        "\"wifi_rssi\":%d,"
        "\"stream_active\":%s,"
        "\"stream_duration\":%lu,"
        "\"frame_count\":%d"
        "}",
        (millis() - bootTime) / 1000,
        ESP.getFreeHeap(),
        ESP.getFreePsram(),
        WiFi.RSSI(),
        streamActive ? "true" : "false",
        streamDuration,
        frameCount
    );
    
    httpd_resp_set_type(req, "application/json");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_set_hdr(req, "Cache-Control", "no-cache, no-store, must-revalidate");
    return httpd_resp_send(req, info, strlen(info));
}

// ===========================
// Reset Handler
// ===========================
static esp_err_t reset_handler(httpd_req_t *req) {
    httpd_resp_set_type(req, "text/plain");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_send(req, "Restarting...", strlen("Restarting..."));
    delay(500);
    ESP.restart();
    return ESP_OK;
}

// ===========================
// Camera Control Handler
// ===========================
static esp_err_t cmd_handler(httpd_req_t *req) {
    char*  buf;
    size_t buf_len;
    char variable[32] = {0,};
    char value[32] = {0,};

    buf_len = httpd_req_get_url_query_len(req) + 1;
    if (buf_len > 1) {
        buf = (char*)malloc(buf_len);
        if(!buf){
            httpd_resp_send_500(req);
            return ESP_FAIL;
        }
        if (httpd_req_get_url_query_str(req, buf, buf_len) == ESP_OK) {
            if (httpd_query_key_value(buf, "var", variable, sizeof(variable)) == ESP_OK &&
                httpd_query_key_value(buf, "val", value, sizeof(value)) == ESP_OK) {
            } else {
                free(buf);
                httpd_resp_send_404(req);
                return ESP_FAIL;
            }
        } else {
            free(buf);
            httpd_resp_send_404(req);
            return ESP_FAIL;
        }
        free(buf);
    } else {
        httpd_resp_send_404(req);
        return ESP_FAIL;
    }

    int val = atoi(value);
    sensor_t * s = esp_camera_sensor_get();
    int res = 0;

    if(!strcmp(variable, "framesize")) {
        if(s->pixformat == PIXFORMAT_JPEG) res = s->set_framesize(s, (framesize_t)val);
    }
    else if(!strcmp(variable, "quality")) res = s->set_quality(s, val);
    else if(!strcmp(variable, "contrast")) res = s->set_contrast(s, val);
    else if(!strcmp(variable, "brightness")) res = s->set_brightness(s, val);
    else if(!strcmp(variable, "saturation")) res = s->set_saturation(s, val);
    else if(!strcmp(variable, "gainceiling")) res = s->set_gainceiling(s, (gainceiling_t)val);
    else if(!strcmp(variable, "colorbar")) res = s->set_colorbar(s, val);
    else if(!strcmp(variable, "awb")) res = s->set_whitebal(s, val);
    else if(!strcmp(variable, "agc")) res = s->set_gain_ctrl(s, val);
    else if(!strcmp(variable, "aec")) res = s->set_exposure_ctrl(s, val);
    else if(!strcmp(variable, "hmirror")) res = s->set_hmirror(s, val);
    else if(!strcmp(variable, "vflip")) res = s->set_vflip(s, val);
    else if(!strcmp(variable, "awb_gain")) res = s->set_awb_gain(s, val);
    else if(!strcmp(variable, "agc_gain")) res = s->set_agc_gain(s, val);
    else if(!strcmp(variable, "aec_value")) res = s->set_aec_value(s, val);
    else if(!strcmp(variable, "aec2")) res = s->set_aec2(s, val);
    else if(!strcmp(variable, "dcw")) res = s->set_dcw(s, val);
    else if(!strcmp(variable, "bpc")) res = s->set_bpc(s, val);
    else if(!strcmp(variable, "wpc")) res = s->set_wpc(s, val);
    else if(!strcmp(variable, "raw_gma")) res = s->set_raw_gma(s, val);
    else if(!strcmp(variable, "lenc")) res = s->set_lenc(s, val);
    else if(!strcmp(variable, "special_effect")) res = s->set_special_effect(s, val);
    else if(!strcmp(variable, "wb_mode")) res = s->set_wb_mode(s, val);
    else if(!strcmp(variable, "ae_level")) res = s->set_ae_level(s, val);
    else {
        res = -1;
    }

    if(res){
        return httpd_resp_send_500(req);
    }

    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    return httpd_resp_send(req, NULL, 0);
}

// ===========================
// Start Servers
// ===========================
void startCameraServer() {
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();
    
    // Main web server
    config.server_port = 80;
    config.ctrl_port = 32768;
    config.lru_purge_enable = true;
    config.max_uri_handlers = 8;
    config.stack_size = 8192;
    
    httpd_uri_t index_uri = {
        .uri       = "/",
        .method    = HTTP_GET,
        .handler   = index_handler,
        .user_ctx  = NULL
    };
    
    httpd_uri_t capture_uri = {
        .uri       = "/capture",
        .method    = HTTP_GET,
        .handler   = capture_handler,
        .user_ctx  = NULL
    };
    
    httpd_uri_t info_uri = {
        .uri       = "/info",
        .method    = HTTP_GET,
        .handler   = system_info_handler,
        .user_ctx  = NULL
    };
    
    httpd_uri_t reset_uri = {
        .uri       = "/reset",
        .method    = HTTP_GET,
        .handler   = reset_handler,
        .user_ctx  = NULL
    };
    
    httpd_uri_t cmd_uri = {
        .uri       = "/control",
        .method    = HTTP_GET,
        .handler   = cmd_handler,
        .user_ctx  = NULL
    };
    
    Serial.printf("Starting web server on port: %d\n", config.server_port);
    if (httpd_start(&camera_httpd, &config) == ESP_OK) {
        httpd_register_uri_handler(camera_httpd, &index_uri);
        httpd_register_uri_handler(camera_httpd, &capture_uri);
        httpd_register_uri_handler(camera_httpd, &info_uri);
        httpd_register_uri_handler(camera_httpd, &reset_uri);
        httpd_register_uri_handler(camera_httpd, &cmd_uri);
        Serial.println("Web server started successfully");
    } else {
        Serial.println("Failed to start web server!");
    }
    
    // Stream server with optimized settings
    config.server_port = 81;
    config.ctrl_port = 32769;
    config.max_uri_handlers = 3;
    config.max_resp_headers = 16;
    config.recv_wait_timeout = 10;
    config.send_wait_timeout = 10;
    config.lru_purge_enable = true;
    config.core_id = 0;  // Pin to core 0
    config.stack_size = 8192;
    
    httpd_uri_t stream_uri = {
        .uri       = "/stream",
        .method    = HTTP_GET,
        .handler   = stream_handler,
        .user_ctx  = NULL
    };
    
    httpd_uri_t simple_uri = {
        .uri       = "/simple",
        .method    = HTTP_GET,
        .handler   = simple_stream_handler,
        .user_ctx  = NULL
    };
    
    Serial.printf("Starting stream server on port: %d\n", config.server_port);
    if (httpd_start(&stream_httpd, &config) == ESP_OK) {
        httpd_register_uri_handler(stream_httpd, &stream_uri);
        httpd_register_uri_handler(stream_httpd, &simple_uri);
        Serial.println("Stream server started successfully");
    } else {
        Serial.println("Failed to start stream server!");
    }
}

// ===========================
// Setup
// ===========================
void setup() {
    WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); // Disable brownout detector
    
    bootTime = millis();
    Serial.begin(115200);
    Serial.setDebugOutput(true);
    Serial.println("\n\nESP32-CAM Starting...");
    
    // Camera configuration
    camera_config_t config;
    config.ledc_channel = LEDC_CHANNEL_0;
    config.ledc_timer = LEDC_TIMER_0;
    config.pin_d0 = Y2_GPIO_NUM;
    config.pin_d1 = Y3_GPIO_NUM;
    config.pin_d2 = Y4_GPIO_NUM;
    config.pin_d3 = Y5_GPIO_NUM;
    config.pin_d4 = Y6_GPIO_NUM;
    config.pin_d5 = Y7_GPIO_NUM;
    config.pin_d6 = Y8_GPIO_NUM;
    config.pin_d7 = Y9_GPIO_NUM;
    config.pin_xclk = XCLK_GPIO_NUM;
    config.pin_pclk = PCLK_GPIO_NUM;
    config.pin_vsync = VSYNC_GPIO_NUM;
    config.pin_href = HREF_GPIO_NUM;
    config.pin_sscb_sda = SIOD_GPIO_NUM;
    config.pin_sscb_scl = SIOC_GPIO_NUM;
    config.pin_pwdn = PWDN_GPIO_NUM;
    config.pin_reset = RESET_GPIO_NUM;
    config.xclk_freq_hz = 20000000;
    config.pixel_format = PIXFORMAT_JPEG;
    config.grab_mode = CAMERA_GRAB_LATEST;
    
    // Init with appropriate specs based on PSRAM availability
    if(psramFound()) {
        Serial.println("PSRAM found");
        config.frame_size = FRAMESIZE_SVGA;  // 800x600
        config.jpeg_quality = 10;            // 0-63 lower means higher quality
        config.fb_count = 2;
        config.fb_location = CAMERA_FB_IN_PSRAM;
    } else {
        Serial.println("No PSRAM");
        config.frame_size = FRAMESIZE_VGA;   // 640x480
        config.jpeg_quality = 12;
        config.fb_count = 1;
        config.fb_location = CAMERA_FB_IN_DRAM;
    }
    
    // Power cycle camera for reliable initialization
    pinMode(PWDN_GPIO_NUM, OUTPUT);
    digitalWrite(PWDN_GPIO_NUM, HIGH);
    delay(100);
    digitalWrite(PWDN_GPIO_NUM, LOW);
    delay(100);
    
    // Camera init
    esp_err_t err = esp_camera_init(&config);
    if (err != ESP_OK) {
        Serial.printf("Camera init failed with error 0x%x\n", err);
        delay(1000);
        ESP.restart();
    }
    
    Serial.println("Camera initialized successfully");
    
    // Optimize camera settings for streaming
    sensor_t * s = esp_camera_sensor_get();
    s->set_framesize(s, FRAMESIZE_CIF);     // 352x288 (smaller than VGA for better streaming)
    s->set_quality(s, 15);                  // Lower quality for better streaming
    s->set_brightness(s, 1);                // Slightly increase brightness
    s->set_contrast(s, 1);                  // Slightly increase contrast
    s->set_saturation(s, 1);                // Slightly increase saturation
    
    // Setup WiFi connection with optimized parameters
    WiFi.mode(WIFI_STA);
    WiFi.setSleep(false);                    // Disable WiFi sleep mode
    WiFi.begin(ssid, password);
    
    // Set auto-reconnect
    WiFi.persistent(true);
    WiFi.setAutoReconnect(true);
    
    Serial.print("Connecting to WiFi");
    
    // Wait for connection with timeout
    int wifiTimeout = 0;
    while (WiFi.status() != WL_CONNECTED && wifiTimeout < 20) {
        delay(500);
        Serial.print(".");
        wifiTimeout++;
    }
    
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("\nWiFi connection failed. Restarting...");
        delay(1000);
        ESP.restart();
    }
    
    Serial.println("\nWiFi connected");
    Serial.print("SSID: ");
    Serial.println(WiFi.SSID());
    Serial.print("RSSI: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());
    
    Serial.print("Camera Ready! Use 'http://");
    Serial.print(WiFi.localIP());
    Serial.println("' to connect");
    Serial.print("Stream URL: 'http://");
    Serial.print(WiFi.localIP());
    Serial.println(":81/stream'");
    
    startCameraServer();
}

// ===========================
// Loop
// ===========================
void loop() {
    // Check WiFi connection every 10 seconds
    if (millis() - lastWiFiCheck > 10000) {
        lastWiFiCheck = millis();
        
        if (WiFi.status() != WL_CONNECTED) {
            Serial.println("WiFi disconnected, attempting to reconnect...");
            WiFi.reconnect();
            
            // Wait for reconnection with timeout
            int reconnectAttempts = 0;
            while (WiFi.status() != WL_CONNECTED && reconnectAttempts < 10) {
                delay(500);
                Serial.print(".");
                reconnectAttempts++;
            }
            
            if (WiFi.status() == WL_CONNECTED) {
                Serial.println("WiFi reconnected successfully");
            } else {
                Serial.println("WiFi reconnection failed, restarting device");
                delay(1000);
                ESP.restart();
            }
        }
        
        // Print system stats periodically
        Serial.printf("System Status - Uptime: %lu s, Heap: %u bytes, PSRAM: %u bytes, RSSI: %d dBm\n",
            (millis() - bootTime) / 1000,
            ESP.getFreeHeap(),
            ESP.getFreePsram(),
            WiFi.RSSI()
        );
        
        if (streamActive) {
            Serial.printf("Stream active for %lu seconds, processed %d frames\n", 
                streamDuration, frameCount);
        }
    }
    
    // Small delay to prevent task issues
    delay(100);
}