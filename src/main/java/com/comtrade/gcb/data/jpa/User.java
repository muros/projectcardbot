package com.comtrade.gcb.data.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
public class User {
    @Id
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private Date createdDate;
    private Date lastAccessed;
    private Boolean isActive = Boolean.TRUE;
    private String location;
    private BigDecimal maxLimit;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public Boolean getActive() {
        return isActive;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(BigDecimal maxLimit) {
        this.maxLimit = maxLimit;
    }
}