package com.comtrade.gcb.data.jpa;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class GiftProvider {
    @Id
    private String id;
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "gift_provider_country",
            joinColumns = { @JoinColumn(name = "id", nullable = false, updatable = false)},
            inverseJoinColumns = { @JoinColumn(name = "code", nullable = false, updatable = false)})
    private Set<Country> countries = new HashSet<Country>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }
}