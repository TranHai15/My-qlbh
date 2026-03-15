package com.grocerypos.inventory.entity;

import com.grocerypos.core.base.BaseEntity;

/**
 * Thực thể Nhà cung cấp.
 */
public class Supplier extends BaseEntity {
    private String name;
    private String phone;
    private String email;
    private String address;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
