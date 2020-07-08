package com.xlb.service.data.client.util.http.enums;

import org.apache.hc.client5.http.classic.methods.*;

/**
 * http/https提交请求方法
 *
 * @author 赵嘉楠
 * @version 1.0
 * @since 1.0
 */
public enum HttpMethod {
    OPTIONS {
        public Class<? extends HttpUriRequest> getMethodClass() {
            return HttpOptions.class;
        }
    },
    GET {
        public Class<? extends HttpUriRequest> getMethodClass() {
            return HttpGet.class;
        }
    }, HEAD {
        public Class<? extends HttpUriRequest> getMethodClass() {
            return HttpHead.class;
        }
    }, POST {
        public Class<? extends HttpUriRequest> getMethodClass() {
            return HttpPost.class;
        }
    }, PUT {
        public Class<? extends HttpUriRequest> getMethodClass() {
            return HttpPut.class;
        }
    }, DELETE {
        public Class<? extends HttpUriRequest> getMethodClass() {
            return HttpDelete.class;
        }
    }, TRACE {
        public Class<? extends HttpUriRequest> getMethodClass() {
            return HttpTrace.class;
        }
    };

    /**
     * 获得请求所对应的请求类
     *
     * @return HttpClient 请求类型
     */
    public abstract Class<? extends HttpUriRequest> getMethodClass();
}

