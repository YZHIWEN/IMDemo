package com.yzw.demoim.im;

/**
 * IM 配置类
 * Created by yzw on 2016/3/15 0015.
 */
public class IMConfig {
    String ip;
    int port;
    String serviceName;

    private IMConfig(Builder builder) {
        this.ip = builder.ip;
        this.port = builder.port;
        this.serviceName = builder.serviceName;
    }

    public static class Builder {

        String ip;
        int port;
        String serviceName;

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }


        public IMConfig build() {
            return new IMConfig(this);
        }
    }

}
