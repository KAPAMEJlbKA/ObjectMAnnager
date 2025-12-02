package com.kapamejlbka.objectmanager.legacy.objectmannage.web.form;

import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Device;
import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.DeviceType;
import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Site;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

public class DeviceForm {

    private static final String IP_REGEX = "^(?:$|(?:(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)|(?:(?:[0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|(?:[0-9A-Fa-f]{1,4}:){1,7}:|:(?:[0-9A-Fa-f]{1,4}:){1,7}|(?:[0-9A-Fa-f]{1,4}:){1,6}:[0-9A-Fa-f]{1,4}|(?:[0-9A-Fa-f]{1,4}:){1,5}(?::[0-9A-Fa-f]{1,4}){1,2}|(?:[0-9A-Fa-f]{1,4}:){1,4}(?::[0-9A-Fa-f]{1,4}){1,3}|(?:[0-9A-Fa-f]{1,4}:){1,3}(?::[0-9A-Fa-f]{1,4}){1,4}|(?:[0-9A-Fa-f]{1,4}:){1,2}(?::[0-9A-Fa-f]{1,4}){1,5}|[0-9A-Fa-f]{1,4}:(?::[0-9A-Fa-f]{1,4}){1,6})))$";

    @NotNull(message = "Выберите площадку")
    private UUID siteId;

    @NotNull(message = "Выберите тип")
    private DeviceType type;

    @Size(max = 100, message = "Модель не должна превышать 100 символов")
    private String model;

    @Size(max = 100, message = "Серийный номер не должен превышать 100 символов")
    private String serial;

    @Size(max = 45, message = "IP не должен превышать 45 символов")
    @Pattern(regexp = IP_REGEX, message = "Укажите корректный IP-адрес")
    private String ip;

    @Size(max = 255, message = "Описание местоположения не должно превышать 255 символов")
    private String locationNote;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate installedAt;

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLocationNote() {
        return locationNote;
    }

    public void setLocationNote(String locationNote) {
        this.locationNote = locationNote;
    }

    public LocalDate getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDate installedAt) {
        this.installedAt = installedAt;
    }

    public void applyTo(Device device, Site site) {
        device.setSite(site);
        device.setType(type);
        device.setModel(normalize(model));
        device.setSerial(normalize(serial));
        device.setIp(normalize(ip));
        device.setLocationNote(normalize(locationNote));
        device.setInstalledAt(installedAt);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static DeviceForm fromDevice(Device device) {
        DeviceForm form = new DeviceForm();
        form.setSiteId(device.getSite() != null ? device.getSite().getId() : null);
        form.setType(device.getType());
        form.setModel(device.getModel());
        form.setSerial(device.getSerial());
        form.setIp(device.getIp());
        form.setLocationNote(device.getLocationNote());
        form.setInstalledAt(device.getInstalledAt());
        return form;
    }
}
