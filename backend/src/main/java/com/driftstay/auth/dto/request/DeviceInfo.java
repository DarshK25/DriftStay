package com.driftstay.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceInfo {
    private String deviceName;
    private String deviceType;
    private String deviceId;
}
