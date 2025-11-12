package com.kapamejlbka.objectmannage.web.form;

import com.kapamejlbka.objectmannage.domain.Site;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SiteForm {

    @NotBlank(message = "Укажите название")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @Size(max = 80, message = "Город не должен превышать 80 символов")
    private String city;

    @Size(max = 255, message = "Адрес не должен превышать 255 символов")
    private String address;

    @Size(max = 80, message = "Имя контакта не должно превышать 80 символов")
    private String contactName;

    @Size(max = 32, message = "Телефон не должен превышать 32 символов")
    private String contactPhone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void applyTo(Site site) {
        site.setName(name != null ? name.trim() : null);
        site.setCity(city != null && !city.isBlank() ? city.trim() : null);
        site.setAddress(address != null && !address.isBlank() ? address.trim() : null);
        site.setContactName(contactName != null && !contactName.isBlank() ? contactName.trim() : null);
        site.setContactPhone(contactPhone != null && !contactPhone.isBlank() ? contactPhone.trim() : null);
    }

    public static SiteForm fromSite(Site site) {
        SiteForm form = new SiteForm();
        form.setName(site.getName());
        form.setCity(site.getCity());
        form.setAddress(site.getAddress());
        form.setContactName(site.getContactName());
        form.setContactPhone(site.getContactPhone());
        return form;
    }
}
